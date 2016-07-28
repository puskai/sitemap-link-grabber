# sitemap-link-grabber

This application grabs every url from a list sitemap.xml-s.
It is based on [ScrumBucket tutorial](http://scrumbucket.org/tutorials/neo4j-site-crawler/).
It uses Spring boot, you can set application settings in the application.properties or by command line arguments.
We are using it grab every page from a site in order the pages to be cached.
The application logs grab stats every <statsRefreshTime> seconds:

		2016-07-28 03:29:35.058  ...      : Processed 2825 pages - 34.5 %.
		2016-07-28 03:29:35.058  ...      : 2825 pages with state: 200 - 100.0 %.

and some stats:

		2016-07-27 12:44:50.434  ...      : Total:
		2016-07-27 12:44:50.439  ...      : ----------------------------------------------------------
		2016-07-27 12:44:50.439  ...      : Http response status: 200
		2016-07-27 12:44:50.439  ...      : Count: 65
		2016-07-27 12:44:50.439  ...      : Average response time : 286 ms
		2016-07-27 12:44:50.439  ...      : Longest response time : 398 ms
		2016-07-27 12:44:50.439  ...      : Longest response url : <longestResponseUrl>
		2016-07-27 12:44:50.439  ...      : Shortest response time : 203 ms
		2016-07-27 12:44:50.439  ...      : Shortest response url : <shortestResponseUrl>

## Run the Sample

* You need Java 8 to run this sample, because it is based on Lambdas, Future, java.util.concurrency, ...
* You can run from your IDE like any maven project
* or from the command line:

java -jar:

		$ java -jar target/sitemap-spider-0.0.1-SNAPSHOT.jar --<command line argument=value>

command line arguments:

		username - username for basic authentication - if left empty, no basic authentication is used (default:<empty>)
		password - password for basic authentication (default:<empty>)
		timeout -  timeout in ms when we grab a page (default:6000)
		sitemapUrls- comma separated list of sitemap urls (default: http://index.hu/sitemap/cikkek_1999.xml)
		maxConnections- how many threads should run (default: 10)
		pauseTime - pause in ms after a page was grabbed by a thread in order the decrease stress on the server - (default:0)
		followRedirects - grabber should follow redirects - (default:false)
		replaceHostFrom - if we need to grab the pages from another host (integration, ...), this string from the urls will be replaced (default:<empty>)
		replaceHostTo - the new host, if replaceHostFrom is used (default:<empty>)
		statsRefreshTime - time period in ms to display the intermediate logs (default:2000)
		solrUrls - comma separated list of solr urls - ex: http://localhost:8983/solr/collection1 (default:<empty>)

### Remarks
This app is an internal tool, has no enough test coverage, there will not be further improvements.


