/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forextraining.core;

/**
 *
 * @author Reimi
 */
public enum ExchangableCurrency implements Currency {
    USD("USD", "Buck", "United State Dollar", "$"),
    CAD("CAD", "Loonie", "Canadian Dollar", "CAD$"),
    NZD("NZD", "Kiwi", "New Zealand Dollar", "NZD$"),
    AUD("AUD", "Aussie", "Austrilian Dollar", "AUD$"),
    CHF("CHF", "Swissy", "Confederation Helvetica Franc", "SFr"),
    GBP("GBP", "Cable Sterling", "Great Britain Pound", "£"),
    JPY("JPY", "Yen", "Japanese Yen", "¥"),
    EUR("EUR", "Fiber", "Europe Dollar", "€")
    ;

    private final String name;
    private final String alias;
    private final String fullName;
    private final String symbol;
        
    private ExchangableCurrency(String name, String alias, String fullName, String symbol) {
        this.name = name;
        this.alias = alias;
        this.fullName = fullName;
        this.symbol = symbol;
    }
        
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean isExchangable() {
        return true;
    }
}
