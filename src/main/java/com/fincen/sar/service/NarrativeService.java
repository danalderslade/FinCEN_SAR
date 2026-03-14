package com.fincen.sar.service;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NarrativeService {

    private final ActivityNarrativeRepository repo;
    private final ActivityRepository activityRepo;

    @Transactional(readOnly = true)
    public List<NarrativeResponse> listByActivity(Long activityId) {
        return repo.findByActivityIdOrderByNarrativeSequenceNumber(activityId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public NarrativeResponse add(Long activityId, NarrativeRequest req) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));
        ActivityNarrative n = ActivityNarrative.builder()
                .activity(activity).seqNum(req.getSeqNum())
                .narrativeSequenceNumber(req.getNarrativeSequenceNumber())
                .narrativeText(req.getNarrativeText())
                .build();
        return toResponse(repo.save(n));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityNarrative", id)));
    }

    private NarrativeResponse toResponse(ActivityNarrative n) {
        return NarrativeResponse.builder()
                .id(n.getId()).narrativeSequenceNumber(n.getNarrativeSequenceNumber())
                .narrativeText(n.getNarrativeText())
                .build();
    }
}
