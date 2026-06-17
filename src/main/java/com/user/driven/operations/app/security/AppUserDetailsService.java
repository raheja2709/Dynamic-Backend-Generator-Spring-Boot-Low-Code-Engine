package com.user.driven.operations.app.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.user.driven.operations.app.core.model.AppUser;
import com.user.driven.operations.app.core.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Custom UserDetailsService implementation that loads user details from the database.
 * Used by Spring Security for authentication and authorization decisions.
 *
 * @author Jatin Raheja
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    /**
     * Loads a user by their email address for Spring Security authentication.
     *
     * @param email the email address (used as username)
     * @return UserDetails containing the user's credentials and authorities
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                appUser.getEmail(),
                appUser.getPasswordHash(),
                appUser.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                !appUser.isLocked(), // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + appUser.getRole()))
        );
    }

}
