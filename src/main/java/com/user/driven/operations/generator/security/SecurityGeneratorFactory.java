package com.user.driven.operations.generator.security;

import com.user.driven.operations.enums.SecurityType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory that routes to the correct SecurityGenerator implementation
 * based on the configured SecurityType.
 *
 * @author Jatin Raheja
 */
@Component
public class SecurityGeneratorFactory {

    private final Map<SecurityType, SecurityGenerator> map = new EnumMap<>(SecurityType.class);

    public SecurityGeneratorFactory(List<SecurityGenerator> generators) {
        for (SecurityGenerator g : generators) {
            if (g instanceof JwtSecurityGenerator) {
                map.put(SecurityType.JWT, g);
            } else if (g instanceof BasicAuthSecurityGenerator) {
                map.put(SecurityType.BASIC_AUTH, g);
            } else if (g instanceof OAuth2SecurityGenerator) {
                map.put(SecurityType.OAUTH2, g);
            } else if (g instanceof SessionSecurityGenerator) {
                map.put(SecurityType.SESSION_BASED, g);
            }
        }
    }

    /**
     * Returns the security generator for the given type, or null if unsupported.
     *
     * @param type the security type
     * @return the corresponding generator, or null
     */
    public SecurityGenerator get(SecurityType type) {
        return map.get(type);
    }
}
