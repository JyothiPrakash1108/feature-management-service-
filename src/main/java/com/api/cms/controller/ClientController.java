package com.api.cms.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.cms.service.FeatureFlagService;
import com.api.cms.util.ApiSecurityUtil;

@RestController
@RequestMapping("/api")
public class ClientController {
    private static final Logger log = LoggerFactory.getLogger(ClientController.class);
    private FeatureFlagService featureFlagService;
    public ClientController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }
    @GetMapping("/features/{featureName}")
    public ResponseEntity<Boolean> checkFeatureFlag(@PathVariable String featureName) {
        UUID companyId = ApiSecurityUtil.getCompanyId();
        log.info("Get featurestatus featureName:{} companyId:{}",featureName,companyId);
        boolean result = featureFlagService.isFeatureEnabledForCompany(featureName, companyId);
        log.info("Feature status returned successfully featureName:{} companyId:{}",featureName,companyId);
        return ResponseEntity.ok(result);
    }
}
