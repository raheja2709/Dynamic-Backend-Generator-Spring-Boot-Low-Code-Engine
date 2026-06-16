package ${project.packageName};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<#if project.cachingEnabled>
import org.springframework.cache.annotation.EnableCaching;
</#if>

@SpringBootApplication
<#if project.cachingEnabled>
@EnableCaching
</#if>
public class ${applicationClassName} {

    public static void main(String[] args) {
        SpringApplication.run(${applicationClassName}.class, args);
    }
}