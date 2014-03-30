/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.data;

import forextraining.core.CurrencyExchange;
import java.math.BigDecimal;

/**
 *
 * @author Reimi
 */
public interface PriceList {
    
    BigDecimal getAskPriceValue(CurrencyExchange exchangeType);
    
    BigDecimal getBidPriceValue(CurrencyExchange exchangeType);
}
