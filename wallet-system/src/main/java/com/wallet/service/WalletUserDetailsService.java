package com.wallet.service;

import com.wallet.repo.UserRepo;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletUserDetailsService implements UserDetailsService {
    private final UserRepo users;

    public WalletUserDetailsService(UserRepo users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var user = users.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }
}
