package com.tfmsp.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.entity.Bureau;
import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.BlockRepository;
import com.tfmsp.backend.repository.BureauRepository;
import com.tfmsp.backend.repository.ProfileRepository;
import com.tfmsp.backend.repository.UserRepository;

/**
 * One place that decides whether a user may see or reach a profile (FRD SRC-04, SRC-06, INT-04).
 * Anything not allowed answers 404, so nobody can find out that a profile exists.
 */
@Service
public class ProfileAccessService {

    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;
    private final ProfileRepository profileRepository;
    private final BlockRepository blockRepository;

    public ProfileAccessService(UserRepository userRepository,
            BureauRepository bureauRepository,
            ProfileRepository profileRepository,
            BlockRepository blockRepository) {
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
        this.profileRepository = profileRepository;
        this.blockRepository = blockRepository;
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
    }

    @Transactional(readOnly = true)
    public void assertCanView(Long viewerUserId, Profile target) {
        // own profile
        if (target.getUserId().equals(viewerUserId)) {
            return;
        }

        User viewer = userRepository.findById(viewerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (viewer.getRole() == Role.SUPER_ADMIN) {
            return;
        }

        User owner = userRepository.findById(target.getUserId()).orElseThrow(this::notFound);
        boolean sameBureau = viewer.getBureauId() != null && viewer.getBureauId().equals(owner.getBureauId());

        // a Host sees only members of their own bureau
        if (viewer.getRole() == Role.HOST) {
            if (!sameBureau) {
                throw notFound();
            }
            return;
        }

        // candidate rules: active, visible, not blocked either way
        if (owner.getStatus() != UserStatus.ACTIVE || owner.isBlocked()
                || target.getVisibility() != Visibility.VISIBLE) {
            throw notFound();
        }
        boolean blocked = profileRepository.findByUserId(viewerUserId)
                .map(vp -> blockRepository.existsByBlockerProfileIdAndBlockedProfileId(vp.getId(), target.getId())
                        || blockRepository.existsByBlockerProfileIdAndBlockedProfileId(target.getId(), vp.getId()))
                .orElse(false);
        if (blocked) {
            throw notFound();
        }

        if (sameBureau) {
            return;
        }

        // other bureau: needs the shared network on both sides AND the member's own consent
        Bureau viewerBureau = viewer.getBureauId() == null ? null
                : bureauRepository.findById(viewer.getBureauId()).orElse(null);
        Bureau ownerBureau = owner.getBureauId() == null ? null
                : bureauRepository.findById(owner.getBureauId()).orElse(null);
        boolean allowed = viewerBureau != null && ownerBureau != null
                && viewerBureau.isSearchOtherBureaus()
                && ownerBureau.isShareMembers()
                && "ACTIVE".equalsIgnoreCase(ownerBureau.getStatus())
                && target.isShareConsent();
        if (!allowed) {
            throw notFound();
        }
    }
}