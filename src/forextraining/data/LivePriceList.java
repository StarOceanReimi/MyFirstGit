/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.data;

import forextraining.core.CurrencyExchange;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 *
 * @author Reimi
 */
public interface LivePriceList extends SimplePriceList {

    String getTick(CurrencyExchange exchangeType);
    
    BigDecimal getChangeValue(CurrencyExchange exchangeType);
    
    String getChangePercentValue(CurrencyExchange exchangeType);
    
    ZonedDateTime getPriceDateTime(CurrencyExchange exchangeType);
    
    BigDecimal getOtherValue(CurrencyExchange exchangeType);
}
