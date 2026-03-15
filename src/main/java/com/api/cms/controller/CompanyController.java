package com.api.cms.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.api.cms.dto.company.CreateCompanyRequestDTO;
import com.api.cms.dto.company.CreateCompanyResponseDTO;
import com.api.cms.entity.Company;
import com.api.cms.entity.User;
import com.api.cms.exception.AdminNotFoundException;
import com.api.cms.exception.CompanyAlreadyExistsException;
import com.api.cms.exception.CompanyDoesNotExistException;
import com.api.cms.mapper.CompanyMapper;
import com.api.cms.service.CompanyService;
import com.api.cms.service.UserService;
import com.api.cms.util.SecurityUtil;

import jakarta.validation.Valid;




@RestController
public class CompanyController {
    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);
    private CompanyService companyService;
    private UserService userService;
    public CompanyController(CompanyService companyService, UserService userService) {
        this.companyService = companyService;
        this.userService = userService;
    }
    @PostMapping("/companies")
    public ResponseEntity<CreateCompanyResponseDTO> createCompany(@Valid @RequestBody CreateCompanyRequestDTO requestDTO) throws AdminNotFoundException, CompanyAlreadyExistsException {
        log.info("Create company request recieved  company:{}",requestDTO.getCompanyName());
        Company createdCompany = companyService.createCompany(requestDTO);
        User adminUser = userService.getAdminByCompanyId(createdCompany.getCompanyId());
        CreateCompanyResponseDTO responseDTO = CompanyMapper.toCreateCompanyResponseDTO(createdCompany, adminUser);
        log.info("Company created successfully companyId:{}",responseDTO.getCompanyId());
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
    @GetMapping("/companies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateCompanyResponseDTO> getCompanyDetails() throws AdminNotFoundException, CompanyDoesNotExistException {
        UUID companyId = SecurityUtil.getCompanyId();
        log.info("Get company details request recieved companyId:{}",companyId);
        Company company = companyService.getCompanyById(companyId);
        User adminUser = userService.getAdminByCompanyId(companyId);
        CreateCompanyResponseDTO responseDTO = CompanyMapper.toCreateCompanyResponseDTO(company, adminUser);
        log.info("Company details returned companyId:{}",responseDTO.getCompanyId());
        return ResponseEntity.ok(responseDTO);
    }

}
