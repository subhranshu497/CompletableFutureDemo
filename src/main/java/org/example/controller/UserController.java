package org.example.controller;

import org.example.model.User;
import org.example.model.UserProfile;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    /**
     * ─────────────────────────────────────────────────────────────────────────
     * PATTERN 3: Wrap in ResponseEntity for HTTP control
     * ─────────────────────────────────────────────────────────────────────────
     *
     * Use this when you need to set headers, status codes, etc.
     */
    @GetMapping("/{id}/with-status")
    public CompletableFuture<ResponseEntity<User>> getUserWithStatus(@PathVariable("id") Long id){
        return userService.findUserWithFallbackAsync(id)
                .thenApply(user ->{
                    if(user.id()== 0L){
                        //fallback user returned
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(user);
                });
    }

    /**
     * ─────────────────────────────────────────────────────────────────────────
     * PATTERN 4: Error handling at controller level
     * ─────────────────────────────────────────────────────────────────────────
     *
     * Handle errors and return appropriate HTTP responses.
     */
    @GetMapping("/{id}/recommendation")
    public CompletableFuture<ResponseEntity<String>> getUserRecommendation(@PathVariable("id") Long id){
        return userService.getUserRecommendationAsync(id)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    //log error and return 500
                    return ResponseEntity.internalServerError()
                            .body("Unable to get Recomendation: "+throwable.getMessage());
                });
    }
    /**
     * ─────────────────────────────────────────────────────────────────────────
     * ❌ ANTI-PATTERN: Blocking in controller
     * ─────────────────────────────────────────────────────────────────────────
     *
     * Never do this - it defeats the purpose of async.
     * The request thread is blocked, no benefit from async service.
     */
    // @GetMapping("/{id}/blocking")
    // public User getUserBlocking(@PathVariable Long id) {
    //     // DON'T DO THIS - blocks the request thread
    //     return userService.findUserAsync(id).join();
    // }
}
