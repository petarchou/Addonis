package com.final_project.addonis.controllers.rest;

import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.CreateUserDto;
import com.final_project.addonis.models.dtos.UpdateUserDto;
import com.final_project.addonis.models.dtos.UserDto;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.mappers.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final UserService service;
    private final UserMapper mapper;

    public UserRestController(UserService service, UserMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<UserDto> getAll() {
        return service.getAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable int id) {
        try {
            User user = service.getById(id);
            return mapper.toDto(user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public UserDto create(@RequestBody CreateUserDto createUserDto) {
        if (createUserDto.getPassword().equals(createUserDto.getConfirmPassword())) {
            try {
                User user = mapper.fromDto(createUserDto);
                user = service.create(user);
                return mapper.toDto(user);
            } catch (DuplicateEntityException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "password not match");
    }

    @PutMapping("/{id}")
    public UserDto update(@RequestBody UpdateUserDto updateUserDto,
                          @PathVariable int id) {
        try {
            User user = service.getById(id);
            user = mapper.fromUpdateDto(updateUserDto, user);
            user = service.update(user);
            return mapper.toDto(user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public UserDto delete(@PathVariable int id) {
        try {
            User user = service.getById(id);
            user = service.delete(user);
            return mapper.toDto(user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
