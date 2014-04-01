/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core.order;

import forextraining.core.CurrencyExchange;
import forextraining.core.Price;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author Reimi
 */
public interface MarketOrder {

    CurrencyExchange getExchangeType();
    
    BigDecimal getOrderAmount();
    
    Price getDealPrice();
    
    default String getTotalValue() {
        if(isAskOrder()) {
            return getExchangeType().getQuoteCurrency().getSymbol() + getOrderAmount().multiply(getDealPrice().getValue()).toString();    
        }
        return getExchangeType().getBaseCurrency().getSymbol() + BigDecimal.ONE.divide(getDealPrice().getValue(), 5, RoundingMode.HALF_UP).multiply(getOrderAmount()).toString();
    }
    
    boolean isAskOrder();
    
    default BigDecimal compareNewPriceValue(BigDecimal priceValue) {
        if(isAskOrder()) {
        
            BigDecimal pipSpread = priceValue.subtract(getDealPrice().getValue()).multiply(new BigDecimal(10000));
            return getDealPrice().getPipValue().multiply(pipSpread).multiply(getOrderAmount());
        } else {
            BigDecimal pipSpread = priceValue.subtract(getDealPrice().getValue()).multiply(new BigDecimal(10000));
            return getDealPrice().getPipValue().divide(getDealPrice().getValue(), 2, RoundingMode.HALF_UP).multiply(pipSpread).multiply(getOrderAmount());
        }
    }
    
}
