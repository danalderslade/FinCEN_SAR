package com.fincen.sar.service;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.mapper.SarMapper;
import com.fincen.sar.repository.EfilingBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EfilingBatchService {

    private final EfilingBatchRepository repo;
    private final SarMapper mapper;

    @Transactional
    public EfilingBatchResponse create(EfilingBatchRequest req) {
        EfilingBatch batch = EfilingBatch.builder()
                .activityCount(req.getActivityCount())
                .totalAmount(req.getTotalAmount())
                .partyCount(req.getPartyCount())
                .activityAttachmentCount(req.getActivityAttachmentCount())
                .attachmentCount(req.getAttachmentCount())
                .build();
        return mapper.toBatchResponse(repo.save(batch));
    }

    @Transactional(readOnly = true)
    public EfilingBatchResponse getById(Long id) {
        return mapper.toBatchResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EfilingBatchResponse> getAll() {
        return repo.findAll().stream().map(mapper::toBatchResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        EfilingBatch batch = findOrThrow(id);
        repo.delete(batch);
    }

    private EfilingBatch findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EfilingBatch", id));
    }
}
