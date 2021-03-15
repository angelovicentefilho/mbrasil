package br.com.jtech.starter.redis.schedule.config;

import java.util.concurrent.Executors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.jtech.starter.redis.schedule.core.Schedule;
import redis.clients.jedis.JedisPool;

@Configuration
@ConditionalOnClass(RedisProperties.class)
@EnableConfigurationProperties(RedisProperties.class)
public class ScheduleAutoConfigure implements ApplicationContextAware {

	@Autowired
	private JedisPool jedisPool;

	private ApplicationContext applicationContext;

	@Bean
	@ConditionalOnMissingBean
	public Schedule schedule() {
		return new Schedule(Executors.newCachedThreadPool(), jedisPool, applicationContext);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}