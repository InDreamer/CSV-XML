package com.csvxml.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.util.Map;

public interface XmlTemplateProcessor {
    /**
     * 加载XML模板
     */
    Document loadTemplate(String templatePath);
    
    /**
     * 克隆模板节点
     */
    Node cloneTemplateNode(Document doc, String nodeName);
    
    /**
     * 使用占位符填充节点
     */
    void fillPlaceholders(Node node, Map<String, String> placeholders);
    
    /**
     * 将Document转换为字符串
     */
    String documentToString(Document doc);
} 