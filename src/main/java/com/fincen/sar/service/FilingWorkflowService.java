package com.fincen.sar.service;

import com.fincen.sar.dto.EfilingBatchResponse;
import com.fincen.sar.entity.Activity;
import com.fincen.sar.entity.EfilingBatch;
import com.fincen.sar.entity.FilingStatus;
import com.fincen.sar.exception.InvalidStateTransitionException;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.mapper.SarMapper;
import com.fincen.sar.repository.EfilingBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilingWorkflowService {

    private final EfilingBatchRepository batchRepo;
    private final SarMapper mapper;

    private static final Map<FilingStatus, Set<FilingStatus>> TRANSITIONS = Map.of(
            FilingStatus.DRAFT,        EnumSet.of(FilingStatus.REVIEW),
            FilingStatus.REVIEW,       EnumSet.of(FilingStatus.DRAFT, FilingStatus.SUBMITTED),
            FilingStatus.SUBMITTED,    EnumSet.of(FilingStatus.ACKNOWLEDGED, FilingStatus.REJECTED),
            FilingStatus.ACKNOWLEDGED, EnumSet.noneOf(FilingStatus.class),
            FilingStatus.REJECTED,     EnumSet.of(FilingStatus.DRAFT)
    );

    @Transactional
    public EfilingBatchResponse transition(Long batchId, FilingStatus target) {
        EfilingBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("EfilingBatch", batchId));

        FilingStatus current = batch.getFilingStatus();
        Set<FilingStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition batch " + batchId + " from " + current + " to " + target);
        }

        batch.setFilingStatus(target);
        for (Activity activity : batch.getActivities()) {
            activity.setFilingStatus(target);
        }

        return mapper.toBatchResponse(batchRepo.save(batch));
    }

    @Transactional(readOnly = true)
    public EfilingBatchResponse getStatus(Long batchId) {
        EfilingBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("EfilingBatch", batchId));
        return mapper.toBatchResponse(batch);
    }
}
