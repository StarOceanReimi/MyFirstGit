/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.data;

import forextraining.core.CurrencyExchange;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Reimi
 */
public class BasicOnadaDataRetriever implements SimplePriceList, DataRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(BasicOnadaDataRetriever.class.getName());
    
    private Map<String, BigDecimal[]> basicData;
    
    private String dataSavePath;
    private static final String DEFAULT_SAVEPATH = "c:/onadaRate";
    
    private static final int CUR_INDEX = 0;
    private static final int BID_INDEX = 1;
    private static final int ASK_INDEX = 2;
    
    private static BasicOnadaDataRetriever instance;
    
    private SimpleOnadaDataRetriever retriver;
    
    private ZonedDateTime retrieveDateTime;
    
    public static BasicOnadaDataRetriever getInstance() {
        if(instance == null)
            instance = new BasicOnadaDataRetriever();
        return instance;
    }
    
    private BasicOnadaDataRetriever() {
        
        basicData = new ConcurrentHashMap<>();
        setDataSavePath(DEFAULT_SAVEPATH);
        try {
            retrieve();
        } catch (IOException ex) {
        }
        restartRetriver();
    }
    
    @Override
    public final void restartRetriver() {
        retriver = new SimpleOnadaDataRetriever();
        retriver.start();
    }
    
    @Override
    public final void stopRetrieve() {
        retriver.stop();
    }
    
    private void parse(String line) {
        String[] pairData = line.split("=");
        
        try {
            CurrencyExchange ce = CurrencyExchange.ofName(pairData[CUR_INDEX]);
            BigDecimal bid = new BigDecimal(pairData[BID_INDEX]);
            BigDecimal ask = new BigDecimal(pairData[ASK_INDEX]);
            basicData.put(ce.getName(), new BigDecimal[]{bid, ask});
        } catch (RuntimeException ex) {
        }
    }
    
    @Override
    public final void retrieve() throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(dataSavePath));
        String line;
        boolean first = true;
        while((line = fileReader.readLine()) != null) {
            if(first) {
                Instant ins = Instant.ofEpochMilli(Long.parseLong(line));
                retrieveDateTime = ZonedDateTime.ofInstant(ins, ZoneId.of("Z"));
                first = false;
            }
            parse(line);
        }
    }

    public final void setDataSavePath(String dataSavePath) {
        this.dataSavePath = dataSavePath;
        File ratePath = new File(dataSavePath);
        if(!ratePath.exists()) {
            try {
                ratePath.createNewFile();
            } catch (IOException ex) {
                LOG.error("create rate save file error! on path[{}]", dataSavePath);
            }
        }
    }    

    @Override
    public ZonedDateTime getRetrieveDateTime() {
        if(retrieveDateTime == null) {
            return ZonedDateTime.now(ZoneId.of("Z"));
        }
        return retrieveDateTime;
    }
    
    @Override
    public BigDecimal getAskPriceValue(CurrencyExchange exchangeType) {
        BigDecimal[] rates = basicData.get(exchangeType.getName());
        if(rates == null) {
            return new BigDecimal(0);
        }
        return rates[1];
    }

    @Override
    public BigDecimal getBidPriceValue(CurrencyExchange exchangeType) {
        BigDecimal[] rates = basicData.get(exchangeType.getName());
        if(rates == null) {
            return new BigDecimal(0);
        }
        return rates[0];
    }
    
    
    public static void main(String[] args) throws ScriptException, InterruptedException, IOException {
        BasicOnadaDataRetriever re = new BasicOnadaDataRetriever();
        
        System.out.println(re.getAskPriceValue(CurrencyExchange.GBP_USD));
        Thread.sleep(5000);
        System.out.println(re.getAskPriceValue(CurrencyExchange.GBP_USD));
        Thread.sleep(5000);
        System.out.println(re.getAskPriceValue(CurrencyExchange.GBP_USD));
        re.stopRetrieve();
    }

    private class SimpleOnadaDataRetriever implements Runnable {

        private static final String ONADA_DATA_URL = "http://www.oanda.com/lfr/rates_lrrr?tstamp=%d&invert=1";
        
        private String rc4DecryptScript = "var key=\"aaf6cb4f0ced8a211c2728328597268509ade33040233a11af\";function hexEncode(e){var d=\"0123456789abcdef\",b=[],a=[],c;for(c=0;c<256;c++){b[c]=d.charAt(c>>4)+d.charAt(c&15)}for(c=0;c<e.length;c++){a[c]=b[e.charCodeAt(c)]}return a.join(\"\")}function hexDecode(f){var e=\"0123456789abcdef\",b=[],a=[],c=0,d;for(d=0;d<256;d++){b[e.charAt(d>>4)+e.charAt(d&15)]=String.fromCharCode(d)}if(!f.match(/^[a-f0-9]*$/i)){return false}if(f.length%2){f=\"0\"+f}for(d=0;d<f.length;d+=2){a[c++]=b[f.substr(d,2)]}return a.join(\"\")}function rc4(e,g){var b=0,d,a,h,f=[],c=[];for(d=0;d<256;d++){f[d]=d}for(d=0;d<256;d++){b=(b+f[d]+e.charCodeAt(d%e.length))%256;a=f[d];f[d]=f[b];f[b]=a}d=0;b=0;for(h=0;h<g.length;h++){d=(d+1)%256;b=(b+f[d])%256;a=f[d];f[d]=f[b];f[b]=a;c[c.length]=String.fromCharCode(g.charCodeAt(h)^f[(f[d]+f[b])%256])}return c.join(\"\")}function rc4decrypt(a){return rc4(key,hexDecode(a))};";
        private ScriptEngineManager sem;
        private ScriptEngine se;
        
        private volatile Thread retrieveThread;
        
        public SimpleOnadaDataRetriever() {

            sem = new ScriptEngineManager(BasicOnadaDataRetriever.class.getClassLoader());
            se = sem.getEngineByExtension("js");
            try {
                se.eval(rc4DecryptScript);
            } catch (ScriptException ex) {
            }
        }
        
        public void start() {
            retrieveThread = new Thread(this, "Thread[Onada Rate Retriever]");
            retrieveThread.start();
        }

        private String decryptString(String encryptString) {
            try {
                return String.valueOf(((Invocable)se).invokeFunction("rc4decrypt", encryptString));
            } catch (ScriptException | NoSuchMethodException ex) {
                LOG.error("unexpected error! something wrong with sciprt.");
            }
            return null;
        }
        
        private void addProperty(URLConnection conn) {
            conn.addRequestProperty("Accept", "*/*");
            conn.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
            conn.addRequestProperty("Connection", "keep-alive");
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");
            conn.addRequestProperty("Referer", "http://www.oanda.com/currency/live-exchange-rates/");
            conn.addRequestProperty("Host", "www.oanda.com");
        }
        
        private InputStream getUrlData() {
            try {
                String onada_url = String.format(ONADA_DATA_URL, 
                        Clock.system(ZoneId.of("Etc/GMT-4")).instant().toEpochMilli());
                URL url = new URL(onada_url);
                URLConnection conn = url.openConnection();
                addProperty(conn);
                conn.setDoOutput(true);
                conn.connect();
                return conn.getInputStream();
            } catch (IOException ex) {
            }
            return null;
        }
        
        private void ioflow(Reader reader, Writer writer) {
            try {
                char[] charBuffer = new char[1024];
                int len;
                while((len = reader.read(charBuffer)) != -1) {
                    writer.write(charBuffer, 0, len);
                }
            } catch (IOException ex) {
                LOG.error("io flow error!", ex);
            }
        }
        
        public synchronized void stop() {
            retrieveThread = null;
        }
        
        @Override
        public void run() {
            
            Thread thisThread = Thread.currentThread();
            
            while(thisThread == retrieveThread) {
                
                InputStream dataStream = null;
                try {
                    dataStream = getUrlData();
                } catch(Exception ex) {
                    try {
                        LOG.info("onada date retrieve failed! due to the network problem! try again later");
                        Thread.sleep(1000);
                        continue;
                    } catch (InterruptedException ex1) {
                    }
                }
               
                if(dataStream == null) {
                    LOG.info("onada date retrieve failed! due to data stream was null! try again later.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                    continue;
                }
                
                StringWriter writer = new StringWriter();
                ioflow(new InputStreamReader(dataStream), writer);
                StringReader reader = new StringReader(decryptString(writer.toString()));
                try {
                    FileWriter fileWriter = new FileWriter(dataSavePath, false);
                    String millis = String.valueOf(Clock.systemUTC().instant().toEpochMilli());
                    fileWriter.write(millis);
                    fileWriter.write(System.getProperty("line.separator"));
                    ioflow(reader, fileWriter);
                    fileWriter.flush();
                } catch (IOException ex) {
                   LOG.error("rate file write error!");
                }
                try {
                    retrieve();
                } catch (IOException ex) {
                    LOG.error("data update error!");
                }
                try {
                    dataStream.close();
                } catch (IOException ex) {
                    LOG.error("data stream close error!");
                }
            }
        }
    }
}
