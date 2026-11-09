package com.sagar.security.user;

import com.sagar.exception.ResourceNotFoundException;
import com.sagar.user.model.User;
import com.sagar.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads an application user using their email address.
     *
     * <p>
     * Spring Security uses this method during username/password
     * authentication and whenever the JWT filter needs to reconstruct
     * the authenticated user's security context.
     * </p>
     *
     * @param username user's email address
     * @return Spring Security user details
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + username
                        )
                );

        return buildUserDetails(user);
    }

    /**
     * Converts the application's User entity into Spring Security's
     * UserDetails representation.
     *
     * @param user application user
     * @return Spring Security user details
     */
    private UserDetails buildUserDetails(User user) {

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(
                        Collections.singleton(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                )
                        )
                )
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build();
    }

    public User loadApplicationUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );
    }
}