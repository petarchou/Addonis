package com.final_project.addonis.services;

import com.final_project.addonis.repositories.contracts.UserRepository;
import com.final_project.addonis.services.contracts.MyUserDetailsService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsServiceImpl implements MyUserDetailsService {

    public static final String USER_NOT_FOUND_MESSAGE = "User with username %s does not exist";

    private final UserService service;

    @Autowired
    public MyUserDetailsServiceImpl(UserService service) {
        this.service = service;
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return service.getByUsername(username);
        }
        catch (EntityNotFoundException e) {
            throw new UsernameNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, username));
        }
    }

}
