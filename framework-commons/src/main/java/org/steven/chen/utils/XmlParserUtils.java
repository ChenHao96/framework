package org.steven.chen.utils;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class XmlParserUtils {

    private XmlParserUtils() {
    }

    public static String object2Xml(Object vo, String charset, Class... classes) throws Exception {
        JAXBContext context = JAXBContext.newInstance(classes);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, charset);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        marshaller.marshal(vo, byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }

    public static <T> T xml2Object(Class<T> clazz, String xmlStr) throws Exception {
        Map<String, String> xmlMap = xmlToMap(xmlStr);
        return BeanMapConvertUtil.map2Bean(xmlMap, clazz);
    }

    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        return documentBuilderFactory.newDocumentBuilder();
    }

    public static Map<String, String> xmlToMap(String strXML) throws Exception {
        if (StringUtils.isEmpty(strXML)) return null;
        Map<String, String> data = null;
        InputStream stream = null;
        try {
            DocumentBuilder documentBuilder = newDocumentBuilder();
            stream = new ByteArrayInputStream(strXML.getBytes());
            Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            if (nodeList.getLength() > 0) {
                data = new LinkedHashMap<>();
                putListNode(data, nodeList, "");
            }
        } finally {
            CommonsUtil.safeClose(stream);
        }
        return data;
    }

    private static void putListNode(Map<String, String> map, NodeList nodeList, String prefix) throws UnsupportedEncodingException {
        if (map == null) return;
        if (nodeList.getLength() > 0) {
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                String nodeName = node.getNodeName();
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String keyName = prefix + nodeName;
                    NodeList childNodes = node.getChildNodes();
                    if (childNodes.getLength() == 1) {
                        map.put(keyName, node.getTextContent());
                    } else {
                        putListNode(map, childNodes, keyName + ".");
                    }
                }
            }
        }
    }
}
