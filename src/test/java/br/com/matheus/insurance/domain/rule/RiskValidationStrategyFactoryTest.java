package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.RiskClassification;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskValidationStrategyFactoryTest {

    @Test
    void should_return_regular_strategy() {
        RiskValidationStrategyFactory factory = new RiskValidationStrategyFactory(
                List.of(
                        new RegularRiskValidationStrategy(),
                        new HighRiskValidationStrategy(),
                        new PreferredRiskValidationStrategy(),
                        new NoInformationRiskValidationStrategy()
                )
        );

        RiskValidationStrategy strategy = factory.get(RiskClassification.REGULAR);

        assertInstanceOf(RegularRiskValidationStrategy.class, strategy);
    }

    @Test
    void should_throw_when_strategy_not_found() {
        RiskValidationStrategyFactory factory = new RiskValidationStrategyFactory(List.of());

        assertThrows(IllegalArgumentException.class, () -> factory.get(RiskClassification.REGULAR));
    }
}