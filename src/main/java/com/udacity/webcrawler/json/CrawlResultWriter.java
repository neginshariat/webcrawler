package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class CrawlResultWriter {
  private final CrawlResult result;


  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }


  public void write(Path path) throws IOException {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(path);
    // TODO: Fill in this method.

      Writer bufferedWriter= Files.newBufferedWriter(path);
      write(bufferedWriter);
      bufferedWriter.close();

  }

  public void write(Writer writer) {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(writer);
    // TODO: Fill in this method.
    ObjectMapper objectMapper= new ObjectMapper();
    objectMapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    try {
      objectMapper.writeValue(writer, result);
    } catch (Exception e){
      System.out.println("Exception of writing value:" + e);
    }

  }
}
