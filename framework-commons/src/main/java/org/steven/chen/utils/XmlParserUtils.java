package org.steven.chen.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

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

    @SuppressWarnings("unchecked")
    public static <T> T xml2Object(Class<T> clazz, String xmlStr) throws Exception {

        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xmlStr));
    }
}
