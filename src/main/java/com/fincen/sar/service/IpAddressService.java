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
public class IpAddressService {

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
