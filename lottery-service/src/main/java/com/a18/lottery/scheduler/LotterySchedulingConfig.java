package com.a18.lottery.scheduler;

import com.a18.common.config.AsyncExecutorConfig;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class LotterySchedulingConfig {

  @Bean(destroyMethod = "shutdown")
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler =
        new AsyncExecutorConfig.ContextAwareTaskScheduler();
    taskScheduler.setPoolSize(10);
    taskScheduler.setThreadNamePrefix("lottery-");
    taskScheduler.initialize();
    return taskScheduler;
  }

  @Bean
  @ConditionalOnMissingBean(name = "rabbitListenerContainerFactory")
  @ConditionalOnProperty(prefix = "spring.rabbitmq.listener", name = "type", havingValue = "direct")
  public DirectRabbitListenerContainerFactory rabbitListenerContainerFactory(
      final ConnectionFactory connectionFactory
  ) {
    ThreadPoolTaskExecutor taskExecutor = new AsyncExecutorConfig.ContextAwareTaskExecutor();
    taskExecutor.setCorePoolSize(10);
    taskExecutor.setMaxPoolSize(500);
    taskExecutor.setQueueCapacity(500);
    taskExecutor.setThreadNamePrefix("rabbitExecutor-");
    taskExecutor.initialize();
    final DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setTaskExecutor(taskExecutor);
    factory.setTaskScheduler(this.threadPoolTaskScheduler());
    return factory;
  }
}