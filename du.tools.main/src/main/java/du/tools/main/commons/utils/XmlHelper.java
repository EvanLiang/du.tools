package du.tools.main.commons.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class XmlHelper {

    private Document doc;
    private XPath xpath;

    public XmlHelper(String uri) throws Exception {
        this(buildDoc(uri));
    }

    public XmlHelper(Document doc) {
        this.doc = doc;
        xpath = XPathFactory.newInstance().newXPath();
    }

    public static Document buildDoc(String uri) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true); // never forget this!
//        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(uri);
    }

    public static Document buildEmptyDoc() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    public String getContent() throws TransformerFactoryConfigurationError, TransformerException {
        return getContentAt(doc);
    }

    public String getContentAt(Node node) throws TransformerFactoryConfigurationError, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    public XmlHelper getNodeAtAsXmlMessage(String xpathExpression) throws TransformerFactoryConfigurationError, TransformerException, Exception {
        Node node = getNodeAt(xpathExpression);
        return node != null ? new XmlHelper(getContentAt(node)) : null;
    }

    public Node getNodeAt(String xpathExpression) throws XPathExpressionException {
        return (Node) xpath.evaluate(xpathExpression, doc, XPathConstants.NODE);
    }

    public String getValueAt(String xpathExpression) throws XPathExpressionException {
        return xpath.evaluate(xpathExpression, doc);
    }

    public <T> List<T> getListAt(String xpathExpression, Class<T> clazz) throws Exception {
        NodeList nl = (NodeList) xpath.evaluate(xpathExpression, doc, XPathConstants.NODESET);
        List<T> list = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            T t = clazz.newInstance();
            NodeList cnl = n.getChildNodes();
            for (int j = 0; j < cnl.getLength(); j++) {
                Node c = cnl.item(j);
                String name = c.getNodeName();
                BeanUtils.setProperty(t, name, c.getTextContent());
            }
            list.add(t);
        }
        return list;
    }

    @Override
    public String toString() {
        try {
            return getContent();
        } catch (Exception e) {
            return "Can't get content for " + super.toString();
        }
    }

    public void setValueAt(String xpathExpression, String value) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        setValueAt(xpathExpression, value, false, false);
    }

    public void setValueAt(String xpathExpression, String value, boolean isValueXml, boolean appendXmlValue) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        Node targetNode = (Node) xpath.evaluate(xpathExpression, doc, XPathConstants.NODE);
        if (targetNode == null) {
            throw new RuntimeException("Node for xpath: " + xpathExpression + " was not found");
        }
        if (isValueXml) {
            Document valueAsDoc = buildDoc(value);
            Node valueAsNode = doc.importNode(valueAsDoc.getDocumentElement(), true);
            if (!appendXmlValue) {
                while (targetNode.hasChildNodes()) {
                    targetNode.removeChild(targetNode.getFirstChild());
                }
            }
            targetNode.appendChild(valueAsNode);
        } else {
            targetNode.setTextContent(value);
        }
    }
}