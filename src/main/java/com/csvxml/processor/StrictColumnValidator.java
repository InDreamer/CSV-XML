package com.csvxml.processor;

import com.csvxml.config.AppConfig;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class StrictColumnValidator implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(StrictColumnValidator.class);
    private static final int EXPECTED_COLUMNS = 4;
    private static final String ERROR_MESSAGE = "第 %d 行列数不正确，期望 %d 列，实际 %d 列";

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.info(AppConfig.LogMessages.CSV_VALIDATION_START);

        @SuppressWarnings("unchecked")
        List<List<String>> rows = exchange.getIn().getBody(List.class);
        List<String> errors = new ArrayList<>();
        
        if (rows == null || rows.isEmpty()) {
            logger.error(AppConfig.LogMessages.VALIDATION_FAILED, "Empty CSV file");
            throw new IllegalArgumentException("Empty CSV file");
        }
        
        
        int rowNum = 0;
        for (List<String> row : rows) {
            rowNum++;
            if (row.size() != EXPECTED_COLUMNS) {
                String error = String.format(ERROR_MESSAGE, rowNum, EXPECTED_COLUMNS, row.size());
                logger.error(AppConfig.LogMessages.VALIDATION_FAILED, error);
                errors.add(error);
            }
        }
        
        if (!errors.isEmpty()) {
            String combinedError = String.join("\n", errors);
            logger.error(AppConfig.LogMessages.VALIDATION_FAILED, "发现以下列数错误：\n" + combinedError);
            throw new IllegalArgumentException(combinedError);
        } else {
            logger.info(AppConfig.LogMessages.CSV_ROW_COUNT, rowNum);
        }
    }
}