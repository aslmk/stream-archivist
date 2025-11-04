package com.aslmk.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "user.cookie")
@Getter
@Setter
public class CookieProperties {
    private String name;
    private String path;
    private boolean httpOnly;
    private boolean secure;
    private String domain;
    private int maxAge;
}
