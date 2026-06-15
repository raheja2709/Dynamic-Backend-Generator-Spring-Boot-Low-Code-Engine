package com.user.driven.operations.app.api.dto;


import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EntityRequest {

    private String name;
    private List<FieldRequest> fields;

    // getters/setters
}
