/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core.order;

import forextraining.core.Price;

/**
 *
 * @author Reimi
 */
public interface StopLossOrder extends MarketOrder {
    
    Price getExpectedPrice();
}
