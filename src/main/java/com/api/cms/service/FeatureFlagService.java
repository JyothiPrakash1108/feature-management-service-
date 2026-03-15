package com.api.cms.service;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.api.cms.dto.featureflag.CreateFeatureFlagRequestDTO;
import com.api.cms.entity.FeatureFlag;
import com.api.cms.enums.Role;
import com.api.cms.exception.FeatureFlagAlreadyExistsException;
import com.api.cms.exception.NoSuchFeatureFlagExistsException;
import com.api.cms.repository.FeatureFlagRepo;
import com.api.cms.util.SecurityUtil;

@Service
public class FeatureFlagService {
    private static final Logger log = LoggerFactory.getLogger(FeatureFlagService.class);
    private FeatureFlagRepo featureFlagRepo;
    public FeatureFlagService(FeatureFlagRepo featureFlagRepo) {
        this.featureFlagRepo = featureFlagRepo;
    }
    public FeatureFlag createFeatureFlag(CreateFeatureFlagRequestDTO createFeatureFlagDTO) throws AccessDeniedException, FeatureFlagAlreadyExistsException {
        UUID companyId = SecurityUtil.getCompanyId();
        log.info("Creating feature flag featureName:{} companyId:{}",createFeatureFlagDTO.getFeatureFlagName(),companyId);
        String featureName = createFeatureFlagDTO.getFeatureFlagName();
        String role = SecurityUtil.getRole();
        if(!"ADMIN".equals(role)){
            log.warn("Feature creation failed featureFlag:{} reason= role_required_admin",featureName);
            throw new AccessDeniedException("only admin of this company can create feature flags");
        }
        if(featureFlagRepo.findByCompanyIdAndName(companyId, featureName).isPresent()){
            log.warn("Feature creation failed featureName:{} reason= feature_already_exists");
            throw new FeatureFlagAlreadyExistsException("feature with name "+featureName+" already exists in your company");
        }
        FeatureFlag featureFlag = new FeatureFlag();
        featureFlag.setCompanyId(companyId);
        featureFlag.setName(featureName);
        featureFlag.setEnabled(createFeatureFlagDTO.isEnabled());
        featureFlag.setCreatedAt(Instant.now());
        featureFlag.setUpdatedAt(Instant.now());
        return featureFlagRepo.save(featureFlag);
    }
    public boolean isFeatureEnabledForCompany(String featureName, UUID companyId) {
        log.debug("Fetching feature status featureName:{} companyId:{}",featureName,companyId);
        return featureFlagRepo.findByCompanyIdAndName(companyId,featureName)
                .map(FeatureFlag::isEnabled)
                .orElse(false);
    }
    public FeatureFlag toggleFeatureFlagStatus(String featureName,UUID companyId,boolean enabled) throws NoSuchFeatureFlagExistsException {
        log.info("Toggling feature flag companyId:{} featureFlagName:{}",companyId,featureName);
        Optional<FeatureFlag> featureFlagOptional = featureFlagRepo.findByCompanyIdAndName(companyId,featureName);
        if(featureFlagOptional.isEmpty()){
            log.warn("Fetching feature flag failed featureFlagName:{} reason= feature_doesNotExist",featureName);
            throw new NoSuchFeatureFlagExistsException("No feature flag with name "+featureName+" exists in your company");
        }
        FeatureFlag featureFlag = featureFlagOptional.get();
        featureFlag.setEnabled(enabled);
        featureFlag.setUpdatedAt(Instant.now());
        return featureFlagRepo.save(featureFlag);
    }
    public List<FeatureFlag> getAllFeatureFlags(UUID companyId) {
        log.info("Fetching all features companyId:{}",companyId);
        List<FeatureFlag> featureFlags = featureFlagRepo.findAllByCompanyId(companyId); 
        if(featureFlags.isEmpty()){
            return Collections.emptyList();
        }
        return featureFlags.stream().toList();
    }
    public FeatureFlag getFeatureFlagByName(String featureName, UUID companyId) throws NoSuchFeatureFlagExistsException {
        log.info("Fetching feature featureFlagName:{} companyId:{}",featureName,companyId);
        Optional<FeatureFlag> optionalFeatureFlag = featureFlagRepo.findByCompanyIdAndName(companyId,featureName);
        if(optionalFeatureFlag.isEmpty()){
            log.warn("Fetching feature failed featureFlagName:{} companyId:{} reason= feature_doesNotExist",featureName,companyId);
            throw new NoSuchFeatureFlagExistsException("No feature flag with name "+featureName+" exists in your company");
        }
        return optionalFeatureFlag.get();
    }
    public FeatureFlag deleteFeatureFlag(UUID companyId, String featureName) throws NoSuchFeatureFlagExistsException {
        log.info("Fetching feature featureFlagName:{} companyId:{}",featureName,companyId);
        Optional<FeatureFlag> featureFlagOptional = featureFlagRepo.findByCompanyIdAndName(companyId,featureName);
        if(featureFlagOptional.isEmpty()){
            log.warn("Feature deletion failed featureFlagName:{} companyId:{}",featureName,companyId);
            throw new NoSuchFeatureFlagExistsException("No feature flag with name "+featureName+" exists in your company");
        }
        featureFlagRepo.delete(featureFlagOptional.get());
        return featureFlagOptional.get();
    }
}
