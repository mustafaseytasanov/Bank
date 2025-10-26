package com.example.bankcards.service;

import com.example.bankcards.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<UserDTO> getUsers();
}
