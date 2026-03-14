package com.fincen.sar.service;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.mapper.SarMapper;
import com.fincen.sar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SuspiciousActivityService {

    private final SuspiciousActivityRepository repo;
    private final ActivityRepository activityRepo;
    private final SarMapper mapper;
    private final ActivityService activityService;

    @Transactional(readOnly = true)
    public SuspiciousActivityResponse getByActivity(Long activityId) {
        return mapper.toSaResponsePublic(repo.findByActivityId(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("SuspiciousActivity for activity", activityId)));
    }

    @Transactional
    public SuspiciousActivityResponse upsert(Long activityId, SuspiciousActivityRequest req) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));

        repo.findByActivityId(activityId).ifPresent(repo::delete);

        SuspiciousActivity sa = activityService.buildSaPublic(activity, req);
        return mapper.toSaResponsePublic(repo.save(sa));
    }

    @Transactional
    public void delete(Long activityId) {
        SuspiciousActivity sa = repo.findByActivityId(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("SuspiciousActivity for activity", activityId));
        repo.delete(sa);
    }
}
