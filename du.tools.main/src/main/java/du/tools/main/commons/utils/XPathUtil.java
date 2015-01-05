package du.tools.main.commons.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XPathUtil {

    private static Logger logger = LoggerFactory.getLogger(XPathUtil.class);

    public static void saveDocument(Document document, File xml) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            FileWriter fw = new FileWriter(xml);
            StreamResult result = new StreamResult(fw);
            transformer.transform(source, result);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Document getDocument(String xml) {
        return getDocument(new File(xml));
    }

    public static Document getDocument(File xmlFile) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream in = new FileInputStream(xmlFile);
            Document document = db.parse(in);
            in.close();
            return document;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static int countElement(Document document, String expression){
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            Double count = (Double) xpath.evaluate("count("+expression+")", document, XPathConstants.NUMBER);
            return count.intValue();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<String> getStringList(Document document, String expression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                list.add(node.getTextContent());
            }
            return list;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getString(Document document, String expression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return xpath.evaluate(expression, document);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Node getNode(Document document, String expression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (Node) xpath.evaluate(expression, document, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static NodeList getNodeList(Document document, String expression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        int a = XPathUtil.countElement(XPathUtil.getDocument("evnwhp.xml"), "/File[1]/File[1]/File[1]/File");
        System.out.println(a);
    }
}