package com.example.springrecipe.service;

import com.example.springrecipe.dto.UserDTO;
import com.example.springrecipe.exceptions.EmailAlreadyExists;
import com.example.springrecipe.exceptions.UserNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.User;
import com.example.springrecipe.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeMapper mapper;

    @InjectMocks
    private UserService userService;

    private UserDTO dto() {
        UserDTO dto = new UserDTO();
        dto.setUserName("tiki");
        dto.setEmail("tiikitak@mail.com");
        return dto;
    }

    @Test
    void getAllUsers_success() {
        User user = new User();
        user.setUserName("tiki");

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(mapper.toUserDTO(any())).thenReturn(dto());

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_success() {
        User user = new User();
        user.setId(1L);
        user.setUserName("tiki");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toUserDTO(any())).thenReturn(dto());

        UserDTO result = userService.getUserById(1L);

        assertEquals("tiki", result.getUserName());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void searchUsers_success() {
        when(userRepository.findByUserName("tiki"))
                .thenReturn(List.of(new User()));
        when(mapper.toUserDTO(any())).thenReturn(dto());

        List<UserDTO> result = userService.searchUsers("tiki");

        assertEquals(1, result.size());
    }

    @Test
    void createUser_success() {
        UserDTO dto = dto();

        when(userRepository.findByEmail("tiikitak@mail.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toUserDTO(any())).thenReturn(dto);

        UserDTO result = userService.createUser(dto);

        assertEquals("tiki", result.getUserName());
        verify(userRepository).save(any());
    }

    @Test
    void createUser_emailExists() {
        UserDTO dto = dto();

        when(userRepository.findByEmail("tiikitak@mail.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExists.class, () -> userService.createUser(dto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_success() {
        User user = new User();
        user.setId(1L);
        user.setUserName("old");

        UserDTO dto = dto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toUserDTO(any())).thenReturn(dto);

        UserDTO result = userService.updateUser(1L, dto);

        assertEquals("tiki", result.getUserName());
    }

    @Test
    void updateUser_notFound() {
        UserDTO dto = dto();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, dto));
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, never()).deleteById(any());
    }
}
