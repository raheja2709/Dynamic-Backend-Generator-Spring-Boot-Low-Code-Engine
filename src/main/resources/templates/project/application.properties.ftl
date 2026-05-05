# Database Configuration
<#if project.databaseType == "H2">
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
<#elseif project.databaseType == "POSTGRESQL">
spring.datasource.url=jdbc:postgresql://localhost:5432/${project.name?lower_case?replace(" ", "_")}
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Fix for PostgreSQL CLOB issue
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
<#elseif project.databaseType == "MYSQL">
spring.datasource.url=jdbc:mysql://localhost:3306/${project.name?lower_case?replace(" ", "_")}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# MySQL specific properties
spring.jpa.properties.hibernate.dialect.storage_engine=innodb
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
</#if>

# JPA Configuration
spring.jpa.hibernate.ddl-auto=<#if project.databaseType == "H2">create-drop<#else>update</#if>
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Server Configuration
server.port=8080

<#if project.swaggerEnabled>
# Swagger Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
</#if>

<#if project.securityEnabled>
# Security Configuration
<#if project.securityType == "JWT">
# JWT Configuration
app.jwtSecret=mySecretKey
app.jwtExpirationMs=86400000

<#elseif project.securityType == "OAUTH2">
# OAuth2 Configuration
app.security.oauth2.enabled=true
app.base-url=http://localhost:8080

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret

# GitHub OAuth2
spring.security.oauth2.client.registration.github.client-id=your-github-client-id
spring.security.oauth2.client.registration.github.client-secret=your-github-client-secret

<#elseif project.securityType == "SESSION_BASED">
# Session Configuration
server.servlet.session.timeout=30m
server.servlet.session.cookie.max-age=1800
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false

<#elseif project.securityType == "BASIC_AUTH">
# Basic Auth Configuration
spring.security.user.name=admin
spring.security.user.password=password
spring.security.user.roles=ADMIN

</#if>

# LDAP Configuration (if needed)
app.security.ldap.enabled=false
app.ldap.url=ldap://localhost:389
app.ldap.base=dc=example,dc=com
app.ldap.username=cn=admin,dc=example,dc=com
app.ldap.password=admin
app.ldap.user-search-base=ou=people
app.ldap.user-search-filter=(uid={0})
app.ldap.group-search-base=ou=groups
app.ldap.group-search-filter=(member={0})
</#if>

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Generated Projects Directory
app.generated-projects.directory=./generated-projects

# Logging Configuration
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Security Headers
server.servlet.session.cookie.same-site=strict
