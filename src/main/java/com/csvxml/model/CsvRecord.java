package com.csvxml.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvRecord {
    private String company;
    private String userId;
    private String fullName;
    private String registerDate;
} 