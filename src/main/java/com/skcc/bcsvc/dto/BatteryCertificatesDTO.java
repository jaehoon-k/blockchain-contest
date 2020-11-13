package com.skcc.bcsvc.dto;

import java.util.Map;

public class BatteryCertificatesDTO extends ERC721MetadataJsonSchema {
    private Map<String, Object> attributes;

    public BatteryCertificatesDTO() {}

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object>attributes) {
        this.attributes = attributes;
    }
}
