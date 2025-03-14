package com.csvxml.processor;

import com.csvxml.model.CsvRecord;
import com.csvxml.service.NameProcessor;
import com.csvxml.service.DateParser;
import com.csvxml.xml.XmlTemplateProcessor;
import com.csvxml.config.AppConfig;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TemplateEnricher implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(TemplateEnricher.class);
    private static final String TEMPLATE_PATH = "/templates/profile.xml";
    private static final String PROFILE_NODE = "Profile";
    private static final String ROOT_NODE = "UserProfiles";
    private static final String COMPANY_NODE = "Company";
    
    private final XmlTemplateProcessor xmlProcessor;
    private final NameProcessor nameProcessor;
    private final DateParser dateParser;
    private final Document template;

    public TemplateEnricher(
            XmlTemplateProcessor xmlProcessor,
            NameProcessor nameProcessor,
            DateParser dateParser) {
        this.xmlProcessor = xmlProcessor;
        this.nameProcessor = nameProcessor;
        this.dateParser = dateParser;
        logger.info(AppConfig.LogMessages.XML_TEMPLATE_LOADING, TEMPLATE_PATH);
        this.template = xmlProcessor.loadTemplate(TEMPLATE_PATH);
        logger.info(AppConfig.LogMessages.XML_TEMPLATE_LOADED);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
        @SuppressWarnings("unchecked")
        List<CsvRecord> records = exchange.getIn().getBody(List.class);
        
        // 按公司分组
        Map<String, List<CsvRecord>> recordsByCompany = records.stream()
            .collect(Collectors.groupingBy(CsvRecord::getCompany));
        
        logger.info("处理文件: {}, 包含 {} 个公司的数据", fileName, recordsByCompany.size());
        
        // 创建新的空白文档
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document finalDoc = dBuilder.newDocument();
        
        // 创建根元素
        Node rootNode = finalDoc.createElement(ROOT_NODE);
        finalDoc.appendChild(rootNode);
        
        // 处理每个公司的数据
        recordsByCompany.forEach((company, companyRecords) -> {
            try {
                logger.info("开始处理公司 {} 的数据，包含 {} 条记录", company, companyRecords.size());
                
                // 创建公司节点
                Node companyNode = finalDoc.createElement(COMPANY_NODE);
                companyNode.setTextContent(company);
                rootNode.appendChild(companyNode);
                
                // 处理该公司的所有用户配置文件
                for (CsvRecord record : companyRecords) {
                    logger.debug(AppConfig.LogMessages.RECORD_PROCESSING, company, record.getUserId(), record.getFullName());
                    Map<String, String> placeholders = createPlaceholderMap(record);
                    
                    logger.debug(AppConfig.LogMessages.XML_NODE_PROCESSING, PROFILE_NODE);
                    Node profileTemplate = template.getElementsByTagName(PROFILE_NODE).item(0);
                    Node profileNode = finalDoc.importNode(profileTemplate, true);
                    xmlProcessor.fillPlaceholders(profileNode, placeholders);
                    rootNode.appendChild(profileNode);
                }
                
                logger.info("完成公司 {} 的数据处理", company);
            } catch (Exception e) {
                logger.error("处理公司 {} 数据时发生错误: {}", company, e.getMessage());
                throw new RuntimeException("Error processing company: " + company, e);
            }
        });
        
        // 转换为字符串输出
        exchange.getIn().setBody(xmlProcessor.documentToString(finalDoc));
        logger.info("完成文件 {} 的XML文档生成", fileName);
    }
    
    private Map<String, String> createPlaceholderMap(CsvRecord record) {
        Map<String, String> placeholders = new HashMap<>();
        
        // 处理用户ID
        placeholders.put("userId", record.getUserId());
        
        // 处理姓名
        Map<String, String> nameParts = nameProcessor.parse(record.getFullName());
        placeholders.put("firstName", nameParts.get("given"));
        placeholders.put("lastName", nameParts.get("surname"));
        
        // 处理日期
        String formattedDate = dateParser.parse(record.getRegisterDate()).toString();
        placeholders.put("registerDate", formattedDate);
        
        return placeholders;
    }
} 