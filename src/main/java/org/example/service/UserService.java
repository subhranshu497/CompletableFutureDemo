package org.example.service;

import org.example.model.User;
import org.example.model.UserProfile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * LEVEL 5 — SPRING BOOT INTEGRATION: Service Layer Best Practices
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * BEST PRACTICES DEMONSTRATED:
 * 1. Inject executor instead of using commonPool
 * 2. Use @Qualifier to select specific executor
 * 3. Return CompletableFuture from service methods (don't block internally)
 * 4. Handle errors at appropriate level
 * 5. Use meaningful method names that indicate async behavior
 *
 * IMPORTANT: This class does NOT use @Async annotation.
 * We manually manage CompletableFuture for better control.
 * See UserServiceWithAsync for the @Async approach and its pitfalls.
 */

@Service
public class UserService {
    private final ExternalUserClient externalUserClient;
    private final Executor ioExecutor;
    private final PreferenceService preferenceService;
    private final LoyaltyService loyaltyService;

    public UserService(ExternalUserClient externalUserClient,
                       PreferenceService preferenceService,
                       LoyaltyService loyaltyService,
                       @Qualifier("ioTaskExecutor") Executor ioExecutor){
        this.externalUserClient = externalUserClient;
        this.preferenceService = preferenceService;
        this.ioExecutor = ioExecutor;
        this.loyaltyService = loyaltyService;
    }
    public CompletableFuture<User> findUserAsync(Long id) {
        return CompletableFuture.supplyAsync(()->
            externalUserClient.fetchUser(id),
                ioExecutor
                );
    }
    /**
     * ─────────────────────────────────────────────────────────────────────────
     * PATTERN 2: Parallel fetching with proper executor
     * ─────────────────────────────────────────────────────────────────────────
     *
     * Launch independent operations in parallel using the same executor.
     * All I/O operations share the I/O executor pool.
     */
    public CompletableFuture<UserProfile> getUserProfileAsync(Long id) {
        //start all three operation in parallel
        CompletableFuture<User> userFuture = findUserAsync(id);
        CompletableFuture<List<String>> prefFuture =
                CompletableFuture.supplyAsync(
                        ()-> preferenceService.getPreferences(id),
                        ioExecutor

                );
        CompletableFuture<Integer> pointsFuture =
                CompletableFuture.supplyAsync(
                        ()->loyaltyService.getPoints(id),
                        ioExecutor
                );

        //now combine the results from all three services
        return CompletableFuture.allOf(userFuture, prefFuture, pointsFuture)
                .thenApplyAsync(ignored ->{
                    User user = userFuture.join();
                    List<String> prefs = prefFuture.join();
                    int points = pointsFuture.join();

                    return UserProfile.of(
                            user,
                            prefs,
                            LocalDateTime.now(),
                            points,
                            calculateRecommendedPlan(user.tier(), points)
                    );
                },ioExecutor);
    }

    private String calculateRecommendedPlan(String currentTier, int loyaltyPoints) {
        if (loyaltyPoints > 5000) return "VIP";
        if (loyaltyPoints > 2000 && "BASIC".equals(currentTier)) return "PREMIUM";
        return currentTier;
    }
}
