package com.Sehaty.Sehaty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "sehaty")
@Getter
@Setter
public class SubcategoryConfig {

    private Map<String, List<String>> subcategories;

}
