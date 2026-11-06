package hu.neumann.homework.testmenu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorServiceTest {

    private CalculatorService service;

    @BeforeEach
    void setUp() {
        service = new CalculatorService();
    }

    @Test
    void addReturnsSum() {
        assertEquals(15.0, service.calculate(10, 5, "add"));
    }

    @Test
    void subtractReturnsDifference() {
        assertEquals(5.0, service.calculate(10, 5, "subtract"));
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(50.0, service.calculate(10, 5, "multiply"));
    }

    @Test
    void divideReturnsQuotient() {
        assertEquals(2.0, service.calculate(10, 5, "divide"));
    }

    @Test
    void divideByZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(10, 0, "divide"));
    }

    @Test
    void unsupportedOperationThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(1, 2, "power"));
    }

    @Test
    void blankOperationThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(1, 2, " "));
    }
}
