package com.tfmsp.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.entity.Block;
import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.repository.BlockRepository;
import com.tfmsp.backend.repository.ProfileRepository;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final ProfileRepository profileRepository;

    public BlockService(BlockRepository blockRepository, ProfileRepository profileRepository) {
        this.blockRepository = blockRepository;
        this.profileRepository = profileRepository;
    }

    private Profile myProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profile not found. Please complete your profile."));
    }

    @Transactional
    public void block(Long userId, Long targetProfileId) {
        Profile me = myProfile(userId);
        if (me.getId().equals(targetProfileId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot block yourself");
        }
        profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target profile not found"));
        if (blockRepository.existsByBlockerProfileIdAndBlockedProfileId(me.getId(), targetProfileId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile is already blocked");
        }
        blockRepository.save(new Block(me.getId(), targetProfileId));
    }

    @Transactional
    public void unblock(Long userId, Long targetProfileId) {
        Profile me = myProfile(userId);
        blockRepository.deleteByBlockerProfileIdAndBlockedProfileId(me.getId(), targetProfileId);
    }

    public List<Long> getBlockedProfileIds(Long userId) {
        Profile me = myProfile(userId);
        return blockRepository.findByBlockerProfileId(me.getId()).stream()
                .map(Block::getBlockedProfileId)
                .collect(Collectors.toList());
    }
}