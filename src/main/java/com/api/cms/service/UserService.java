package com.api.cms.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.cms.dto.CreateUserRequestDTO;
import com.api.cms.dto.UpdateUserRequestDTO;
import com.api.cms.entity.User;
import com.api.cms.enums.Role;
import com.api.cms.exception.AdminNotFoundException;
import com.api.cms.exception.CompanyAlreadyExistsException;
import com.api.cms.exception.UserAlreadyExistsException;
import com.api.cms.exception.UserDoesNotExistException;
import com.api.cms.repository.UserRepo;
import com.api.cms.util.SecurityUtil;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private UserRepo userRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequestDTO requestDTO) throws UserAlreadyExistsException {
        log.info("creating user userEmail:{}",requestDTO.getEmail());
        String email = requestDTO.getEmail();
        UUID companyId = SecurityUtil.getCompanyId();
        if (userRepository.existsByEmailAndCompanyId(email, companyId)) {
            log.warn("User creation failed userEmail:{} reason=user_already_exists",email);
            throw new UserAlreadyExistsException("User with email " + email + " already exists.");
        }
        User newUser = new User();
        newUser.setActive(true);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        newUser.setRole(Role.MEMBER);
        newUser.setUsername(requestDTO.getUsername());
        newUser.setCompanyId(companyId);
        log.info("User created sucessfully userId:{}",newUser.getUserId());
        return userRepository.save(newUser);
    }

    public User getAdminByCompanyId(UUID companyId) throws AdminNotFoundException {
        log.info("Fetching admin companyId:{}",companyId);
        Optional<User> adminUser = userRepository.findByCompanyIdAndRole(companyId, Role.ADMIN);
        if (adminUser.isPresent()) {
            log.info("admin details fetched successfully companyId:{}",companyId);
            return adminUser.get();
        }
        log.warn("admin not found companyId:{}",companyId);
        throw new AdminNotFoundException("Admin user not found for company ID: " + companyId);
    }

    public User updateUser(UUID userId, UpdateUserRequestDTO requestDTO) throws UserDoesNotExistException {
        log.info("updating user userId:{}",userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserDoesNotExistException("User with id : "+userId+" not found"));
        String email = requestDTO.getEmail();
        if(email != null){
            user.setEmail(email);
        }
        String username = requestDTO.getUsername();
        if(username != null){
            user.setUsername(username);
        }
        
        String role = requestDTO.getRole() != null ? requestDTO.getRole().name() : null;
        if(role != null){
            user.setRole(requestDTO.getRole());
        }
        log.info("updated user userId:{}",userId);
        return userRepository.save(user);
    }
}
