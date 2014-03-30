/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author Reimi
 *
 */
public interface OrderFactory {
    
    public MarketOrder createAskMarketOrder(BigDecimal amount);
    
    public MarketOrder createBidMarketOrder(BigDecimal amount);
    
    public StopLossOrder createAskStopLossOrder(BigDecimal stopPrice, BigDecimal amount);
    
    public StopLossOrder createBidStopLossOrder(BigDecimal stopPrice, BigDecimal amount);
    
    public LimitOrder createAskLimitOrder(BigDecimal  expectedPrice, BigDecimal amount, LocalDateTime expireTime);
    
    public LimitOrder createBidLimitOrder(BigDecimal  expectedPrice, BigDecimal amount, LocalDateTime expireTime);
}
