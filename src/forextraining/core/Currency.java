/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forextraining.core;

/**
 *
 * @author Reimi
 */
public interface Currency {
    
    String getName();
    
    String getAlias();
    
    String getFullName();
    
    String getSymbol();
    
    boolean isExchangable();
}
