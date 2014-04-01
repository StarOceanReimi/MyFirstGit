/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Reimi
 */
public class TestCurrencyExchangeCal {
    
    @Test
    public void testByteBuffer() throws InterruptedException {
        
        ByteBuffer bb = ByteBuffer.allocate(7);
        bb.putChar('A');
        bb.putInt(10);
        System.out.println("Position:"+bb.position());
        System.out.println("Limit:"+bb.limit());
        System.out.println("Array:"+Arrays.toString(bb.array()));
        bb.flip();
        System.out.println("After Flip");
        System.out.println("Position:"+bb.position());
        System.out.println("Limit:"+bb.limit());
        System.out.println("Array:"+Arrays.toString(bb.array()));
    }
    
    @Test
    public void testSaxHandler() throws Exception {
        
        FileInputStream fis = new FileInputStream("c:/soapresponse.txt");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        
        String xml = "<project><users><user age=\"30\">jim</user><user age=\"28\">mary</user></users></project>";
        
        parser.parse(new InputSource(new StringReader(xml)), new DefaultHandler(){
            
            int age;
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                
                if(qName.equals("user")) {
                    age = Integer.parseInt(attributes.getValue("age"));
//                    for (int i = 0; i < attributes.getLength(); i++) {
//                        System.out.println(attributes.getQName(i)+"="+attributes.getValue(i));
//                    }
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                System.out.println("Name:"+new String(ch, start, length)+", Age:"+age);
            }
        });
        
    }
}



















