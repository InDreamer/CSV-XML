package com.csvxml.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    public static final String INPUT_DIR = "input";
    public static final String OUTPUT_DIR = "output";
    public static final String ERROR_DIR = "error";
    
    public static final class LogMessages {
        // 文件处理相关
        public static final String FILE_FOUND = "发现新文件: {}";
        public static final String FILE_PROCESSING_START = "开始处理文件:";
        public static final String FILE_PROCESSING_COMPLETE = "文件处理完成: {}";
        
        // CSV解析相关
        public static final String CSV_PARSING_START = "开始解析CSV文件:";
        public static final String CSV_PARSED = "CSV文件解析完成，共 {} 行数据";
        public static final String CSV_ROW_COUNT = "CSV文件包含 {} 行数据";
        public static final String CSV_VALIDATION_START = "开始验证CSV数据格式";
        
        // 数据验证相关
        public static final String VALIDATION_PASSED = "数据验证通过，开始转换处理， 包含 {} 行数据";
        public static final String VALIDATION_FAILED = "数据验证失败，原因: {}";
        public static final String RECORD_PROCESSING = "正在处理记录 -> 公司: {}, 用户ID: {}, 姓名: {}";
        
        // XML转换相关
        public static final String XML_CONVERSION_START = "开始XML转换处理";
        public static final String XML_TEMPLATE_LOADING = "正在加载XML模板: {}";
        public static final String XML_TEMPLATE_LOADED = "XML模板加载完成";
        public static final String XML_NODE_PROCESSING = "正在处理XML节点: {}";
        
        // 错误处理相关
        public static final String ERROR_OCCURRED = "处理文件 {} 时发生错误";
        public static final String ERROR_RETRY = "第 {} 次重试处理";
        public static final String ERROR_DETAILS = "错误详情: {}";
        
        // 处理完成
        public static final String PROCESSING_COMPLETED = "文件处理完成:";
    }
} 