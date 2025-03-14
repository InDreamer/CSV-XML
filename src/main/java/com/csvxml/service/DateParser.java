package com.csvxml.service;

import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
public class DateParser {
    private static final List<String> DATE_FORMATS = Arrays.asList(
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MMM dd, yyyy",
        "MM/dd/yyyy"
    );
    
    public Instant parse(String rawDate) {
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(rawDate).toInstant();
            } catch (ParseException ignored) {
                // 继续尝试下一个格式
            }
        }
        throw new IllegalArgumentException("Unsupported date format: " + rawDate);
    }
} 