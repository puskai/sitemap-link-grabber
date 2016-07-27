package com.wundermanpxp;

import java.io.IOException;
import java.net.URL;
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

	@Autowired
	private GrabSitemap grabSitemap;

	private ExecutorService executorService;

	private final List<Future<GrabPage>> futures = new ArrayList<>();

	private final Map<Integer, ResponseStats> statusMap = new HashMap<>();

	@PostConstruct
	public void postConstruct() throws IOException, InterruptedException, ExecutionException {
		executorService = Executors.newFixedThreadPool(spiderConfig.getMaxConnections());
		grabSitemap.grab().stream().forEach((url) -> {
			submitUrl(url);
		});
		while (checkPageGrabs()) {
		}
		executorService.shutdown();
		log.info("Total:");
		statusMap.entrySet().stream().forEach((entry) -> {
			Integer statusCode = entry.getKey();
			ResponseStats stats = entry.getValue();
			log.info("----------------------------------------------------------");
			log.info("Http response status: " + statusCode);
			log.info("Count: " + stats.getCount());
			log.info("Average response time : " + stats.getAverageResponseTime() + " ms");
			log.info("Longest response time : " + stats.getLongestResponseTime() + " ms");
			log.info("Longest response url : " + stats.getLongestResponseUrl().toString());
			log.info("Shortest response time : " + stats.getShortestResponseTime() + " ms");
			log.info("Shortest response url : " + stats.getShortestResponseUrl().toString());
		});

	}

	private void addToMap(GrabPage page) {
		if (!statusMap.containsKey(page.getStatusCode())) {
			statusMap.put(page.getStatusCode(), new ResponseStats());
		}
		statusMap.get(page.getStatusCode()).addPage(page);
	}

	private boolean checkPageGrabs() throws InterruptedException, ExecutionException {
		Thread.sleep(1000);
		Iterator<Future<GrabPage>> iterator = futures.iterator();
		while (iterator.hasNext()) {
			Future<GrabPage> future = iterator.next();
			if (future.isDone()) {
				addToMap(future.get());
				iterator.remove();
			}
		}
		return (futures.size() > 0);
	}

	private void submitUrl(URL url) {

		GrabPage grabPage = new GrabPage(spiderConfig, url);
		Future<GrabPage> future = executorService.submit(grabPage);
		futures.add(future);
	}

}
