package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final ProfilingState profilingState;
  private final Object objectChecker;
  private final Clock clock;

  ProfilingMethodInterceptor(ProfilingState profilingState, Object objectChecker, Clock clock) {
    this.profilingState = profilingState;
    this.objectChecker = objectChecker;
    this.clock = clock;
  }


  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (method.getAnnotation(Profiled.class).equals(Object.class)) {
      final Instant start = clock.instant();
      try {
        method.invoke(objectChecker, args);
      } catch (InvocationTargetException invocationTargetException) {
        throw invocationTargetException.getTargetException();
      } catch (IllegalAccessException illegalAccessException) {
        throw new RuntimeException(illegalAccessException);
      } finally {
        profilingState.record(objectChecker.getClass(), method, Duration.between(start, clock.instant()));
      }
    }
    return method.invoke(objectChecker,args);
  }
}
