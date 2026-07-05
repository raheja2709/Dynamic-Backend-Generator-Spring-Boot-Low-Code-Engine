package com.user.driven.operations.app.api.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class OperationRequest {
    private String operationType;
    private boolean enabled = true;
}
