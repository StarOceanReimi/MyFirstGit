/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.data;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 *
 * @author Reimi
 */
public interface DataRetriever {
    
    void restartRetriver();
    
    void stopRetrieve();
    
    void retrieve() throws IOException;
    
    ZonedDateTime getRetrieveDateTime();
}
