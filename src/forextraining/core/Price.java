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
 */
public interface Price {
    
    /**
     * current bid/ask price
     * @return 
     */
    BigDecimal getValue();
    
    /**
     * To see if 1 point change how much USD will be affected
     * @return 
     */
    BigDecimal getPipValue();
    
    default BigDecimal down(double point) {
        return getValue().subtract(new BigDecimal(String.valueOf(point/10000)));
    }
    
    default BigDecimal up(double point) {
        return getValue().add(new BigDecimal(String.valueOf(point/10000)));
    }
    
    /**
     * To see the difference between two price, positive value means current price
     * is greater than other price, negative value means opposite
     * @param otherPrice
     * @return difference
     */
    default double diff(Price otherPrice) {
        return getValue().subtract(otherPrice.getValue()).multiply(new BigDecimal(10000)).doubleValue();
    }
    
}
