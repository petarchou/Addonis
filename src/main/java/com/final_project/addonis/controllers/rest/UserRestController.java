package com.final_project.addonis.controllers.rest;

import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.CreateUserDto;
import com.final_project.addonis.models.dtos.PasswordDto;
import com.final_project.addonis.models.dtos.UpdateUserDto;
import com.final_project.addonis.models.dtos.UserDto;
import com.final_project.addonis.services.contracts.EmailService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.config.springsecurity.metaannotations.IsHimselfOrAdmin;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.PasswordNotMatchException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.mappers.InvitedUserMapper;
import com.final_project.addonis.utils.mappers.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final UserService service;
    private final UserMapper mapper;
    private final InvitedUserMapper invitedUserMapper;
    private final EmailService emailService;

    public UserRestController(UserService service,
                              UserMapper mapper,
                              InvitedUserMapper invitedUserMapper,
                              EmailService emailService) {
        this.service = service;
        this.mapper = mapper;
        this.invitedUserMapper = invitedUserMapper;
        this.emailService = emailService;
    }

    @Secured("ROLE_ADMIN")
    @GetMapping
    public List<UserDto> getAll(@RequestParam(value = "search", required = false) Optional<String> keyword,
                                @RequestParam(value = "filterByField", required = false) Optional<String> filterByField,
                                @RequestParam(value = "sortByField", required = false) Optional<String> sortByField,
                                @RequestParam(value = "ascending", required = false) Optional<Boolean> order,
                                @RequestParam(value = "page", required = false) Optional<Integer> page,
                                @RequestParam(value = "size", required = false) Optional<Integer> size) {
        try {

            return service.getAll(keyword, filterByField, sortByField, order, page, size)
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
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
    public UserDto create(@Valid @RequestBody CreateUserDto createUserDto,
                          HttpServletRequest request) {
        try {
            User user = mapper.fromCreateDto(createUserDto);
            user = service.create(user, getSiteUrl(request));
            return mapper.toDto(user);
        } catch (DuplicateEntityException | PasswordNotMatchException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }


    @IsHimselfOrAdmin
    @PutMapping("/{id}")
    public UserDto update(@Valid @RequestBody UpdateUserDto updateUserDto,
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

    @IsHimselfOrAdmin
    @PutMapping("/{id}/changePassword")
    public UserDto changePassword(@PathVariable int id,
                                  @Valid @RequestBody PasswordDto changePasswordDto) {
        try {
            User user = service.getById(id);
            user = mapper.fromPasswordDto(user, changePasswordDto);
            user = service.update(user);
            return mapper.toDto(user);
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @IsHimselfOrAdmin
    @PutMapping("/{id}/photo")
    public UserDto changePhoto(@PathVariable int id,
                               @RequestParam("file") MultipartFile file) {
        try {
            User user = service.getById(id);
            user = mapper.changePhoto(user, file);
            user = service.update(user);
            return mapper.toDto(user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }


    @IsHimselfOrAdmin
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

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/promote/{role}")
    public UserDto addRole(@PathVariable int id, @PathVariable String role) {
        try {
            User promotedUser = service.changeUserRole(id, role, "promote");
            return mapper.toDto(promotedUser);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/demote/{role}")
    public UserDto removeRole(@PathVariable int id, @PathVariable String role) {
        try {
            User promotedUser = service.changeUserRole(id, role, "demote");
            return mapper.toDto(promotedUser);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/block")
    public UserDto blockUser(@PathVariable int id) {
        try {
            User user = service.changeBlockedStatus(id, "block");
            return mapper.toDto(user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/unblock")
    public UserDto unblockUser(@PathVariable int id) {
        try {
            User user = service.changeBlockedStatus(id, "unblock");
            return mapper.toDto(user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }


    @GetMapping("/{id}/invite")
    public String inviteFriend(@RequestParam(name = "email") String toEmail
            , @PathVariable int id, HttpServletRequest request) {
        try {
            User user = service.getById(id);
            InvitedUser invitedUser = invitedUserMapper.fromEmail(toEmail);
            emailService.sendInvitationEmail(user, getSiteUrl(request), invitedUser);
            return "Invitation successful! An invitation email was  sent to " + toEmail;
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    private String getSiteUrl(HttpServletRequest request) {
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }

}
