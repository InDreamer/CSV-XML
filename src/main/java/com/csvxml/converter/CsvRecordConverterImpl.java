package com.csvxml.converter;

import com.csvxml.model.CsvRecord;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvRecordConverterImpl implements RecordConverter<CsvRecord>, Processor {
    
    @Override
    public List<CsvRecord> convert(List<List<String>> rows) {
        List<CsvRecord> records = new ArrayList<>();
        for (List<String> row : rows) {
            records.add(new CsvRecord(
                row.get(0),  //company
                row.get(1),  // userId
                row.get(2),   // fullName
                row.get(3)  // registerDate
            ));
        }
        return records;
    }
    
    @Override
    public void process(Exchange exchange) {
        @SuppressWarnings("unchecked")
        List<List<String>> rows = exchange.getIn().getBody(List.class);
        exchange.getIn().setBody(convert(rows));
    }
} 