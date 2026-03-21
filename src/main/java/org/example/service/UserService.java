package org.example.service;

import org.example.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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

    public UserService(ExternalUserClient externalUserClient,
                       @Qualifier("ioTaskExecutor") Executor ioExecutor){
        this.externalUserClient = externalUserClient;
        this.ioExecutor = ioExecutor;
    }
    public CompletableFuture<User> findUserAsync(Long id) {
        return CompletableFuture.supplyAsync(()->
            externalUserClient.fetchUser(id),
                ioExecutor
                );
    }
}
