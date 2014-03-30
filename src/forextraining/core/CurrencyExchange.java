/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forextraining.core;

import static forextraining.core.ExchangableCurrency.*;
import forextraining.core.order.DefaultOrder;
import forextraining.core.order.DefaultStopLossOrder;
import forextraining.core.order.DefautLimitOrder;
import forextraining.core.order.LimitOrder;
import forextraining.core.order.MarketOrder;
import forextraining.core.order.OrderFactory;
import forextraining.core.order.StopLossOrder;
import forextraining.data.BasicOnadaDataRetriever;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author Reimi
 */
public enum CurrencyExchange implements Exchange<ExchangableCurrency, ExchangableCurrency>, OrderFactory {

    GBP_USD(GBP, USD, "0.0001"),
    EUR_USD(EUR, USD, "0.0001"),
    AUD_USD(AUD, USD, "0.0001"),
    NZD_USD(NZD, USD, "0.0001"),
    USD_JPY(USD, JPY, "0.01"),
    USD_CHF(USD, CHF, "0.0001"),
    USD_CAD(USD, CAD, "0.0001");

    private final ExchangableCurrency base;

    private final ExchangableCurrency quote;

    private final BigDecimal basePip;

    private CurrencyExchange(ExchangableCurrency base, ExchangableCurrency quote, String basePip) {
        this.base = base;
        this.quote = quote;
        this.basePip = new BigDecimal(basePip);
    }

    @Override
    public ExchangableCurrency getBaseCurrency() {
        return base;
    }

    @Override
    public ExchangableCurrency getQuoteCurrency() {
        return quote;
    }

    @Override
    public BigDecimal getBasePip() {
        return basePip;
    }
    
    public static CurrencyExchange ofSimpleName(String name) {
        switch (name.toUpperCase()) {
            case "GBP":
                return GBP_USD;
            case "EUR":
                return EUR_USD;
            case "NZD":
                return NZD_USD;
            case "AUD":
                return AUD_USD;
            case "JPY":
                return USD_JPY;
            case "CHF":
                return USD_CHF;
            case "CAD":
                return USD_CAD;
        }
        throw new IllegalArgumentException("Unknow simple name of currency exchange in this system");
    }
    
    public static CurrencyExchange ofName(String name) {
        for(CurrencyExchange ce : values()) {
            if(ce.getName().equalsIgnoreCase(name)) {
                return ce;
            }
        }
        throw new IllegalArgumentException("Unknow name of currency exchange in this system");
    }
    
    public BigDecimal getAskBidSpread() {
        return getAskPrice().getValue().subtract(getBidPrice().getValue());
    }
    
    public static void main(String[] args) {
        
        System.out.println(CurrencyExchange.ofName("USD/CAD").getBasePip());
    }

    @Override
    public MarketOrder createAskMarketOrder(BigDecimal amount) {
        return new DefaultOrder(this, amount, getAskPrice(), true);
    }

    @Override
    public MarketOrder createBidMarketOrder(BigDecimal amount) {
        return new DefaultOrder(this, amount, getBidPrice(), false);
    }

    @Override
    public StopLossOrder createAskStopLossOrder(BigDecimal stopPrice, BigDecimal amount) {
        return new DefaultStopLossOrder(new ExchangePrice(base, basePip, stopPrice), this, amount, getAskPrice(), true);
    }

    @Override
    public StopLossOrder createBidStopLossOrder(BigDecimal stopPrice, BigDecimal amount) {
        return new DefaultStopLossOrder(new ExchangePrice(base, basePip, stopPrice), this, amount, getBidPrice(), false);
    }

    @Override
    public LimitOrder createAskLimitOrder(BigDecimal expectedPrice, BigDecimal amount, LocalDateTime expireTime) {
        return new DefautLimitOrder(expireTime, new ExchangePrice(base, basePip, expectedPrice), this, amount, getAskPrice(), true);
    }

    @Override
    public LimitOrder createBidLimitOrder(BigDecimal expectedPrice, BigDecimal amount, LocalDateTime expireTime) {
        return new DefautLimitOrder(expireTime, new ExchangePrice(base, basePip, expectedPrice), this, amount, getBidPrice(), false);
    }
    
    public Price getAskPrice() {
        return new ExchangePrice(base, basePip, BasicOnadaDataRetriever.getInstance().getAskPriceValue(this));
    }
    
    public Price getBidPrice() {
        return new ExchangePrice(base, basePip, BasicOnadaDataRetriever.getInstance().getBidPriceValue(this));
    }
    
}
