package org.example.exchangeproject;

public class ExchangeRates{
    private Currency baseCurrency;
    private Currency targetCurrency;
    private int id;
    private double rate;

    public ExchangeRates(int id,Currency baseCurrency,Currency targetCurrency,double rate){
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency =targetCurrency;
        this.rate = rate;
    }

}
