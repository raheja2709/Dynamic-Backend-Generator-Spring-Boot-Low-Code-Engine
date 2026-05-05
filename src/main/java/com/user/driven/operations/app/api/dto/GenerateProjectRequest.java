package com.user.driven.operations.app.api.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GenerateProjectRequest {

    private String name;
    private String packageName;
    private boolean securityEnabled;
    private String securityType;
    private List<EntityRequest> entities;

}
