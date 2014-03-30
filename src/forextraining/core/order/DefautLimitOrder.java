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
public class DefautLimitOrder extends AbstractOrder {

    public DefautLimitOrder(LocalDateTime expireTime, Price expectedPrice, CurrencyExchange exchangeType, BigDecimal orderAmount, Price dealPrice, boolean isAskOrder) {
        super(expireTime, expectedPrice, exchangeType, orderAmount, dealPrice, isAskOrder);
    }
    
}
