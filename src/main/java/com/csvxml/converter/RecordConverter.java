package com.csvxml.converter;

import java.util.List;

public interface RecordConverter<T> {
    List<T> convert(List<List<String>> rows);
} 