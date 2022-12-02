package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.udacity.webcrawler.parser.PageParserFactory;


public class ParallelWebCrawler implements WebCrawler {

    private final Clock clock;
    private final Duration timeout;
    private final int popularWordCount;
    private final ForkJoinPool pool;
    private final List<Pattern> ignoredUrls;
    private final int maxDepth;
    private final PageParserFactory parserFactory;


  @Inject
  public ParallelWebCrawler(
          Clock clock,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount,
          @IgnoredUrls List<Pattern> ignoredUrls,
          @MaxDepth int maxDepth,
          PageParserFactory parserFactory) {
      this.clock = clock;
      this.timeout = timeout;
      this.popularWordCount = popularWordCount;
      this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
      this.ignoredUrls = ignoredUrls;
      this.maxDepth = maxDepth;
      this.parserFactory = parserFactory;

  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {

      Instant deadline = clock.instant().plus(timeout);
      Map<String, Integer> counts = Collections.synchronizedMap(new HashMap<>());
      Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
      for (String url : startingUrls) {
          pool.invoke(new CrawResultlInternal(url, deadline, maxDepth, counts, visitedUrls));
      }

      if (counts.isEmpty()) {
          return new CrawlResult.Builder()
                  .setWordCounts(counts)
                  .setUrlsVisited(visitedUrls.size())
                  .build();
      }

      return new CrawlResult.Builder()
              .setWordCounts(WordCounts.sort(counts, popularWordCount))
              .setUrlsVisited(visitedUrls.size())
              .build();
  }
  private class CrawResultlInternal extends RecursiveAction{
     private String url;
     private Instant deadLine;
     private int maxDepth;
     private Map<String, Integer> counts;
     private Set<String> visitedUrls;


    public CrawResultlInternal(String url, Instant deadLine, int maxDepth, Map<String, Integer> counts, Set<String> visitedUrls) {
        this.url = url;
        this.deadLine = deadLine;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
    }
    @Override
    protected void compute() {
     if (maxDepth == 0 || clock.instant().isAfter(deadLine)) {
        return;
      }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
/*        List<Pattern> patterns=  ignoredUrls.stream()
                .filter(pattern -> pattern.matcher(url).matches())
                .collect(Collectors.toList());*/

        // i tried to write this piece with Stream but it doesn't work. It would be nice if you wrote if for me in feedback. Thanks
        if (!visitedUrls.add(url)) {
            return;
        }

      PageParser.Result result = parserFactory.get(url).parse();
      for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
        if (counts.containsKey(e.getKey())) {
          counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
        } else {
          counts.put(e.getKey(), e.getValue());
        }
      }

        List<CrawResultlInternal> tasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            tasks.add(new CrawResultlInternal(link, deadLine, maxDepth - 1, counts, visitedUrls));
        }
        invokeAll(tasks);
    }

  }


  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
