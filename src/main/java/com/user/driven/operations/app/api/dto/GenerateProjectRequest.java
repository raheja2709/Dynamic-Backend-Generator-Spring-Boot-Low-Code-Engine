package com.user.driven.operations.app.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GenerateProjectRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String packageName;

    private boolean securityEnabled;

    private String securityType;

    private String databaseType;

    private String javaVersion;

    private String springBootVersion;

    @NotEmpty
    private List<EntityRequest> entities;

}
