package com.wallet.controller;

import com.wallet.repo.UserRepo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepo users;
    public AdminController(UserRepo users) { this.users = users; }

    @GetMapping("/user-count")
    public UserCountResponse getUserCount() { return new UserCountResponse(users.count()); }

    public record UserCountResponse(long count) { }
}
