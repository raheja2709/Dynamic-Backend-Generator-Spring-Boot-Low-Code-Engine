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
public class ${project.name?replace(" ", "")?cap_first}Application {

    public static void main(String[] args) {
        SpringApplication.run(${project.name?replace(" ", "")?cap_first}Application.class, args);
    }
}