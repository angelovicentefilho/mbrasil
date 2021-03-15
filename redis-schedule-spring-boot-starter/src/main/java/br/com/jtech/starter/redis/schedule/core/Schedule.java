package br.com.jtech.starter.redis.schedule.core;

import java.util.concurrent.ExecutorService;

import org.springframework.context.ApplicationContext;

import br.com.jtech.starter.redis.schedule.enums.TaskTypeEnum;
import br.com.jtech.starter.redis.schedule.listener.KeyExpiredListener;
import br.com.jtech.starter.redis.schedule.task.TaskHandler;
import br.com.jtech.starter.redis.schedule.task.TaskParam;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
public class Schedule {
    private TaskHandler taskHandler;

    public Schedule(ExecutorService executorService, JedisPool jedisPool, ApplicationContext applicationContext) {
        if (executorService == null || jedisPool == null) {
            throw new NullPointerException();
        }
        this.taskHandler = new TaskHandler(executorService, jedisPool, applicationContext);
        start();
    }

    private void start() {
        log.info("Starting Schedule Module...");
        JedisPool jedisPool = taskHandler.getRedisPool();
        Jedis jedis = jedisPool.getResource();
        log.info("Redis loading success...");

        jedis.configSet("notify-keyspace-events", "Ex");

        log.info("Starting subscribe expired key...");
        Runnable runnable = () -> jedis.subscribe(new KeyExpiredListener(taskHandler), "__keyevent@0__:expired");
        Thread subThread = new Thread(runnable);
        subThread.start();
        log.info("Schedule started!");
    }

    public void add(String key, Integer delayTime, TaskTypeEnum taskTypeEnum, TaskParam param) {
        this.taskHandler.add(key, delayTime, taskTypeEnum, param);
    }

    public void remove(String key) {
        this.taskHandler.remove(key);
    }
}