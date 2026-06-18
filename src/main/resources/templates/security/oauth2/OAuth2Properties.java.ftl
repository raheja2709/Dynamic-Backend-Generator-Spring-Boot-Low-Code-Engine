package ${project.packageName}.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * OAuth2 provider configuration properties.
 * Configure in application.properties:
 *
 * spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
 * spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
 * spring.security.oauth2.client.registration.google.scope=openid,profile,email
 *
 * spring.security.oauth2.client.registration.github.client-id=YOUR_CLIENT_ID
 * spring.security.oauth2.client.registration.github.client-secret=YOUR_CLIENT_SECRET
 * spring.security.oauth2.client.registration.github.scope=read:user,user:email
 */
@Configuration
@ConfigurationProperties(prefix = "app.oauth2")
@Getter
@Setter
public class OAuth2Properties {

    private String defaultSuccessUrl = "/api/dashboard";
    private String defaultFailureUrl = "/login?error";
}
