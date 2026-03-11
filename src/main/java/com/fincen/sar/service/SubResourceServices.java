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

// ══════════════════════════════════════════════════════════════════════════════
// PartyService
// ══════════════════════════════════════════════════════════════════════════════

@Service
@RequiredArgsConstructor
class PartyService {

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
        // reuse builder in ActivityService via package-private access
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

// ══════════════════════════════════════════════════════════════════════════════
// SuspiciousActivityService
// ══════════════════════════════════════════════════════════════════════════════

@Service
@RequiredArgsConstructor
class SuspiciousActivityService {

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

// ══════════════════════════════════════════════════════════════════════════════
// IpAddressService
// ══════════════════════════════════════════════════════════════════════════════

@Service
@RequiredArgsConstructor
class IpAddressService {

    private final ActivityIpAddressRepository repo;
    private final ActivityRepository activityRepo;

    @Transactional(readOnly = true)
    public List<IpAddressResponse> listByActivity(Long activityId) {
        return repo.findByActivityId(activityId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public IpAddressResponse add(Long activityId, IpAddressRequest req) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));
        ActivityIpAddress ip = ActivityIpAddress.builder()
                .activity(activity).seqNum(req.getSeqNum())
                .ipAddressText(req.getIpAddressText())
                .ipAddressDate(req.getIpAddressDate())
                .ipAddressTimestamp(req.getIpAddressTimestamp())
                .build();
        return toResponse(repo.save(ip));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityIpAddress", id)));
    }

    private IpAddressResponse toResponse(ActivityIpAddress ip) {
        return IpAddressResponse.builder()
                .id(ip.getId()).ipAddressText(ip.getIpAddressText())
                .ipAddressDate(ip.getIpAddressDate()).ipAddressTimestamp(ip.getIpAddressTimestamp())
                .build();
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// NarrativeService
// ══════════════════════════════════════════════════════════════════════════════

@Service
@RequiredArgsConstructor
class NarrativeService {

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
