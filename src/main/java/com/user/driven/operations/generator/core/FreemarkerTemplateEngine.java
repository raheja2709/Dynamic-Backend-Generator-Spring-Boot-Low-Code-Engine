package com.user.driven.operations.generator.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.Map;

@Component
public class FreemarkerTemplateEngine implements TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(FreemarkerTemplateEngine.class);

    private final Configuration config;

    public FreemarkerTemplateEngine() {
        this.config = new Configuration(Configuration.VERSION_2_3_32);
        this.config.setClassForTemplateLoading(this.getClass(), "/templates");
        this.config.setDefaultEncoding("UTF-8");
    }

    @Override
    public String process(String templateName, Map<String, Object> model) {
        try {
            Template template = config.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Template processing failed: {}", templateName, e);
            throw new RuntimeException("Template processing failed: " + templateName, e);
        }
    }
}
