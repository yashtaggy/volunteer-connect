package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.UserResponseDto;
import com.volunteerconnect.backend.dto.UserUpdateDto;
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service // Marks this class as a Spring Service component
public class UserServiceImpl implements UserService { // Implements the new UserService interface

    private final UserRepository userRepository;

    @Autowired // Spring will inject UserRepository here
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Helper method to convert User entity to UserResponseDto
    private UserResponseDto convertToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + userId + " not found."));
        return convertToDto(user);
    }

    @Override
    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + userId + " not found."));

        user.setFirstName(userUpdateDto.getFirstName());
        user.setLastName(userUpdateDto.getLastName());
        user.setEmail(userUpdateDto.getEmail());

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + userId + " not found.");
        }
        userRepository.deleteById(userId);
    }
}