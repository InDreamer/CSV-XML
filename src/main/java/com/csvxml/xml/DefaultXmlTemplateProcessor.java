package com.csvxml.xml;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

@Component
public class DefaultXmlTemplateProcessor implements XmlTemplateProcessor {
    
    private static final String INDENT_AMOUNT = "4";
    private static final String ENCODING = "UTF-8";
    
    @Override
    public Document loadTemplate(String templatePath) {
        try {
            InputStream is = getClass().getResourceAsStream(templatePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load XML template: " + templatePath, e);
        }
    }
    
    @Override
    public Node cloneTemplateNode(Document doc, String nodeName) {
        Node templateNode = doc.getElementsByTagName(nodeName).item(0);
        return templateNode.cloneNode(true);
    }
    
    @Override
    public void fillPlaceholders(Node node, Map<String, String> placeholders) {
        // 处理属性
        if (node.getAttributes() != null) {
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                Node attr = node.getAttributes().item(i);
                String value = attr.getNodeValue();
                if (isPlaceholder(value)) {
                    String placeholder = extractPlaceholder(value);
                    if (placeholders.containsKey(placeholder)) {
                        attr.setNodeValue(placeholders.get(placeholder));
                    }
                }
            }
        }
        
        // 处理文本内容
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (node.getChildNodes().getLength() == 1 && 
                node.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                String text = node.getFirstChild().getNodeValue();
                if (isPlaceholder(text)) {
                    String placeholder = extractPlaceholder(text);
                    if (placeholders.containsKey(placeholder)) {
                        node.getFirstChild().setNodeValue(placeholders.get(placeholder));
                    }
                }
            }
        }
        
        // 递归处理子节点
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            fillPlaceholders(children.item(i), placeholders);
        }
    }
    
    @Override
    public String documentToString(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            
            // 设置XML输出属性
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, ENCODING);
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", INDENT_AMOUNT);
            
            // 移除多余的空白
            doc.normalize();
            
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to String", e);
        }
    }
    
    private boolean isPlaceholder(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }
    
    private String extractPlaceholder(String value) {
        return value.substring(2, value.length() - 1);
    }
} 