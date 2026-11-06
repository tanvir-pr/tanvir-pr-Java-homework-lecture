package hu.neumann.homework.soap;

import java.time.LocalDate;

public class ExchangeRate {

    private LocalDate date;
    private String currency;
    private double unit;
    private double value;

    public ExchangeRate() {
    }

    public ExchangeRate(LocalDate date, String currency, double unit, double value) {
        this.date = date;
        this.currency = currency;
        this.unit = unit;
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getUnit() {
        return unit;
    }

    public void setUnit(double unit) {
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
