package com.example.nintec.models;

import java.util.Map;

public class ProductSpecification {
    private Map<String, String> specs;

    public ProductSpecification(Map<String, String> specs) {
        this.specs = specs;
    }

    public Map<String, String> getSpecs() { return specs; }
}