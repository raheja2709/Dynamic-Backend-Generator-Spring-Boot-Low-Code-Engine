package com.user.driven.operations.app.api.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FieldRequest {
    private String name;
    private String type;
    // Role (PRIMARY_KEY, FOREIGN_KEY, NORMAL)
    private String fieldType;

    // Optional (good to add now, saves refactor later)
    private Boolean nullable;

    // For foreign key support (future-proof)
    private String referenceEntity;
    private String referenceField;

    // getters & setters

}