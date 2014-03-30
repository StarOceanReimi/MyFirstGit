/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core;

import java.time.LocalDateTime;

/**
 *
 * @author Reimi
 */
public interface MarketDateTime {
    
    LocalDateTime getStartTimeInWeek();
    
    LocalDateTime getEndTimeInWeek();
    
    
}
