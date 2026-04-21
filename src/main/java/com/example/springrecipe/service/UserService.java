package com.example.springrecipe.service;

import com.example.springrecipe.dto.UserDTO;
import com.example.springrecipe.exceptions.EmailAlreadyExists;
import com.example.springrecipe.exceptions.UserNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.User;
import com.example.springrecipe.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RecipeMapper mapper;

    public List<UserDTO> getAllUsers() {
        log.debug("Запрос всех пользователей");
        List<UserDTO> result = userRepository.findAll().stream()
                .map(mapper::toUserDTO)
                .toList();
        log.info("Найдено {} пользователей", result.size());
        return result;
    }

    public UserDTO getUserById(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("User not found");
                });
        log.info("Найден пользователь: {} (ID: {})", user.getUserName(), user.getId());
        return mapper.toUserDTO(user);
    }

    public List<UserDTO> searchUsers(String query) {
        log.debug("Поиск пользователей по запросу: {}", query);
        List<UserDTO> result = userRepository.findByUserName(query).stream()
                .map(mapper::toUserDTO)
                .toList();
        log.info("Найдено {} пользователей по запросу '{}'", result.size(), query);
        return result;
    }

    @Transactional
    public UserDTO createUser(UserDTO dto) {
        log.info("Создание нового пользователя: userName='{}', email='{}'", dto.getUserName(), dto.getEmail());

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("Email '{}' уже используется", dto.getEmail());
            throw new EmailAlreadyExists("Email already exists");
        }

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setAvatarUrl(dto.getAvatarUrl());

        user = userRepository.save(user);
        log.info("Пользователь успешно создан: ID={}, userName='{}'", user.getId(), user.getUserName());
        return mapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO dto) {
        log.info("Обновление пользователя: ID={}, userName='{}'", id, dto.getUserName());

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден для обновления", id);
                    return new UserNotFoundException("User not found");
                });

        String oldName = user.getUserName();
        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setAvatarUrl(dto.getAvatarUrl());

        log.debug("Имя пользователя изменено: '{}' -> '{}'", oldName, dto.getUserName());
        user = userRepository.save(user);
        log.info("Пользователь ID={} успешно обновлен", id);

        return mapper.toUserDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.warn("УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ: ID={}", id);

        if (!userRepository.existsById(id)) {
            log.warn("Пользователь с ID {} не найден для удаления", id);
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        log.info("Пользователь ID={} успешно удален", id);
    }
}
