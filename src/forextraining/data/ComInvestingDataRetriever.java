/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.data;

import forextraining.core.CurrencyExchange;
import forextraining.data.SimpleSoapRequestBuilder.RequestMethod;
import forextraining.data.SimpleSoapRequestBuilder.RequestMethodArgument;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Reimi
 */
public class ComInvestingDataRetriever implements LivePriceList, DataRetriever, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ComInvestingDataRetriever.class);
    
    private final ComInvestingWebServiceInvoker invoker;
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String PAR_SEPARATOR = "=";
    private static final String ATT_SEPARATOR = ";";
    
    public static final int OPEN_INDEX = 0;
    public static final int CLOSE_INDEX = 1;
    public static final int LOW_INDEX = 2;
    public static final int HIGH_INDEX = 3;
    public static final int DATE_INDEX = 4;
    
    public static final DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS");

    private SAXParserFactory saxParserFactory;
    
    private String liveDataSaveDir = "c:/forex_livedata";
    
    private String tempHistorySavePath = "c:/forex_recent_history";
    
    private static final String THREAD_PREFIX = "T-";
    
    private ThreadGroup liveDataRetrieveThreadGroup;
    
    private boolean stopRetriveSignal = false;
    
    private volatile Duration retrieveFrequent = Duration.ofSeconds(5);
    
    private Map<String, Map<String, String>> liveDataMap;
    
    private Map<String, String> lastRetrieveStamp;
    
    public ComInvestingDataRetriever() {
        this.invoker = new ComInvestingWebServiceInvoker();
        this.liveDataMap = new ConcurrentHashMap<>();
        this.lastRetrieveStamp = new ConcurrentHashMap<>();
        this.saxParserFactory = SAXParserFactory.newInstance();
        try {    
            retrieve();
        } catch (IOException ex) {
            LOG.error("retrieving data error!");
        }
    }

    private static final String[][] exchangeType = new String[][]{
        new String[]{"EUR/USD","1"},
        new String[]{"USD/JPY","3"},
        new String[]{"USD/CHF","4"},
        new String[]{"GBP/USD","2"},
        new String[]{"USD/CAD","7"},
        new String[]{"AUD/USD","5"},
        new String[]{"NZD/USD","8"}
    };
    
    public static String getInstrument(String name) {
        for(String[] pairData : exchangeType) {
            if(pairData[0].equalsIgnoreCase(name)) {
                return pairData[1];
            }
        }
        return null;
    }
    
    public static String fromInstrument(String instru) {
        for(String[] pairData : exchangeType) {
            if(pairData[1].equalsIgnoreCase(instru)) {
                return pairData[0];
            }
        }
        return null;
    }
    
    /**
     * Retrieve Recent History Data
     * @param ce The currency type u want retrieve
     * @param maxDataLength total data length u want retrieve
     * @param duration the time length u want retrieve normally would be 10 minutes
     * @return 
     */
    public List<Map<String, String>> retrieveRHToMapList(CurrencyExchange ce, int maxDataLength, Duration duration) {
        StringWriter sw = new StringWriter();
        invoker.retrieveRHData(ce, maxDataLength, duration, sw);
        InputSource input = new InputSource(new StringReader(sw.toString()));
        ComInvestingWebServiceResponseParser parser = new ComInvestingWebServiceResponseParser();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(input, parser);
        } catch (SAXException | IOException ex) {
            LOG.error("retrive recent history data error", ex);
        } catch (ParserConfigurationException ex) {
        }
        return parser.getDataMap();
    }
    
    public void retrieveRHToFile(CurrencyExchange ce, int maxDataLength, Duration duration) {
        requireNonNull(ce);
        File file = makeSureFileExists(tempHistorySavePath);
        try {
            FileWriter fwriter = new FileWriter(file, false);
            StringWriter writer = new StringWriter();
            invoker.retrieveRHData(ce, maxDataLength, duration, writer);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(writer.toString())), 
                    new ComInvestingWebServiceResponseParser(fwriter));
        } catch (IOException ex) {
            LOG.error("retrieveRHToFile error", ex);
        } catch (ParserConfigurationException ex) {
        } catch (SAXException ex) {
            LOG.error("saxparser error", ex);
        }

    }

    public void setRetrieveFrequent(Duration retrieveFrequent) {
        if(retrieveFrequent != null) {
            this.retrieveFrequent = retrieveFrequent;
        }
    }
    
    private File makeSureFileExists(String filePath) {
        return makeSureFileExists(new File(filePath));
    }
    
    private File makeSureFileExists(File file) {
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                LOG.error("error occurs when creating a file");
            }
        }
        return file;
    }
    
    public Object[][] parseRHFileToArrays(int maxDataLength) {
        Object[][] data = new Object[maxDataLength+1][5];

        try (BufferedReader reader = new BufferedReader(new FileReader(tempHistorySavePath))) {
            String line;
            boolean first = true;
            int lineNum = 1;
            while((line = reader.readLine()) != null && lineNum <= maxDataLength) {
                if(first) {
                    data[0][0] = line;
                    first = false;
                } else {
                    parseHistoryLine(line, data, lineNum++);
                }
            }
        } catch (IOException ex) {
            LOG.error("history file parsing error", ex);
        }

        return data;
    }
    
    private void parseHistoryLine(String line, Object[][] data, int lineNum) {
        String[] attList = line.split(ATT_SEPARATOR);
        
        for(String pairs : attList) {
            
            String[] pair = pairs.split(PAR_SEPARATOR);

            switch(pair[0]){
                case ComInvestingWebServiceResponseParser.OPEN_ATT:
                    data[lineNum][OPEN_INDEX] = pair[1];
                    break;
                case ComInvestingWebServiceResponseParser.COLSE_ATT:
                    data[lineNum][CLOSE_INDEX] = pair[1];
                    break;
                case ComInvestingWebServiceResponseParser.LOW_ATT:
                    data[lineNum][LOW_INDEX] = pair[1];
                    break;
                case ComInvestingWebServiceResponseParser.HIGH_ATT:
                    data[lineNum][HIGH_INDEX] = pair[1];
                    break;
                case ComInvestingWebServiceResponseParser.DATE_ATT:
                    data[lineNum][DATE_INDEX] = pair[1];
                    break;
                default:
                    throw new UnsupportedOperationException("Unknow data attributes!");
            }
            
        }
    }
    
    @Override
    public final void restartRetriver() {
        stopRetrieve();
        liveDataRetrieveThreadGroup = new ThreadGroup("Live Rate Retrieve Group");
        for(CurrencyExchange ce : CurrencyExchange.values()) {
            Thread retrieveThread = new Thread(liveDataRetrieveThreadGroup, this, THREAD_PREFIX + ce.getName());
            retrieveThread.start();
        }
    }

    @Override
    public void stopRetrieve() {
        if(liveDataRetrieveThreadGroup == null)
            return;
        
        Thread[] list = new Thread[liveDataRetrieveThreadGroup.activeCount()];
        stopRetriveSignal = true;
        liveDataRetrieveThreadGroup.enumerate(list);
        LOG.info("Stoping Retrieving Live Rate...");
        
        for(int i=0; i<list.length; i++) {
            try {
                if(list[i] != null)
                    list[i].join();
            } catch (InterruptedException ex) {
                LOG.warn(list[i].getName()+" join failed!");
            }
        }
        liveDataRetrieveThreadGroup.destroy();
        liveDataRetrieveThreadGroup = null;
    }

    @Override
    public final void retrieve() throws IOException {
        for(CurrencyExchange ce : CurrencyExchange.values()) {
            retrieve(ce);
        }
    }
    
    public final void retrieve(CurrencyExchange ce) throws IOException {
        File liveRateDir = prepareLiveRateDir(liveDataSaveDir);
        File rateFile = makeSureFileExists(new File(liveRateDir, ce.toString()));
        BufferedReader reader = new BufferedReader(new FileReader(rateFile));
        String lastTStamp = reader.readLine();
        if(lastTStamp == null)
            return;
        String dataLine = reader.readLine();
        if(dataLine == null)
            return;
        String name = ce.getName();
        parseLiveDataLine(name, dataLine);
        lastRetrieveStamp.put(name, lastTStamp);
    }

    @Override
    public ZonedDateTime getRetrieveDateTime() {
        return ZonedDateTime.now(Clock.systemUTC());
    }

    public void setTempHistorySavePath(String tempHistorySavePath) {
        if(tempHistorySavePath != null) {
            this.tempHistorySavePath = tempHistorySavePath;
        }
    }

    public void setLiveDataSaveDir(String liveDataSaveDir) {
        if(liveDataSaveDir != null) {
            this.liveDataSaveDir = liveDataSaveDir;
        }
    }
    
    public static void main(String[] args) throws Exception {
        ComInvestingDataRetriever retriever = new ComInvestingDataRetriever();
        Object[][] data = retriever.parseRHFileToArrays(20);
        for(Object[] objArr : data) {
            boolean first = true;
            for(Object obj : objArr) {

                if(obj != null) {
                    if(!first) {
                        System.out.print(",");
                    }
                    System.out.print(obj);
                    first = false;
                }
            }
            System.out.println();
        }
    }

    @Override
    public void run() {
        
        File liveDir = prepareLiveRateDir(liveDataSaveDir);
        Thread thisThread = Thread.currentThread();
        String tName = thisThread.getName();
        String retriveName = tName.substring(THREAD_PREFIX.length());
        CurrencyExchange type = CurrencyExchange.ofName(retriveName);
        String fileName = type.toString();
        File rateFile = makeSureFileExists(new File(liveDir, fileName));
        SAXParser saxParser = null;
        
        try {
            saxParser = saxParserFactory.newSAXParser();
        } catch (ParserConfigurationException | SAXException ex) {
            LOG.error("saxParser creating error!", ex);
        }
        
        requireNonNull(saxParser);
        
        while(!stopRetriveSignal) {
            try(FileWriter fwriter = new FileWriter(rateFile, false))  {
                String lastTStamp = lastRetrieveStamp.get(retriveName);
                StringWriter writer = new StringWriter();
                if(lastTStamp == null) {
                    invoker.retrieveRTData(type, writer);
                } else {
                    invoker.retrieveLastRTData(type, lastTStamp, writer);
                }
                
                saxParser.parse(new InputSource(new StringReader(writer.toString())), 
                        new ComInvestingWebServiceResponseParser(fwriter));
                retrieve(type);
            }catch(IOException ex) {
                LOG.error("write live rate file error!", ex);
            } catch (SAXException ex) {
                LOG.error("the retrieved xml error!", ex);
            }
            
            try {
                Thread.sleep(retrieveFrequent.toMillis());
            } catch (InterruptedException ex) {
            }
        }
    }

    private void parseLiveDataLine(String currencyType, String liveDataLine) {
        String[] attList = liveDataLine.split(ATT_SEPARATOR);
        Map<String, String> currencyDataMap = new HashMap<>();
        for(String pairs : attList) {
            String[] pair = pairs.split(PAR_SEPARATOR);
            currencyDataMap.put(pair[0], pair[1]);
        }
        liveDataMap.put(currencyType, currencyDataMap);
    }

    private File prepareLiveRateDir(String liveDataSaveDir) {
        File dir = new File(liveDataSaveDir);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    @Override
    public String getTick(CurrencyExchange exchangeType) {
        Map<String, String> dataMap = liveDataMap.get(exchangeType.getName());
        if(dataMap == null) {
            return null;
        }
        return dataMap.get(ComInvestingWebServiceResponseParser.TICK_ATT);
    }

    @Override
    public BigDecimal getChangeValue(CurrencyExchange exchangeType) {
        Map<String, String> dataMap = liveDataMap.get(exchangeType.getName());
        if(dataMap == null) {
            return new BigDecimal(0);
        }
        return new BigDecimal(dataMap.get(ComInvestingWebServiceResponseParser.CHANGE_ATT));
    }

    @Override
    public String getChangePercentValue(CurrencyExchange exchangeType) {
        Map<String, String> dataMap = liveDataMap.get(exchangeType.getName());
        if(dataMap == null) {
            return null;
        }
        return dataMap.get(ComInvestingWebServiceResponseParser.CHANGE_PERCENT_ATT);
    }

    @Override
    public ZonedDateTime getPriceDateTime(CurrencyExchange exchangeType) {
                Map<String, String> dataMap = liveDataMap.get(exchangeType.getName());
        if(dataMap == null) {
            return ZonedDateTime.now(Clock.systemUTC());
        }
        return LocalDateTime.from(datetimeFormatter.parse(dataMap.get(ComInvestingWebServiceResponseParser.DATE_ATT))).atZone(ZoneId.of("Z"));
    }

    @Override
    public BigDecimal getOtherValue(CurrencyExchange exchangeType) {
        Map<String, String> dataMap = liveDataMap.get(exchangeType.getName());
        if(dataMap == null) {
            return new BigDecimal(0);
        }
        return new BigDecimal(dataMap.get(ComInvestingWebServiceResponseParser.OTHER_ATT));
    }

    @Override
    public BigDecimal getAskPriceValue(CurrencyExchange exchangeType) {
        Map<String, String> dataMap = liveDataMap.get(exchangeType.getName());
        if(dataMap == null) {
            return new BigDecimal(0);
        }
        return new BigDecimal(dataMap.get(ComInvestingWebServiceResponseParser.ASK_ATT));
    }

    @Override
    public BigDecimal getBidPriceValue(CurrencyExchange exchangeType) {
        Map<String, String> dataMap = liveDataMap.get(exchangeType.getName());
        if(dataMap == null) {
            return new BigDecimal(0);
        }
        return new BigDecimal(dataMap.get(ComInvestingWebServiceResponseParser.BID_ATT));
    }
    
    private class ComInvestingWebServiceResponseParser extends DefaultHandler {

        public static final String SYMBOL_TAG = "symbol";
        public static final String LAST_TSTAMP_ATT = "lastTimeStamp";
        public static final String DATA_TAG = "data";
        public static final String ASK_ATT = "ask";
        public static final String BID_ATT = "bid";
        public static final String OTHER_ATT = "other";
        public static final String OPEN_ATT = "open";
        public static final String COLSE_ATT = "close";
        public static final String LOW_ATT = "low";
        public static final String HIGH_ATT = "high";
        public static final String DATE_ATT = "date";
        public static final String CHANGE_ATT = "change";
        public static final String CHANGE_PERCENT_ATT = "changep";
        public static final String TICK_ATT = "tick";
        
        private Writer writer;
        
        private List<Map<String, String>> dataMap;
        
        public ComInvestingWebServiceResponseParser(String savePath) {

            requireNonNull(savePath);            
            File file = makeSureFileExists(savePath);
            try {    
                writer = new FileWriter(savePath, false);
            } catch (IOException ex) {
                LOG.error("Error occur when connecting to save file:" + file.getAbsolutePath());
            }
        }
        
        public ComInvestingWebServiceResponseParser(Writer writer) {
            requireNonNull(writer);
            this.writer = writer;
        }

        public ComInvestingWebServiceResponseParser() {
            this.dataMap = new LinkedList<>();
        }
        
        public ComInvestingWebServiceResponseParser(List<Map<String, String>> dataMap) {
            requireNonNull(dataMap);
            this.dataMap = dataMap;
        }

        public void setWriter(Writer writer) {
            this.writer = writer;
        }

        public void setDataMap(List<Map<String, String>> dataMap) {
            this.dataMap = dataMap;
        }

        public Writer getWriter() {
            return writer;
        }

        /**
         * Get the history rate data list
         * @return the first map of the list is a single value map that only contains lastTimeStamp
         *         the rest of them contain the map that have data of open,close,low,high,date 
         */ 
        public List<Map<String, String>> getDataMap() {
            return dataMap;
        }

        @Override
        public void endDocument() throws SAXException {
            if(writer != null) {
                try {
                    writer.flush();
                } catch (IOException ex) {
                    LOG.error("Write service response error when flushing writer!");
                }
            }
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            try {
                if(qName.equalsIgnoreCase(SYMBOL_TAG)) {
                    String tstamp = attributes.getValue(LAST_TSTAMP_ATT);
                    if(writer != null) {
                        writer.write(tstamp);
                        writer.write(LINE_SEPARATOR);
                    }
                    if(dataMap != null) {
                        Map<String, String> dateMap = new HashMap<>(1);
                        dateMap.put(LAST_TSTAMP_ATT, tstamp);
                        dataMap.add(dateMap);
                    }
                } else if(qName.equalsIgnoreCase(DATA_TAG)) {
                    
                    Map<String, String> attMap = null;
                    if(dataMap != null) {
                        attMap = new HashMap<>();
                    }
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String value = attributes.getValue(i);
                        if(value.trim().length() != 0) {
                            String key = attributes.getQName(i);
                            if(writer != null) {
                                writer.write(key);
                                writer.write(PAR_SEPARATOR);
                                writer.write(value);
                                writer.write(ATT_SEPARATOR);
                            }
                            if(attMap != null) {
                                attMap.put(key, value);
                            }
                        }
                    }
                    if(writer != null) {
                        writer.write(LINE_SEPARATOR);
                    }
                    if(dataMap != null) {
                        dataMap.add(attMap);
                    }
                }
                
            } catch (IOException ex) {
                 LOG.error("writing field error!", ex);
            }
            
        }
    }
    
    private class ComInvestingWebServiceInvoker {
        
        private static final String seviceUrl = "http://advcharts.forexpros.com/advinion_charts.php";
        
        private static final String RT_SERVICE = "tns:GetRT";
        private static final String RH_SERVICE = "tns:GetRecentHistory";
        private static final String SERVICE_NS = "http://advtest.forexpros.com/";
        private static final String ARR_SYMBOL = "arrSymbol";
        private static final String ARR_LAST_TSTAMP = "arrLastTimeStamp";
        private static final String TYPE_ARR_STRING = "tns:ArrayOfString";
        private static final String ARR_TOP = "arrTop";
        private static final String ARR_TIME_FRAME = "arrTimeFrame";
        private static final String TYPE_ARR_INT = "tns:ArrayOfInt";
        private static final String INT_ARG = "int";
        private static final String TYPE_INT = "int";
        private static final String ARR_PRICE_TYPE = "arrPriceType";
        private static final String STR_USER_ID = "strUserId";
        private static final String STR_EXTRA_DATA = "strExtraData";
        private static final String ARR_FIELDS_MODE = "arrFieldsMode";
        private static final String STRING_ARG = "string";
        private static final String TYPE_STRING = "string";

        public void retrieveLastRTData(CurrencyExchange ce, String tstamp, Writer output) {
            requireNonNull(ce);
            requireNonNull(tstamp);
            requireNonNull(output);
            invokeWebMethod(rtLastMethod(getInstrument(ce.getName()), tstamp), output);
        }
        
        public void retrieveRTData(CurrencyExchange ce, Writer output) {
            requireNonNull(ce);
            requireNonNull(output);
            invokeWebMethod(rtMethod(getInstrument(ce.getName())), output);
        }
        
        public void retrieveRHData(CurrencyExchange ce, int maxData, Duration duration, Writer output) {
            
            requireNonNull(ce);
            requireNonNull(output);
            
            String top = maxData == 0 ? "600" : String.valueOf(maxData);
            String timeFrame = "15M";
            long days = duration.toDays();
            if(days > 30) {
                timeFrame = months(days / 30);
            } else if(days > 7){
                timeFrame = weeks(days / 7);
            }else if(days != 0) {
                timeFrame = days(days);
            } else if(duration.toHours() != 0) {
                timeFrame = hours(duration.toHours());
            } else if(duration.toMinutes() != 0) {
                timeFrame = minitues(duration.toMinutes());
            }
            invokeWebMethod(rhMethod(getInstrument(ce.getName()), top, timeFrame), output);
        }
        
        public void invokeWebMethod(RequestMethod method, Writer output) {
            try {
                URL url = new URL(seviceUrl);
                URLConnection conn = url.openConnection();
                fakeProperty(conn);
                SimpleSoapRequestBuilder rqBuilder = new SimpleSoapRequestBuilder();
                rqBuilder.addMethod(method);
                byte[] bytes = rqBuilder.toXmlBytes();
                conn.addRequestProperty("Content-Length", String.valueOf(bytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(bytes);
                conn.connect();
                int len;
                InputStream input;
                String contentEncoding = conn.getHeaderField("Content-Encoding");
                if("gzip".equalsIgnoreCase(contentEncoding)) {
                    input = new GZIPInputStream(conn.getInputStream());
                } else {
                    input = conn.getInputStream();
                }
                while((len = input.read()) != -1) {
                    output.write(len);
                }
                output.flush();
                input.close();
            } catch (IOException ex) {
                LOG.error("error occured when invoking web method", ex);
            }
        }
        
        private void fakeProperty(URLConnection conn) {
            conn.addRequestProperty("Accept", "*/*");
            conn.addRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
            conn.addRequestProperty("Connection", "keep-alive");
            conn.addRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.addRequestProperty("Host", "advcharts.forexpros.com");
            conn.addRequestProperty("Origin", "http://advcharts.forexpros.com");
            conn.addRequestProperty("Referer", "http://advcharts.forexpros.com/advinion2012/AdvinionProfessionalChart.swf?sid=A07102009a");
            conn.addRequestProperty("SOAPAction", "http://advcharts.forexpros.com/advinion_charts.php/GetRecentHistory");
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");
        }
        
        private RequestMethod rtLastMethod(String arrSymbol, String arrLastTimeStamp) {
            RequestMethod method = new RequestMethod(RT_SERVICE, SERVICE_NS);
            RequestMethodArgument symbol = new RequestMethodArgument(ARR_SYMBOL, TYPE_ARR_STRING);
            symbol.setValue(new RequestMethodArgument(STRING_ARG, TYPE_STRING, arrSymbol));
            RequestMethodArgument lastTimeStamp = new RequestMethodArgument(ARR_LAST_TSTAMP, TYPE_ARR_STRING);
            lastTimeStamp.setValue(new RequestMethodArgument(STRING_ARG, TYPE_STRING, arrLastTimeStamp));
            RequestMethodArgument strUserId = new RequestMethodArgument(STR_USER_ID, TYPE_STRING, "");
            RequestMethodArgument strExtraData = new RequestMethodArgument(STR_EXTRA_DATA, TYPE_STRING, "forex,1");
            method.getArguments().add(symbol);
            method.getArguments().add(lastTimeStamp);
            method.getArguments().add(strUserId);
            method.getArguments().add(strExtraData);
            return method;
        }
        
        private RequestMethod rtMethod(String arrSymbol) {
            RequestMethod method = new RequestMethod(RT_SERVICE, SERVICE_NS);
            RequestMethodArgument symbol = new RequestMethodArgument(ARR_SYMBOL, TYPE_ARR_STRING);
            symbol.setValue(new RequestMethodArgument(STRING_ARG, TYPE_STRING, arrSymbol));
            RequestMethodArgument strUserId = new RequestMethodArgument(STR_USER_ID, TYPE_STRING, "");
            RequestMethodArgument strExtraData = new RequestMethodArgument(STR_EXTRA_DATA, TYPE_STRING, "forex,1");
            method.getArguments().add(symbol);
            method.getArguments().add(strUserId);
            method.getArguments().add(strExtraData);
            return method;
        }
        
        private RequestMethod rhMethod(String arrSymbol, String arrTop, String arrTimeFrame) {
            RequestMethod method = new RequestMethod(RH_SERVICE, SERVICE_NS);
            RequestMethodArgument symbol = new RequestMethodArgument(ARR_SYMBOL, TYPE_ARR_STRING);
            symbol.setValue(new RequestMethodArgument(STRING_ARG, TYPE_STRING, arrSymbol));
            RequestMethodArgument top = new RequestMethodArgument(ARR_TOP, TYPE_ARR_INT);
            top.setValue(new RequestMethodArgument(INT_ARG, TYPE_INT, arrTop));
            RequestMethodArgument timeFrame = new RequestMethodArgument(ARR_TIME_FRAME, TYPE_ARR_STRING);
            timeFrame.setValue(new RequestMethodArgument(STRING_ARG, TYPE_STRING, arrTimeFrame));
            RequestMethodArgument priceType = new RequestMethodArgument(ARR_PRICE_TYPE, TYPE_ARR_STRING);
            priceType.setValue(new RequestMethodArgument(STRING_ARG, TYPE_STRING, "other"));
            RequestMethodArgument fieldsMode = new RequestMethodArgument(ARR_FIELDS_MODE, TYPE_ARR_STRING);
            fieldsMode.setValue(new RequestMethodArgument(STRING_ARG, TYPE_STRING, "allFields"));
            RequestMethodArgument strUserId = new RequestMethodArgument(STR_USER_ID, TYPE_STRING, "");
            RequestMethodArgument strExtraData = new RequestMethodArgument(STR_EXTRA_DATA, TYPE_STRING, "forex,1");
            method.getArguments().add(symbol);
            method.getArguments().add(top);
            method.getArguments().add(timeFrame);
            method.getArguments().add(priceType);
            method.getArguments().add(fieldsMode);
            method.getArguments().add(strUserId);
            method.getArguments().add(strExtraData);
            return method;
        }
        

        
        private String minitues(long num) {
            return num + "M";
        }
        
        private String hours(long num) {
            return num + "H";
        }
                
        private String days(long num) {
            return num + "D";
        }
        
        private String weeks(long num) {
            return num + "W";
        }
        
        private String months(long num) {
            return num + "MON";
        }        
        
    }
}
