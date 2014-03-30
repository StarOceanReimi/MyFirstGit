/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core.order;

import forextraining.core.CurrencyExchange;
import forextraining.core.Price;
import java.math.BigDecimal;

/**
 *
 * @author Reimi
 */
public class DefaultOrder extends AbstractOrder {

    public DefaultOrder(CurrencyExchange exchangeType, BigDecimal orderAmount, Price dealPrice, boolean isAskOrder) {
        super(exchangeType, orderAmount, dealPrice, isAskOrder);
    }
}
