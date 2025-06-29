package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.UserResponseDto;
import com.volunteerconnect.backend.dto.UserUpdateDto;

import java.util.List;

public interface UserService { // Make sure this is 'interface'

    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(Long userId);
    UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto);
    void deleteUser(Long userId);
}