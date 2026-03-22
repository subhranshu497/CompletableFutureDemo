package org.example.controller;

import org.example.model.User;
import org.example.model.UserProfile;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * LEVEL 5 — SPRING BOOT INTEGRATION: Controller Layer
     * ═══════════════════════════════════════════════════════════════════════════════
     *
     * Spring MVC (since 4.2) supports CompletableFuture as return type.
     * The request thread is released while waiting for the async result.
     *
     * IMPORTANT:
     * - Servlet container must support async (Tomcat, Jetty, Undertow all do)
     * - Spring Boot enables async by default
     * - Request thread is freed during async operation
     * - Timeout handling is important for production
     *
     * ALTERNATIVE: Spring WebFlux with Mono/Flux for fully reactive approach.
     */
    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ─────────────────────────────────────────────────────────────────────────
     * PATTERN 1: Return CompletableFuture directly
     * ─────────────────────────────────────────────────────────────────────────
     *
     * Spring handles the async result automatically.
     * The Tomcat thread is released while waiting.
     */

    @GetMapping({"/{id}"})
    public CompletableFuture<User> getUser(@PathVariable("id") Long id){
        return userService.findUserAsync(id);
    }

    /**
     * ─────────────────────────────────────────────────────────────────────────
     * PATTERN 2: Transform async result
     * ─────────────────────────────────────────────────────────────────────────
     *
     * Chain transformations before returning.
     */
    @GetMapping("/{id}/profile")
    public CompletableFuture<UserProfile> getUSerProfile(@PathVariable("id") Long id){
        return userService.getUserProfileAsync(id);
    }
}
