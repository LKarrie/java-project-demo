package com.psbc.actuator.v2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties("management.metrics.web.tags.uri")
@Component
@Data
public class CustomMetricsProperties {
	protected boolean enabled = false;
	protected String defaultName = "/unmonitored";
	protected final Set monitored = new LinkedHashSet();
}
