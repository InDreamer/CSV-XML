package com.csvxml.util;

public enum TransformError {
    COLUMN_MISMATCH("列数量不匹配"),
    NAME_FORMAT_ERR("姓名格式错误"),
    DATE_PARSE_FAIL("日期解析失败");

    private final String message;

    TransformError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
} 