package br.com.jtech.starter.redis.schedule.task;

import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;

public interface Task {

	void add(Jedis redis, ConcurrentHashMap<String, Runnable> taskMap, String key, Integer delayTime, TaskParam parameter);
	
	Runnable taskBody(TaskHandler handler, String message);
	
}
