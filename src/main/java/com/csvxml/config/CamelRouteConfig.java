package com.csvxml.config;

import com.csvxml.converter.CsvRecordConverterImpl;
import com.csvxml.processor.StrictColumnValidator;
import com.csvxml.processor.TemplateEnricher;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Configuration
public class CamelRouteConfig extends RouteBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(CamelRouteConfig.class);
    private final StrictColumnValidator columnValidator;
    private final TemplateEnricher templateEnricher;
    private final CsvRecordConverterImpl csvRecordConverter;
    
    public CamelRouteConfig(
            StrictColumnValidator columnValidator,
            TemplateEnricher templateEnricher,
            CsvRecordConverterImpl csvRecordConverter) {
        this.columnValidator = columnValidator;
        this.templateEnricher = templateEnricher;
        this.csvRecordConverter = csvRecordConverter;
    }
    
    @Bean
    public CsvDataFormat csvFormat() {
        CsvDataFormat csv = new CsvDataFormat();
        csv.setDelimiter(',');
        csv.setSkipHeaderRecord(false);
        csv.setUseMaps(false);
        return csv;
    }
    
    @Override
    public void configure() throws Exception {
        // CSV格式相关异常处理
        onException(IllegalArgumentException.class)
            .handled(true)
            .maximumRedeliveries(2)
            .redeliveryDelay(1000)
            .logRetryAttempted(true)
            .process(exchange -> {
                // 获取异常信息
                Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
                
                // 记录详细日志
                logger.error("CSV处理错误 - 文件: {}, 错误: {}", fileName, cause.getMessage());
                
                // 保存原始CSV内容和错误信息
                String originalContent = exchange.getIn().getHeader("CamelFileContent", String.class);
                if (originalContent == null) {
                    // 如果没有原始内容，尝试从body获取
                    Object body = exchange.getIn().getBody();
                    if (body instanceof List) {
                        originalContent = body.toString();
                    } else {
                        originalContent = "无法获取原始内容";
                    }
                }
                // 设置错误输出内容
                StringBuilder errorOutput = new StringBuilder();
                errorOutput.append("CSV处理错误:\n").append(cause.getMessage()).append("\n\n");
                errorOutput.append("原始内容:\n").append(originalContent);
                
                exchange.getIn().setBody(errorOutput.toString());
            })
            .log("CSV处理错误 - 第${header.CamelRedeliveryCounter}次重试")
            .to("file://" + AppConfig.ERROR_DIR + "?fileName=${file:name.noext}_csv_error_${date:now:yyyyMMddHHmmss}.txt");
        
        // 数据转换异常处理
        onException(TypeConversionException.class)
            .handled(true)
            .maximumRedeliveries(2)
            .redeliveryDelay(1000)
            .logRetryAttempted(true)
            .process(exchange -> {
                // 获取异常信息
                Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
                
                // 记录详细日志
                logger.error("数据转换错误 - 文件: {}, 错误: {}", fileName, cause.getMessage());
                
                // 保存原始内容和错误信息
                String originalContent = exchange.getIn().getHeader("CamelFileContent", String.class);
                if (originalContent == null) {
                    originalContent = "无法获取原始内容";
                }
                
                // 设置错误输出内容
                StringBuilder errorOutput = new StringBuilder();
                errorOutput.append("数据转换错误: ").append(cause.getMessage()).append("\n\n");
                errorOutput.append("原始内容:\n").append(originalContent);
                
                exchange.getIn().setBody(errorOutput.toString());
            })
            .log("数据转换错误 - 文件: ${header.CamelFileName}")
            .to("file://" + AppConfig.ERROR_DIR + "?fileName=${file:name.noext}_conversion_error_${date:now:yyyyMMddHHmmss}.txt");
        
        // 系统或运行时异常处理
        onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(1)
            .redeliveryDelay(1000)
            .logRetryAttempted(true)
            .process(exchange -> {
                // 获取异常信息
                Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
                
                // 记录详细日志
                logger.error("系统错误 - 文件: {}, 错误类型: {}, 错误信息: {}", 
                    fileName, cause.getClass().getName(), cause.getMessage());
                
                // 设置错误输出内容
                StringBuilder errorOutput = new StringBuilder();
                errorOutput.append("系统错误: ").append(cause.getMessage()).append("\n");
                errorOutput.append("异常类型: ").append(cause.getClass().getName()).append("\n");
                
                // 添加堆栈跟踪
                errorOutput.append("\n堆栈跟踪:\n");
                for (StackTraceElement element : cause.getStackTrace()) {
                    errorOutput.append(element.toString()).append("\n");
                }
                
                exchange.getIn().setBody(errorOutput.toString());
            })
            .log("系统错误 - 文件: ${header.CamelFileName}")
            .to("file://" + AppConfig.ERROR_DIR + "?fileName=${file:name.noext}_system_error_${date:now:yyyyMMddHHmmss}.txt");

        from("file://" + AppConfig.INPUT_DIR + "?initialDelay=1000&delay=5000&readLock=none&delete=false&include=.*\\.csv")
            .routeId("csvToXmlRoute")
            .log(AppConfig.LogMessages.FILE_PROCESSING_START + " ${header.CamelFileName}")
            .log(AppConfig.LogMessages.CSV_PARSING_START + " ${header.CamelFileName}")
            .unmarshal(csvFormat())
            .process(columnValidator)
            .process(csvRecordConverter)
            .log(AppConfig.LogMessages.XML_CONVERSION_START)
            .process(templateEnricher)
            .to("file://" + AppConfig.OUTPUT_DIR + "?fileName=${file:name.noext}_${date:now:yyyyMMddHHmmss}.xml")
            .log(AppConfig.LogMessages.PROCESSING_COMPLETED + " ${header.CamelFileName}");

        // 启动时的日志
        logger.info("Camel路由配置完成，开始监听{}目录", AppConfig.INPUT_DIR);
    }
} 