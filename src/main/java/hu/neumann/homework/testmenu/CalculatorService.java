package hu.neumann.homework.testmenu;

import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public double calculate(double a, double b, String operation) {
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("Operation is required.");
        }
        return switch (operation.trim().toLowerCase()) {
            case "add", "+" -> a + b;
            case "subtract", "-" -> a - b;
            case "multiply", "*" -> a * b;
            case "divide", "/" -> {
                if (b == 0) {
                    throw new IllegalArgumentException("Division by zero is not allowed.");
                }
                yield a / b;
            }
            default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
        };
    }
}
