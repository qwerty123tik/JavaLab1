package com.example.springrecipe.service;

import com.example.springrecipe.dto.UserDTO;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.User;
import com.example.springrecipe.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RecipeMapper mapper;

   // @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toUserDTO)
                .collect(Collectors.toList());
    }

    //@Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapper.toUserDTO(user);
    }

   // @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String query) {
        return userRepository.findByUserName(query).stream()
                .map(mapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(UserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());

        user = userRepository.save(user);
        return mapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());

        user = userRepository.save(user);
        return mapper.toUserDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}
