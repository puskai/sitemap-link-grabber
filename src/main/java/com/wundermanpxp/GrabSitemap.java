package com.wundermanpxp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/**
 *
 * @author zolika
 */
@Data
@Slf4j
@AllArgsConstructor
public class GrabSitemap {

	private SpiderConfig spiderConfig;

	private URL url;

	public Set<URL> grab() throws IOException {
		Set<URL> urlList = new HashSet<>();
		log.info("Grabbing sitemap : " + url.toString());
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Document document;
		if (!StringUtils.isEmpty(spiderConfig.getUsername()) && !StringUtils.isEmpty(spiderConfig.getPassword())) {
			String login = spiderConfig.getUsername() + ":" + spiderConfig.getPassword();
			String base64login = new String(Base64.getEncoder().encode(login.getBytes()));
			document = Jsoup.connect(url.toString())
							.header("Authorization", "Basic " + base64login)
							.timeout(spiderConfig.getTimeout())
							.get();
		} else {
			document = Jsoup.connect(url.toString())
							.timeout(spiderConfig.getTimeout())
							.get();
		}
		document.select("loc").stream().forEach(tag -> {
			String location = tag.childNode(0).toString();
			if (!StringUtils.isEmpty(spiderConfig.getReplaceHostFrom()) && !StringUtils.isEmpty(spiderConfig.getReplaceHostTo())) {
				location = location.replace(spiderConfig.getReplaceHostFrom(), spiderConfig.getReplaceHostTo());
			}
			log.trace("Found url : " + location);
			try {
				urlList.add(new URL(location));
			} catch (MalformedURLException ex) {
				log.warn("Wrong url : " + url, ex);
			}
		});
		stopWatch.stop();
		log.info("Found " + urlList.size() + " urls in " + stopWatch.getTotalTimeSeconds() + " seconds. (" + url.toString() + ")");
		return urlList;
	}

}
