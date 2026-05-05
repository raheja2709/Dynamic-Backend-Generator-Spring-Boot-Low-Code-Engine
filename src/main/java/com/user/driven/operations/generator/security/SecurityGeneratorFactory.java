package com.user.driven.operations.generator.security;

import com.user.driven.operations.enums.SecurityType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SecurityGeneratorFactory {

    private final Map<SecurityType, SecurityGenerator> map = new HashMap<>();

    public SecurityGeneratorFactory(List<SecurityGenerator> list) {
        for (SecurityGenerator g : list) {
            if (g instanceof JwtSecurityGenerator)
                map.put(SecurityType.JWT, g);
        }
    }

    public SecurityGenerator get(SecurityType type) {
        return map.get(type);
    }
}
