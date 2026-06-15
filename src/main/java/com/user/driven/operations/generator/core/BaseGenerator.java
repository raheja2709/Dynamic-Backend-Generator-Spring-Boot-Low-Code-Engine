package com.user.driven.operations.generator.core;

import java.nio.file.Path;
import java.util.Map;

public abstract class BaseGenerator {

    protected final TemplateEngine engine;
    protected final FileWriterService writer;

    protected BaseGenerator(TemplateEngine engine, FileWriterService writer) {
        this.engine = engine;
        this.writer = writer;
    }

    protected void generate(String template, Map<String, Object> model, Path path) {
        String content = engine.process(template, model);
        writer.write(path, content);
    }

}