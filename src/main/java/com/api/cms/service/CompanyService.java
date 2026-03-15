package com.api.cms.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.cms.dto.AdminInfoDTO;
import com.api.cms.dto.company.CreateCompanyRequestDTO;
import com.api.cms.entity.Company;
import com.api.cms.entity.User;
import com.api.cms.enums.Role;
import com.api.cms.exception.CompanyAlreadyExistsException;
import com.api.cms.exception.CompanyDoesNotExistException;
import com.api.cms.repository.CompanyRepo;
import com.api.cms.repository.UserRepo;
import com.api.cms.util.Util;

@Service
public class CompanyService {
    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);
    private CompanyRepo companyRepo;
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;
    public CompanyService(CompanyRepo companyRepo, UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public Company createCompany(CreateCompanyRequestDTO requestDTO) throws CompanyAlreadyExistsException{
        log.info("Creating company companyName:{}",requestDTO.getCompanyName());
        Optional<Company> existingCompany = companyRepo.findByCompanyName(requestDTO.getCompanyName());
        if(existingCompany.isPresent()) {
            log.warn("Company creating failed companyName:{} reason:already exists");
            throw new CompanyAlreadyExistsException("Company with name " + requestDTO.getCompanyName() + " already exists.");
        }
        Company newCompany = new Company();
        newCompany.setCompanyName(requestDTO.getCompanyName());
        newCompany.setCreatedAt(Instant.now());
        newCompany.setApiKey(Util.generateApiKey());
        Company savedCompany = companyRepo.save(newCompany);
        AdminInfoDTO requestAdmin = requestDTO.getAdmin();
        User admin = new User();
        admin.setCompanyId(savedCompany.getCompanyId());
        admin.setEmail(requestAdmin.getEmail());
        admin.setUsername(requestAdmin.getUsername());
        admin.setRole(Role.ADMIN);
        admin.setPassword(passwordEncoder.encode(requestAdmin.getPassword()));
        userRepo.save(admin);
        log.info("Company created successfully companyId:{}",savedCompany.getCompanyId());
        return savedCompany;
    }
    public Company getCompanyById(UUID companyId) throws CompanyDoesNotExistException {
        log.debug("Fetching company companyId:{}",companyId);
        Optional<Company> optionalCompany = companyRepo.findById(companyId);
        if(optionalCompany.isEmpty()){
            log.warn("Company not found companyId:{}",companyId);
            throw new CompanyDoesNotExistException("Company with id "+companyId+" not found");
        }
        log.debug("Company Fetched Sucessfully companyId:{}",companyId);
        return optionalCompany.get();
    }
}
