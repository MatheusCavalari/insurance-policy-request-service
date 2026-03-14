package br.com.matheus.insurance.domain.rule;

public record ValidationDecision(
        boolean approved,
        String reason
) {
    public static ValidationDecision accept() {
        return new ValidationDecision(true, "approved");
    }

    public static ValidationDecision reject(String reason) {
        return new ValidationDecision(false, reason);
    }
}