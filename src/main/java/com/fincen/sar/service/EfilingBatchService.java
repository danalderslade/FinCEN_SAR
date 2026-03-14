package com.fincen.sar.service;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.mapper.SarMapper;
import com.fincen.sar.repository.EfilingBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        var builder = EfilingBatch.builder()
                .activityCount(req.getActivityCount())
                .totalAmount(req.getTotalAmount())
                .partyCount(req.getPartyCount());
        if (req.getActivityAttachmentCount() != null) {
            builder.activityAttachmentCount(req.getActivityAttachmentCount());
        }
        if (req.getAttachmentCount() != null) {
            builder.attachmentCount(req.getAttachmentCount());
        }
        return mapper.toBatchResponse(repo.save(builder.build()));
    }

    @Transactional(readOnly = true)
    public EfilingBatchResponse getById(Long id) {
        return mapper.toBatchResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EfilingBatchResponse> getAll() {
        return repo.findAll().stream().map(mapper::toBatchResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<EfilingBatchResponse> list(FilingStatus status, Pageable pageable) {
        Page<EfilingBatch> page = (status != null)
                ? repo.findByFilingStatus(status, pageable)
                : repo.findAll(pageable);

        return PageResponse.<EfilingBatchResponse>builder()
                .content(page.getContent().stream().map(mapper::toBatchResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
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
