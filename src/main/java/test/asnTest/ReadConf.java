package test.asnTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ReadConf {
    private final static String file_name = "src/main/resources/asn.xml";
    private static Document document;
    static {
        try {
            document = new SAXReader().read(new File(file_name));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public static void getConf(String item, Object obj) {
        Element root = document.getRootElement();
        Element itElement = root.element(item);
        Element head = itElement.element("head");
        Element body = itElement.element("body");
        Element end = itElement.element("end");
        BillFile billFile = (BillFile) obj;
        if (head != null) {
            billFile.headerStruct = getStructFromElement(head);
        }
        if (body != null) {
            Element rcds = body.element("rcds");
            billFile.rcdStruct = getStructFromElement(rcds);
            billFile.rcdTag = Integer.valueOf(rcds.attributeValue("tag"));
        }
        if (end != null) {
            billFile.endStruct = getStructFromElement(end);
        }
    }

    private static List<String[]> getStructFromElement(Element element) {
        List<String[]> list = new ArrayList<>();
        for (Iterator<Element> iterator = element.elementIterator(); iterator.hasNext();) {
            Element child = iterator.next();
            String[] tmp = new String[4];
            tmp[0] = child.attributeValue("type");
            tmp[1] = child.attributeValue("len");
            tmp[2] = child.attributeValue("tag");
            tmp[3] = child.attributeValue("errCode");
            list.add(tmp);
        }
        return list;
    }
}
