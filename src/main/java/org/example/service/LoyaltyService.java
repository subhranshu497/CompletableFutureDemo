package org.example.service;

import org.example.model.User;
import org.springframework.stereotype.Service;

@Service
public class LoyaltyService {
    public int getPoints(Long userId) {
        simulateLatency(60);
        return 3500;
    }

    public String getRecommendation(User user) {
        simulateLatency(40);
        return switch (user.tier()) {
            case "VIP" -> "Exclusive VIP deals available!";
            case "PREMIUM" -> "Upgrade to VIP for extra benefits";
            default -> "Join Premium for 2x points";
        };
    }

    private void simulateLatency(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
