package com.user.driven.operations.generator.core;

import java.util.Map;

public interface TemplateEngine {
    String process(String template, Map<String, Object> model);
}
