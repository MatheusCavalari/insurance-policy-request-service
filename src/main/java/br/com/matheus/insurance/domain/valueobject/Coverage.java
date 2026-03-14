package br.com.matheus.insurance.domain.valueobject;

import java.math.BigDecimal;

public record Coverage(String name, BigDecimal amount) {
}