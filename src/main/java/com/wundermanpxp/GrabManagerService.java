package com.wundermanpxp;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GrabManagerService {

	@Autowired
	private SpiderConfig spiderConfig;

	private ExecutorService executorService;

	private final List<Future<GrabPage>> futures = new ArrayList<>();

	private final Map<Integer, ResponseStats> statusMap = new HashMap<>();

	private int totalRecords;

	private final DecimalFormat df = new DecimalFormat("###.0");

	@PostConstruct
	public void postConstruct() throws IOException, InterruptedException, ExecutionException {
		log.info("----------------------------------------------------------");
		log.info("Working with " + spiderConfig.getMaxConnections() + " threads.");
		log.info("Pause between grabs: " + spiderConfig.getPauseTime() + " ms.");
		executorService = Executors.newFixedThreadPool(spiderConfig.getMaxConnections());
		List<URL> urls = new ArrayList<>();
		for (String sitemapUrl : spiderConfig.getSitemapUrls()) {
			GrabSitemap grabSitemap = new GrabSitemap(spiderConfig, new URL(sitemapUrl));
			grabSitemap.grab().stream().forEach((url) -> {
				urls.add(url);
			});
		}
		totalRecords = urls.size();
		log.info("Total " + totalRecords + " pages.");
		urls.stream().forEach((url) -> {
			submitUrl(url);
		});

		while (checkPageGrabs()) {
		}
		executorService.shutdown();
		log.info("----------------------------------------------------------");
		log.info("Results:");
		statusMap.entrySet().stream().forEach((entry) -> {
			Integer statusCode = entry.getKey();
			ResponseStats stats = entry.getValue();
			log.info("----------------------------------------------");
			log.info("Http response status: " + statusCode);
			log.info("Count: " + stats.getCount());
			log.info("Average response time : " + stats.getAverageResponseTime() + " ms");
			log.info("Longest response time : " + stats.getLongestResponseTime() + " ms");
			log.info("Longest response url : " + stats.getLongestResponseUrl().toString());
			log.info("Shortest response time : " + stats.getShortestResponseTime() + " ms");
			log.info("Shortest response url : " + stats.getShortestResponseUrl().toString());
		});
		log.info("----------------------------------------------");

	}

	private void addToMap(GrabPage page) {
		if (!statusMap.containsKey(page.getStatusCode())) {
			statusMap.put(page.getStatusCode(), new ResponseStats());
		}
		statusMap.get(page.getStatusCode()).addPage(page);
	}

	private boolean checkPageGrabs() throws InterruptedException, ExecutionException {
		Thread.sleep(spiderConfig.getStatsRefreshTime());
		Iterator<Future<GrabPage>> iterator = futures.iterator();
		while (iterator.hasNext()) {
			Future<GrabPage> future = iterator.next();
			if (future.isDone()) {
				addToMap(future.get());
				iterator.remove();
			}
		}
		int count = 0;
		count = statusMap.entrySet().stream().map((entry) -> entry.getValue()).map((value) -> value.getCount()).reduce(count, Integer::sum);
		log.info("----------------------------------------------");
		log.info("Processed " + count + " pages - " + df.format(count * 100.0 / totalRecords) + " %.");
		for (Map.Entry<Integer, ResponseStats> entry : statusMap.entrySet()) {
			Integer key = entry.getKey();
			ResponseStats value = entry.getValue();
			log.info(value.getCount() + " pages with state: " + key + " - " + df.format(value.getCount() * 100.0 / count) + " %.");
		}
		return (futures.size() > 0);
	}

	private void submitUrl(URL url) {

		GrabPage grabPage = new GrabPage(spiderConfig, url);
		Future<GrabPage> future = executorService.submit(grabPage);
		futures.add(future);
	}

}
