/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.data;

import forextraining.core.CurrencyExchange;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Reimi
 */
public class BasicOnadaDataRetriever implements PriceList {
    
    private Map<CurrencyExchange, BigDecimal[]> basicData;

    private static final int CUR_INDEX = 0;
    private static final int BID_INDEX = 1;
    private static final int ASK_INDEX = 2;
    
    private static BasicOnadaDataRetriever instance;
    
    public static BasicOnadaDataRetriever getInstance() {
        if(instance == null)
            instance = new BasicOnadaDataRetriever();
        return instance;
    }
    
    private BasicOnadaDataRetriever() {
        basicData = new ConcurrentHashMap<>();
        try {
            retrieve();
        } catch (IOException ex) {
        }
    }
    
    private void parse(String line) {
        String[] pairData = line.split("=");
        
        try {
            CurrencyExchange ce = CurrencyExchange.ofName(pairData[CUR_INDEX]);
            BigDecimal bid = new BigDecimal(pairData[BID_INDEX]);
            BigDecimal ask = new BigDecimal(pairData[ASK_INDEX]);
            basicData.put(ce, new BigDecimal[]{bid, ask});
        } catch (RuntimeException ex) {
        }
    }
    
    public final void retrieve() throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader("f:/getRate.txt"));
        String line;
        while((line = fileReader.readLine()) != null) {
            parse(line);
        }
    }

    @Override
    public BigDecimal getAskPriceValue(CurrencyExchange exchangeType) {
        return basicData.get(exchangeType)[1];
    }

    @Override
    public BigDecimal getBidPriceValue(CurrencyExchange exchangeType) {
        return basicData.get(exchangeType)[0];
    }
}
