package com.wundermanpxp;

import java.net.URL;
import lombok.Data;

/**
 *
 * @author zolika
 */
@Data
public class ResponseStats {

	private int count = 0;
	private long averageResponseTime;
	private long totalResponseTime = 0;
	private long longestResponseTime = 0;
	private URL longestResponseUrl;
	private long shortestResponseTime = Long.MAX_VALUE;
	private URL shortestResponseUrl;

	public void addPage(GrabPage page) {
		count++;
		long pageResponseTime = page.getResponseTime();
		if (pageResponseTime >= longestResponseTime) {
			longestResponseTime = pageResponseTime;
			longestResponseUrl = page.getUrl();
		}
		if (pageResponseTime <= shortestResponseTime) {
			shortestResponseTime = pageResponseTime;
			shortestResponseUrl = page.getUrl();
		}
		totalResponseTime += pageResponseTime;
		averageResponseTime = totalResponseTime / (long) count;
	}

}
