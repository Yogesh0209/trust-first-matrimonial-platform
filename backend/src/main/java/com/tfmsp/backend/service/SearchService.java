package com.tfmsp.backend.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.SearchFilterCriteria;
import com.tfmsp.backend.dto.SearchResultResponse;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.Gender;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.*;

@Service
public class SearchService {

    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;
    private final ShortlistRepository shortlistRepository;
    private final ProfileService profileService;
    private final BlockRepository blockRepository;

    public SearchService(
            ProfileRepository profileRepository,
            ProfilePhotoRepository profilePhotoRepository,
            UserRepository userRepository,
            BureauRepository bureauRepository,
            ShortlistRepository shortlistRepository,
            ProfileService profileService,
            BlockRepository blockRepository) {
        this.profileRepository = profileRepository;
        this.profilePhotoRepository = profilePhotoRepository;
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
        this.shortlistRepository = shortlistRepository;
        this.profileService = profileService;
        this.blockRepository = blockRepository;
    }

    public List<SearchResultResponse> searchHomeBureau(Long callerUserId, SearchFilterCriteria criteria) {
        User caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caller user not found"));

        if (caller.getBureauId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with any bureau");
        }

        Bureau callerBureau = bureauRepository.findById(caller.getBureauId()).orElse(null);
        Optional<Profile> callerProfileOpt = profileRepository.findByUserId(callerUserId);

        // Fetch candidate users in the same bureau
        List<User> candidateUsers = userRepository.findByBureauIdAndRole(caller.getBureauId(), Role.END_USER)
                .stream()
                .filter(u -> !u.getId().equals(callerUserId))
                .filter(u -> u.getStatus() == UserStatus.ACTIVE && !u.isBlocked())
                .collect(Collectors.toList());

        return filterAndBuildResults(candidateUsers, callerProfileOpt, criteria, callerBureau, false);
    }

    public List<SearchResultResponse> searchShared(Long callerUserId, SearchFilterCriteria criteria) {
        User caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caller user not found"));

        if (caller.getBureauId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with any bureau");
        }

        Bureau callerBureau = bureauRepository.findById(caller.getBureauId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bureau not found"));

        if (!callerBureau.isSearchOtherBureaus()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your bureau has not enabled cross-bureau discovery");
        }

        Optional<Profile> callerProfileOpt = profileRepository.findByUserId(callerUserId);

        // Find other bureaus that allow sharing members
        List<Bureau> partnerBureaus = bureauRepository.findByShareMembersTrueAndStatus("ACTIVE")
                .stream()
                .filter(b -> !b.getId().equals(caller.getBureauId()))
                .collect(Collectors.toList());

        List<User> candidateUsers = new ArrayList<>();
        Map<Long, Bureau> bureauMap = new HashMap<>();
        for (Bureau pb : partnerBureaus) {
            bureauMap.put(pb.getId(), pb);
            // Only members who gave cross-bureau sharing consent (FRD SRC-05)
            List<User> members = userRepository.findByBureauIdAndRole(pb.getId(), Role.END_USER)
                    .stream()
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE && !u.isBlocked())
                    .filter(u -> profileRepository.findByUserId(u.getId())
                            .map(Profile::isShareConsent).orElse(false))
                    .collect(Collectors.toList());
            candidateUsers.addAll(members);
        }

        return filterAndBuildResults(candidateUsers, callerProfileOpt, criteria, callerBureau, true, bureauMap);
    }

    @Transactional
    public void addToShortlist(Long callerUserId, Long targetProfileId) {
        Profile callerProfile = profileRepository.findByUserId(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caller profile not found"));

        if (callerProfile.getId().equals(targetProfileId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot shortlist your own profile");
        }

        Profile targetProfile = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target profile not found"));

        if (targetProfile.getVisibility() != Visibility.VISIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot shortlist a hidden profile");
        }

        ShortlistId id = new ShortlistId(callerProfile.getId(), targetProfileId);
        if (!shortlistRepository.existsById(id)) {
            shortlistRepository.save(new Shortlist(callerProfile.getId(), targetProfileId));
        }
    }

    @Transactional
    public void removeFromShortlist(Long callerUserId, Long targetProfileId) {
        Profile callerProfile = profileRepository.findByUserId(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caller profile not found"));

        ShortlistId id = new ShortlistId(callerProfile.getId(), targetProfileId);
        if (shortlistRepository.existsById(id)) {
            shortlistRepository.deleteById(id);
        }
    }

    public List<SearchResultResponse> getShortlistedProfiles(Long callerUserId) {
        Profile callerProfile = profileRepository.findByUserId(callerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caller profile not found"));

        List<Shortlist> shortlists = shortlistRepository.findByIdProfileIdOrderByCreatedAtDesc(callerProfile.getId());
        List<SearchResultResponse> results = new ArrayList<>();

        for (Shortlist s : shortlists) {
            Long targetProfileId = s.getId().getShortlistedProfileId();
            profileRepository.findById(targetProfileId).ifPresent(targetProfile -> {
                userRepository.findById(targetProfile.getUserId()).ifPresent(targetUser -> {
                    Bureau bureau = targetUser.getBureauId() != null
                            ? bureauRepository.findById(targetUser.getBureauId()).orElse(null)
                            : null;
                    SearchResultResponse res = buildResult(targetProfile, targetUser, bureau, true, true);
                    results.add(res);
                });
            });
        }

        return results;
    }

    private List<SearchResultResponse> filterAndBuildResults(
            List<User> candidateUsers,
            Optional<Profile> callerProfileOpt,
            SearchFilterCriteria criteria,
            Bureau callerBureau,
            boolean isCrossBureau) {
        return filterAndBuildResults(candidateUsers, callerProfileOpt, criteria, callerBureau, isCrossBureau, Collections.emptyMap());
    }

    private List<SearchResultResponse> filterAndBuildResults(
            List<User> candidateUsers,
            Optional<Profile> callerProfileOpt,
            SearchFilterCriteria criteria,
            Bureau callerBureau,
            boolean isCrossBureau,
            Map<Long, Bureau> bureauMap) {

        if (candidateUsers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = candidateUsers.stream().map(User::getId).collect(Collectors.toList());
        List<Profile> profiles = profileRepository.findByUserIdInAndVisibility(userIds, Visibility.VISIBLE);

        Map<Long, User> userMap = candidateUsers.stream().collect(Collectors.toMap(User::getId, u -> u));
        Long callerProfileId = callerProfileOpt.map(Profile::getId).orElse(null);
        Set<Long> hiddenProfileIds = new HashSet<>();
        if (callerProfileId != null) {
            for (Block b : blockRepository.findByBlockerProfileIdOrBlockedProfileId(callerProfileId, callerProfileId)) {
                hiddenProfileIds.add(b.getBlockerProfileId().equals(callerProfileId)
                        ? b.getBlockedProfileId() : b.getBlockerProfileId());
            }
        }

        // Determine default target gender if caller has a gender and criteria does not specify
        Gender targetGender = (criteria != null && criteria.getGender() != null)
                ? criteria.getGender()
                : callerProfileOpt.map(p -> {
                    if (p.getGender() == Gender.MALE) return Gender.FEMALE;
                    if (p.getGender() == Gender.FEMALE) return Gender.MALE;
                    return null;
                }).orElse(null);

        List<SearchResultResponse> results = new ArrayList<>();

        for (Profile p : profiles) {
            User user = userMap.get(p.getUserId());
            if (user == null) continue;
            if (hiddenProfileIds.contains(p.getId())) continue;

            // 1. Gender filter
            if (targetGender != null && p.getGender() != targetGender) {
                continue;
            }

            // 2. Age filter
            int age = Period.between(p.getDateOfBirth(), LocalDate.now()).getYears();
            if (criteria != null) {
                if (criteria.getMinAge() != null && age < criteria.getMinAge()) continue;
                if (criteria.getMaxAge() != null && age > criteria.getMaxAge()) continue;

                // 3. Religion
                if (criteria.getReligion() != null && !criteria.getReligion().isBlank()) {
                    if (p.getReligion() == null || !p.getReligion().equalsIgnoreCase(criteria.getReligion().trim())) {
                        continue;
                    }
                }

                // 4. Marital Status
                if (criteria.getMaritalStatus() != null && p.getMaritalStatus() != criteria.getMaritalStatus()) {
                    continue;
                }

                // 5. City
                if (criteria.getCity() != null && !criteria.getCity().isBlank()) {
                    if (p.getCity() == null || !p.getCity().toLowerCase().contains(criteria.getCity().trim().toLowerCase())) {
                        continue;
                    }
                }

                // 6. Education
                if (criteria.getEducation() != null && !criteria.getEducation().isBlank()) {
                    if (p.getEducation() == null || !p.getEducation().toLowerCase().contains(criteria.getEducation().trim().toLowerCase())) {
                        continue;
                    }
                }

                // 7. Occupation
                if (criteria.getOccupation() != null && !criteria.getOccupation().isBlank()) {
                    if (p.getOccupation() == null || !p.getOccupation().toLowerCase().contains(criteria.getOccupation().trim().toLowerCase())) {
                        continue;
                    }
                }

                // 8. Trust Level
                if (criteria.getMinTrustLevel() != null && p.getTrustLevel() < criteria.getMinTrustLevel()) {
                    continue;
                }
            }

            int completeness = profileService.calculateCompleteness(p);
            int trustScore = profileService.calculateTrustScore(user, p, completeness);
            String trustBand = profileService.getTrustBand(trustScore);

            if (criteria != null) {
                if (criteria.getMinTrustScore() != null && trustScore < criteria.getMinTrustScore()) {
                    continue;
                }
                if (criteria.getTrustBand() != null && !criteria.getTrustBand().isBlank()) {
                    if (!trustBand.equalsIgnoreCase(criteria.getTrustBand().trim())) {
                        continue;
                    }
                }
            }

            boolean isShortlisted = false;
            if (callerProfileId != null) {
                isShortlisted = shortlistRepository.existsById(new ShortlistId(callerProfileId, p.getId()));
            }

            Bureau bureau = isCrossBureau ? bureauMap.get(user.getBureauId()) : callerBureau;
            SearchResultResponse res = buildResult(p, user, bureau, isShortlisted, isCrossBureau);
            res.setCompletenessPercentage(completeness);
            res.setTrustScore(trustScore);
            res.setTrustBand(trustBand);
            res.setAge(age);
            results.add(res);
        }

        // Sort by trust score descending, then last active at descending
        results.sort((a, b) -> {
            int scoreCompare = Integer.compare(b.getTrustScore(), a.getTrustScore());
            if (scoreCompare != 0) return scoreCompare;
            if (a.getLastActiveAt() == null && b.getLastActiveAt() == null) return 0;
            if (a.getLastActiveAt() == null) return 1;
            if (b.getLastActiveAt() == null) return -1;
            return b.getLastActiveAt().compareTo(a.getLastActiveAt());
        });

        return results;
    }

    private SearchResultResponse buildResult(
            Profile profile,
            User user,
            Bureau bureau,
            boolean isShortlisted,
            boolean isCrossBureau) {
        SearchResultResponse res = new SearchResultResponse();
        res.setProfileId(profile.getId());
        res.setUserId(user.getId());
        res.setFullName(user.getFullName());
        res.setGender(profile.getGender());
        res.setDateOfBirth(profile.getDateOfBirth());
        res.setAge(Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears());
        res.setMaritalStatus(profile.getMaritalStatus());
        res.setReligion(profile.getReligion());
        res.setCity(profile.getCity());
        res.setHeightCm(profile.getHeightCm());
        res.setEducation(profile.getEducation());
        res.setOccupation(profile.getOccupation());
        res.setFamilyType(profile.getFamilyType());
        res.setAbout(profile.getAbout());
        res.setTrustLevel((short) profile.getTrustLevel());
        res.setShortlisted(isShortlisted);
        res.setLastActiveAt(user.getLastActiveAt());

        if (bureau != null && (bureau.isShowBureauName() || isCrossBureau)) {
            res.setBureauName(bureau.getName());
        }

        profilePhotoRepository.findByProfileIdAndIsPrimaryTrue(profile.getId())
                .ifPresent(photo -> res.setPrimaryPhotoUrl(photo.getPhotoUrl()));

        return res;
    }
}
