package com.wundermanpxp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties
public class SpiderConfig {

	private int timeout;

	private String sitemapUrl;

	private String replaceHostFrom;

	private String replaceHostTo;

	private int maxConnections;

	private int pauseTime;

	private boolean followRedirects;

	private String username;

	private String password;
}
