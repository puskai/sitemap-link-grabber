package com.wundermanpxp;

import java.net.URL;
import java.util.Base64;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

@Data
@Slf4j
public class GrabPage implements Callable<GrabPage> {

	private SpiderConfig spiderConfig;
	private URL url;
	private int statusCode;
	private long responseTime;
	private String body;

	public GrabPage(SpiderConfig spiderConfig, URL url) {
		this.spiderConfig = spiderConfig;
		this.url = url;
	}

	@Override
	public GrabPage call() throws Exception {
		log.debug("Grabbing page: " + url.toString());
		StopWatch stopWatch = new StopWatch();
		StopWatch responseStopWatch = new StopWatch();
		stopWatch.start();
		Connection.Response response = null;
		try {
			responseStopWatch.start();

			if (!StringUtils.isEmpty(spiderConfig.getUsername()) && !StringUtils.isEmpty(spiderConfig.getPassword())) {
				String login = spiderConfig.getUsername() + ":" + spiderConfig.getPassword();
				String base64login = new String(Base64.getEncoder().encode(login.getBytes()));
				response = Jsoup.connect(url.toString())
								.header("Authorization", "Basic " + base64login)
								.timeout(spiderConfig.getTimeout())
								.followRedirects(spiderConfig.isFollowRedirects())
								.execute();
			} else {
				response = Jsoup.connect(url.toString())
								.timeout(spiderConfig.getTimeout())
								.followRedirects(spiderConfig.isFollowRedirects())
								.execute();
			}
			body = response.body();
			responseStopWatch.stop();
			//System.out.println(response.body().substring(0, 100));
			statusCode = response.statusCode();

		} catch (HttpStatusException e) {
			responseStopWatch.stop();
			statusCode = e.getStatusCode();
			log.debug("Failed to grab : " + url.toString(), e);
		} catch (Exception e) {
			responseStopWatch.stop();
			log.warn("Failed to grab : " + url.toString(), e);
		}
		responseTime = responseStopWatch.getTotalTimeMillis();
		log.debug("Status code : " + statusCode + " for page: " + url.toString());
		stopWatch.stop();
		log.trace("Page " + url.toString() + " processed in " + stopWatch.getTotalTimeSeconds() + " seconds");

		if (spiderConfig.getPauseTime() > 0) {
			log.trace("Waiting for  " + spiderConfig.getPauseTime() + " ms - not to stress the server");
			Thread.sleep(spiderConfig.getPauseTime());
		}
		return this;
	}
}
