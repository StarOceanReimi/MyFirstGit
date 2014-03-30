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
}
