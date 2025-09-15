package com.sakufukai.sales202606.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private List<String> adminEmails;

    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }
}
