package com.skcc.bcsvc.dto;

import java.util.List;

public class ERC721MetadataJsonSchema {
    private String name;
    private String description;
    private String image;

    public ERC721MetadataJsonSchema() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
