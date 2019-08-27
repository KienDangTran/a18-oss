package com.a18.common.config;

import com.a18.common.exception.CommonExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.AbstractRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
@EnableAsync
public class AsyncExecutorConfig implements AsyncConfigurer {

  @Bean
  @Override
  public Executor getAsyncExecutor() {
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    ThreadPoolTaskExecutor executor = new ContextAwareTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(500);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("async-");
    executor.initialize();
    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new CommonExceptionHandler();
  }

  public static class ContextAwareTaskExecutor extends ThreadPoolTaskExecutor {
    @Override
    public <T> Future<T> submit(Callable<T> task) {
      return super.submit(new ContextAwareCallable<>(
          task,
          Objects.requireNonNullElse(
              RequestContextHolder.getRequestAttributes(),
              new CustomRequestScopeAttribute()
          )
      ));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
      return super.submitListenable(new ContextAwareCallable<>(
          task,
          Objects.requireNonNullElse(
              RequestContextHolder.getRequestAttributes(),
              new CustomRequestScopeAttribute()
          )
      ));
    }
  }

  public static class ContextAwareTaskScheduler extends ThreadPoolTaskScheduler {
    @Override
    public <T> Future<T> submit(Callable<T> task) {
      return super.submit(new ContextAwareCallable<>(
          task,
          Objects.requireNonNullElse(
              RequestContextHolder.getRequestAttributes(),
              new CustomRequestScopeAttribute()
          )
      ));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
      return super.submitListenable(new ContextAwareCallable<>(
          task,
          Objects.requireNonNullElse(
              RequestContextHolder.getRequestAttributes(),
              new CustomRequestScopeAttribute()
          )
      ));
    }
  }

  public static class ContextAwareCallable<T> implements Callable<T> {
    private Callable<T> task;

    private RequestAttributes context;

    ContextAwareCallable(Callable<T> task, RequestAttributes context) {
      this.task = task;
      this.context = context;
    }

    @Override
    public T call() throws Exception {
      if (context != null) {
        RequestContextHolder.setRequestAttributes(context);
      }

      try {
        return task.call();
      } finally {
        RequestContextHolder.resetRequestAttributes();
      }
    }
  }

  public static class CustomRequestScopeAttribute extends AbstractRequestAttributes {
    private Map<String, Object> requestAttributeMap = new HashMap<>();

    @Override
    public Object getAttribute(String name, int scope) {
      if (scope == RequestAttributes.SCOPE_REQUEST) {
        return this.requestAttributeMap.get(name);
      }
      return null;
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
      if (scope == RequestAttributes.SCOPE_REQUEST) {
        this.requestAttributeMap.put(name, value);
      }
    }

    @Override
    public void removeAttribute(String name, int scope) {
      if (scope == RequestAttributes.SCOPE_REQUEST) {
        this.requestAttributeMap.remove(name);
      }
    }

    @Override
    public String[] getAttributeNames(int scope) {
      if (scope == RequestAttributes.SCOPE_REQUEST) {
        return this.requestAttributeMap.keySet().toArray(new String[0]);
      }
      return new String[0];
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope) {
      // Not Supported
    }

    @Override
    public Object resolveReference(String key) {
      // Not supported
      return null;
    }

    @Override
    public String getSessionId() {
      return null;
    }

    @Override
    public Object getSessionMutex() {
      return null;
    }

    @Override protected void updateAccessedSessionAttributes() {
    }
  }
}