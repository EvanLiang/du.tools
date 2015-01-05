package du.tools.main.commons.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

public class FileToXml {

    public static void main(String[] args) throws Exception {
        String xml = toXmlString(new File("F:\\WorkSpace\\evnwhp"), true);
        System.out.println(xml);
    }

    public static Document toXmlDocument(File dir, boolean recursive) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = db.newDocument();
            Element element = document.createElement("File");
            element.setAttribute("name", dir.getName());
            element.setAttribute("type", "D");
            document.appendChild(element);
            createElements(dir, document, element, recursive);
            return document;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void toXmlFile(File dir, boolean recursive, File xml) {
        try {
            Document document = toXmlDocument(dir, recursive);
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

    public static String toXmlString(File dir, boolean recursive) {
        try {
            Document document = toXmlDocument(dir, recursive);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void createElements(File file, Document document, Element parent, boolean recursive) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                Element element = document.createElement("File");
                element.setAttribute("name", f.getName());
                element.setAttribute("type", f.isDirectory() ? "D" : "F");
                parent.appendChild(element);
                if (recursive && f.isDirectory()) {
                    createElements(f, document, element, true);
                }
            }
        }
    }
}