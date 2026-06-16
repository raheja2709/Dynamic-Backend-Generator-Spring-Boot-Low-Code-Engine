package com.user.driven.operations.generator.module;

import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Component
public class EntityModuleGenerator extends BaseGenerator {

    public EntityModuleGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    public void generate(ProjectDefinition project, EntityDefinition entity, Path basePath) {
        log.info("Starting entity generation for entity={}",
                entity.getName());

        String pkg = project.getPackageName().replace(".", "/");
        log.info("Resolved package path={}", pkg);
        Map<String, Object> model = Map.of(
                "project", project,
                "entity", entity
        );

        Path base = basePath.resolve("src/main/java/" + pkg);
        log.info("Resolved base source path={}",
                base);
        generate("entity/Entity.java.ftl", model,
                base.resolve("model/" + entity.getName() + ".java"));

        generate("entity/Repository.java.ftl", model,
                base.resolve("repository/" + entity.getName() + "Repository.java"));

        generate("entity/Service.java.ftl", model,
                base.resolve("service/" + entity.getName() + "Service.java"));

        generate("entity/ServiceImpl.java.ftl", model,
                base.resolve("service/impl/" + entity.getName() + "ServiceImpl.java"));

        generate("entity/Controller.java.ftl", model,
                base.resolve("controller/" + entity.getName() + "Controller.java"));

        generate("entity/Dto.java.ftl", model,
                base.resolve("dto/" + entity.getName() + "Dto.java"));
    }
}