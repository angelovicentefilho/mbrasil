package br.com.jtech.starter.redis.schedule.task.impl;

import java.util.concurrent.ConcurrentHashMap;

import br.com.jtech.starter.redis.schedule.namespace.TaskPrefixNamespace;
import br.com.jtech.starter.redis.schedule.task.Task;
import br.com.jtech.starter.redis.schedule.task.TaskHandler;
import br.com.jtech.starter.redis.schedule.task.TaskParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class SimpleTask implements Task {

	@Override
	public void add(Jedis jedis, ConcurrentHashMap<String, Runnable> taskMap, String key, Integer delayTime,
			TaskParam param) {
		Param param1 = (Param) param;
		taskMap.put(TaskPrefixNamespace.RUNNABLE + key, param1.getRunnable());
	}

	@Override
	public Runnable taskBody(TaskHandler taskHandler, String message) {
		Runnable task = taskHandler.getTaskMap().get(message);
		log.info("Schedule task [{}], time={}", message, System.currentTimeMillis());
		return task;
	}

	@Data
	public static class Param extends TaskParam {
		private Runnable runnable;
	}
}