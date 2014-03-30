/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forextraining.core.order;

import forextraining.core.CurrencyExchange;
import forextraining.core.Price;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author Reimi
 */
public abstract class AbstractOrder implements LimitOrder {

    private LocalDateTime expireTime;

    private Price expectedPrice;

    private CurrencyExchange exchangeType;

    private  BigDecimal orderAmount;

    private Price dealPrice;

    private boolean isAskOrder;

        public AbstractOrder(CurrencyExchange exchangeType, BigDecimal orderAmount,
            Price dealPrice, boolean isAskOrder) {

        this.exchangeType = exchangeType;
        this.orderAmount = orderAmount;
        this.dealPrice = dealPrice;
        this.isAskOrder = isAskOrder;
    }
    
    public AbstractOrder(Price expectedPrice,
            CurrencyExchange exchangeType, BigDecimal orderAmount,
            Price dealPrice, boolean isAskOrder) {
        
        this.expectedPrice = expectedPrice;
        this.exchangeType = exchangeType;
        this.orderAmount = orderAmount;
        this.dealPrice = dealPrice;
        this.isAskOrder = isAskOrder;
    }
    
    public AbstractOrder(LocalDateTime expireTime, Price expectedPrice,
            CurrencyExchange exchangeType, BigDecimal orderAmount,
            Price dealPrice, boolean isAskOrder) {
        
        this.expireTime = expireTime;
        this.expectedPrice = expectedPrice;
        this.exchangeType = exchangeType;
        this.orderAmount = orderAmount;
        this.dealPrice = dealPrice;
        this.isAskOrder = isAskOrder;
    }

    @Override
    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    @Override
    public Price getExpectedPrice() {
        return expectedPrice;
    }

    @Override
    public CurrencyExchange getExchangeType() {
        return exchangeType;
    }

    @Override
    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    @Override
    public Price getDealPrice() {
        return dealPrice;
    }

    @Override
    public boolean isAskOrder() {
        return isAskOrder;
    }
}
