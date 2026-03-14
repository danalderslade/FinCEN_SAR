package com.fincen.sar.controller;

import com.fincen.sar.service.BsaXmlGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batches/{batchId}")
@RequiredArgsConstructor
@Tag(name = "BSA XML", description = "Generate FinCEN BSA XML from batch data")
public class BsaXmlController {

    private final BsaXmlGenerationService xmlService;

    @Operation(summary = "Generate FinCEN BSA XML for a batch")
    @GetMapping(value = "/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String generateXml(@PathVariable Long batchId) {
        return xmlService.generateXml(batchId);
    }
}
