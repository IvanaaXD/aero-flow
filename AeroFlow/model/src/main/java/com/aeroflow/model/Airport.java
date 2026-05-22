package com.aeroflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {
    private String code;
    private String name;
    private Map<String, Integer> mctMatrix;
}