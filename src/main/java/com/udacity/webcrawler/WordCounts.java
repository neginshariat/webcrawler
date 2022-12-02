package com.udacity.webcrawler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class that sorts the map of word counts.
 *
 * <p>TODO: Reimplement the sort() method using only the Stream API and lambdas and/or method
 *          references.
 */
final class WordCounts {

  static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {


    WordCountComparator wordCountComparator = new WordCountComparator();
    return wordCounts.entrySet().stream()
            .sorted(wordCountComparator)
            .filter(Objects::nonNull)
            .limit(Math.min(popularWordCount, wordCounts.size())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> k, LinkedHashMap::new));
  }

  private static final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
      if (!a.getValue().equals(b.getValue())) {
        return b.getValue() - a.getValue();
      }
      if (a.getKey().length() != b.getKey().length()) {
        return b.getKey().length() - a.getKey().length();
      }
      return a.getKey().compareTo(b.getKey());
    }
  }

  private WordCounts() {
    // This class cannot be instantiated
  }
}