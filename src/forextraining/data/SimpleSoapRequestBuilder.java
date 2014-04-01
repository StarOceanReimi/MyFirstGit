/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.data;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Reimi
 */
public class SimpleSoapRequestBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleSoapRequestBuilder.class);
    
    private Document requestDoc;

    private Transformer transformer;
    
    private static final String SOAP_ENV_PREFIX = "SOAP-ENV";
    private static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    public SimpleSoapRequestBuilder() {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", 2);
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException ex) {
            LOG.error("builder.transformer init error!");
        }
        initDoc();
        initRequestHead();
        initRequestBody();
    }
    
    private void initDoc() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = dbFactory.newDocumentBuilder();
            requestDoc = docBuilder.newDocument();
        } catch (ParserConfigurationException ex) {
        }
    }

    private void initRequestHead() {
        Element root = requestDoc.createElementNS(SOAP_ENV_NS, "Envelope");
        root.setPrefix(SOAP_ENV_PREFIX);
        root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        requestDoc.appendChild(root);
    }
    
    private void initRequestBody() {
        Element root = requestDoc.getDocumentElement();
        Element body = requestDoc.createElementNS(SOAP_ENV_NS, "Body");
        body.setPrefix(SOAP_ENV_PREFIX);
        Attr encoding = requestDoc.createAttributeNS(SOAP_ENV_NS, "encodingStyle");
        encoding.setPrefix(SOAP_ENV_PREFIX);
        encoding.setNodeValue("http://schemas.xmlsoap.org/soap/encoding/");
        body.setAttributeNode(encoding);
        root.appendChild(body);
    }
    
    public void addMethod(RequestMethod method) {
        Element body = (Element) requestDoc.getElementsByTagNameNS(SOAP_ENV_NS, "Body").item(0);
        Element soapMethod = requestDoc.createElementNS(method.getNameSpace(), method.getName());
        List<RequestMethodArgument> argList = method.getArguments();
        for (RequestMethodArgument arg : argList) {
            appendArgument(soapMethod, arg);
        }
        body.appendChild(soapMethod);
    }
     
    private void appendArgument(Element method, RequestMethodArgument arg) {
        Element soapMethodArg = newValueElement(arg);
        setArgValue(soapMethodArg, arg);
        method.appendChild(soapMethodArg);
    }
    
    private Element newValueElement(RequestMethodArgument arg) {
        Element soapMethodArg = requestDoc.createElement(arg.getName());
        soapMethodArg.setAttribute("xsi:type", arg.getType());
        return soapMethodArg;
    }
    
    private void setArgValue(Element soapMethodArg, RequestMethodArgument arg) {
        Object value = arg.getValue();
        Element valueTag = null;
        List<Element> list = null;
        if(value.getClass().isArray() && value.getClass().getComponentType()
                .equals(RequestMethodArgument.class)) {
            list = new LinkedList<>();
            RequestMethodArgument[] args = (RequestMethodArgument[])value;
            for(RequestMethodArgument a : args) {
                if(a == null)
                    continue;
                Element inner = newValueElement(a);
                setArgValue(inner, a);
                list.add(inner);
            }
        }
        if(value instanceof RequestMethodArgument) {
            RequestMethodArgument argValue = (RequestMethodArgument)value;
            valueTag = newValueElement(argValue);
            setArgValue(valueTag, argValue);
        }
        if(value instanceof String) {
            soapMethodArg.appendChild(requestDoc.createTextNode(String.valueOf(value)));
        } else if(value instanceof RequestMethodArgument && valueTag != null) {
            soapMethodArg.appendChild(valueTag);
        } else if(value.getClass().isArray() && list != null) {
            for(Element el : list) {
                soapMethodArg.appendChild(el);
            }
        }
    }
    
    public String toXmlString() {
        return toXmlString(false);
    }
    
    public String toXmlString(boolean prettyPrint) {
        if(prettyPrint)
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        try {
            transformer.transform(new DOMSource(requestDoc), result);
        } catch (TransformerException ex) {
            LOG.error("builder toxmlstring transforming error!");
        }
        return result.getWriter().toString();
    }
    
    public byte[] toXmlBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        try {
            transformer.transform(new DOMSource(requestDoc), result);
        } catch (TransformerException ex) {
            LOG.error("builder toxmlstring transforming error!");
        }
        return baos.toByteArray();
    }
    
    public static void main(String[] args) throws TransformerException {
        SimpleSoapRequestBuilder ssr = new SimpleSoapRequestBuilder();
        RequestMethod rm = new RequestMethod("tns:GetRT", "http://advtest.forexpros.com/");
        RequestMethodArgument rma = new RequestMethodArgument();
        rma.setName("arraySymbols");
        rma.setType("ArrayOfString", "tns");
        rma.setValue(new RequestMethodArgument("string","string","1"));
        rm.getArguments().add(rma);
        ssr.addMethod(rm);
        
        System.out.println(ssr.toXmlBytes().length);
        
    }



    public static class RequestMethod {

        private String name;
        
        private String nameSpace;
        
        private List<RequestMethodArgument> arguments;

        public RequestMethod() {
        }

        public RequestMethod(String name, String nameSpace) {
            this.name = name;
            this.nameSpace = nameSpace;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameSpace() {
            return nameSpace;
        }

        public void setNameSpace(String nameSpace) {
            this.nameSpace = nameSpace;
        }

        public List<RequestMethodArgument> getArguments() {
            if(arguments == null) {
                arguments = new LinkedList<>();
            }
            return arguments;
        }

        public void setArguments(List<RequestMethodArgument> arguments) {
            this.arguments = arguments;
        }
    }
    
    public static class RequestMethodArgument {
        
        private String name;
        
        private String type;
        
        private Object value;

        public RequestMethodArgument() {
        }

        public RequestMethodArgument(String name, String type) {
            this.name = name;
            this.type = type;
        }
        
        public RequestMethodArgument(String name, String type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
        
        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = "xsd:" + type;
        }
        
        public void setType(String type, String prefix) {
            this.type = prefix + ":" + type;
        }

        public void setValue(Object value) {
            if(value == null) {
                this.value = "";
            }
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
        
    }
}
