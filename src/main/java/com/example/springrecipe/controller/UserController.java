package com.example.springrecipe.controller;

import com.example.springrecipe.dto.UserDTO;
import com.example.springrecipe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(name = "Пользователи", description = "Управление пользователями (CRUD)")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Успешно получен список пользователей"),
                           @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                                   content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе по его идентификатору"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Пользователь найден"),
                           @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                                   content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById( @Parameter(description = "ID пользователя",
            required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Поиск пользователей",
            description = "Поиск пользователей по имени (частичное совпадение)"
    )
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers( @Parameter(description = "Поисковый запрос (имя пользователя)",
            required = true, example = "TikiTaka")
                                                          @RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    @Operation(
            summary = "Создать нового пользователя",
            description = "Создает нового пользователя. Имя и email должны быть уникальными."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
                           @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content),
                           @ApiResponse(responseCode = "409", description = "Email уже используется",
                                   content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        return new ResponseEntity<>(userService.createUser(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Обновить пользователя",
            description = "Обновляет существующего пользователя по его ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
                           @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                                   content = @Content),
                           @ApiResponse(responseCode = "409", description = "Email уже используется",
                                   content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser( @Parameter(description = "ID пользователя",
            required = true, example = "1") @PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по его ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
                           @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                                   content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser( @Parameter(description = "ID пользователя",
            required = true, example = "1") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
