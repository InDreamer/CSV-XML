package com.csvxml.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class NameProcessor {
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]+");
    
    public Map<String, String> parse(String fullName) {
        Map<String, String> result = new HashMap<>();
        
        if (isChineseName(fullName)) {
            // 处理中文名
            if (fullName.length() < 2) {
                throw new IllegalArgumentException("Invalid Chinese name length");
            }
            result.put("surname", fullName.substring(0, 1));
            result.put("given", fullName.substring(1));
        } else {
            // 处理西方名
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Western name must contain both given name and surname");
            }
            result.put("given", parts[0]);
            result.put("surname", parts[parts.length - 1]);
        }
        
        return result;
    }
    
    private boolean isChineseName(String name) {
        return CHINESE_PATTERN.matcher(name).matches();
    }
} 