/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core;

import java.math.BigDecimal;

/**
 *
 * @author Reimi
 * @param <B>
 * @param <Q>
 */
public interface Exchange<B extends Currency, Q extends Currency> {
    
    B getBaseCurrency();
    
    Q getQuoteCurrency();
    
    BigDecimal getBasePip();
    
    default String getName() {
        return String.format("%s/%s", getBaseCurrency().getName(), getQuoteCurrency().getName());
    }
    
}
