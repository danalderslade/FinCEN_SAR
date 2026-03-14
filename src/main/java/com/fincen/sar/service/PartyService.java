package com.fincen.sar.service;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.mapper.SarMapper;
import com.fincen.sar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository repo;
    private final ActivityRepository activityRepo;
    private final SarMapper mapper;
    private final ActivityService activityService;

    @Transactional(readOnly = true)
    public List<PartyResponse> listByActivity(Long activityId) {
        return repo.findByActivityId(activityId).stream().map(mapper::toPartyResponse).toList();
    }

    @Transactional(readOnly = true)
    public PartyResponse getById(Long id) {
        return mapper.toPartyResponse(findOrThrow(id));
    }

    @Transactional
    public PartyResponse addToActivity(Long activityId, PartyRequest req) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));
        Party party = activityService.buildPartyPublic(activity, req);
        activity.getParties().add(party);
        return mapper.toPartyResponse(repo.save(party));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(findOrThrow(id));
    }

    private Party findOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Party", id));
    }
}
