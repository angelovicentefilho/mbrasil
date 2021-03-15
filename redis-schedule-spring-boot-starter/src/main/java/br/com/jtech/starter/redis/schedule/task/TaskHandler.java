package br.com.jtech.starter.redis.schedule.task;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.springframework.context.ApplicationContext;

import br.com.jtech.starter.redis.schedule.enums.TaskTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Getter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class TaskHandler {

	private ExecutorService taskPool;

	private JedisPool redisPool;

	private ApplicationContext applicationContext;

	public TaskHandler(ExecutorService service, JedisPool redis, ApplicationContext context) {
		this.taskPool = service;
		this.redisPool = redis;
		this.applicationContext = context;
	}

	private ConcurrentHashMap<String, Runnable> taskMap = new ConcurrentHashMap<>();

	public void add(String key, Integer delay, TaskTypeEnum taskTypeEnum, TaskParam param) {
		Long now = System.currentTimeMillis();
		try (Jedis jedis = redisPool.getResource()) {
			log.info("Add ['{}'] task, key='{}', delayTime='{}', now='{}'", taskTypeEnum.code, key, delay, now);
			jedis.setex(taskTypeEnum.code + "::" + key, delay, "");
			taskTypeEnum.getTaskClass().add(jedis, taskMap, key, delay, param);
		}
	}

	public void remove(String key) {
		try (Jedis jedis = redisPool.getResource()) {
			jedis.del(key);
			this.taskMap.remove(key);
		} catch (Exception e) {
			log.error("Delete key={}, error, Message={}", key, e.getMessage());
			e.printStackTrace();
		}
	}

}
