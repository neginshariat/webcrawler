package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }


  public CrawlerConfiguration load(){
/*    Reader BufferedReader = Files.newBufferedReader(path);
    BufferedReader.close();
    return read(BufferedReader);*/
    try (Reader reader = Files.newBufferedReader(path)) {
      return read(reader);
    } catch (IOException e) {
      System.out.println("IO Exception is:"+ e);
      return null;
    }
  }

  public static CrawlerConfiguration read(Reader reader) {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(reader);
    // TODO: Fill in this method
    ObjectMapper objectMapper= new ObjectMapper();
    objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    try {
      CrawlerConfiguration.Builder readParameter =objectMapper.readValue(reader, CrawlerConfiguration.Builder.class);
      return readParameter.build();
    }catch (Exception e){
      System.out.println("Reader Exception:"+ e);
      return null;
    }
  }
}
