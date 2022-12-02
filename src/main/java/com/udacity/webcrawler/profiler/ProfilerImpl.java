package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Handler;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }


  private boolean isProfiledValues(Class<?> Klass){
  boolean profiledValue= Arrays.stream(Klass.getDeclaredMethods()).anyMatch(
          result -> result.getAnnotation(Profiled.class) != null);
    return profiledValue;
  }
  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    if (!isProfiledValues(klass)) {
      throw new IllegalArgumentException("Not Profiled");
    }

    ProfilingMethodInterceptor profilingMethodInterceptor= new ProfilingMethodInterceptor(state,delegate,clock);
    Object proxy = Proxy.newProxyInstance(
            klass.getClassLoader(),
            new Class<?>[] {klass},
            profilingMethodInterceptor);
    return (T) proxy;


  }

  @Override
  public void writeData(Path path) {
    Objects.requireNonNull(path);
    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)){
      writeData(bufferedWriter);
    }catch (IOException exception){
      exception.getMessage();
    }

  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
