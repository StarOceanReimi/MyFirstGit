/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core;

import static forextraining.core.ExchangableCurrency.USD;
import java.math.BigDecimal;

/**
 *
 * @author Reimi
 */
class ExchangePrice implements Price {

    private final Currency base;
    
    private final BigDecimal basePip;

    private BigDecimal value;
    
    public ExchangePrice(Currency base, BigDecimal basePip, BigDecimal value) {
        this.base = base;
        this.basePip = basePip;
        this.value = value;
    }
    
    @Override
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public BigDecimal getPipValue() {
        if(!base.equals(USD)) {
            return basePip;
        } else {
            return (basePip.divide(value));
        }
    }
    
}
