package ${project.packageName}.dto;

import lombok.*;

/**
 * Summary DTO for ${entity.name?cap_first} - contains only id and a display label.
 * Used by the SUMMARY DTO strategy when referencing this entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ${entity.name?cap_first}SummaryDto {

    private Long id;
    private String ${labelField!"name"};
}
