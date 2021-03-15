package br.com.jtech.starter.redis.schedule.task.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import br.com.jtech.starter.redis.schedule.enums.TaskTypeEnum;
import br.com.jtech.starter.redis.schedule.namespace.TaskPrefixNamespace;
import br.com.jtech.starter.redis.schedule.task.Task;
import br.com.jtech.starter.redis.schedule.task.TaskHandler;
import br.com.jtech.starter.redis.schedule.task.TaskParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class BeanTask implements Task {
    private final static String SPLIT_STRING = "::";

    @Override
    public void add(Jedis jedis, ConcurrentHashMap<String, Runnable> taskMap, String key, Integer delayTime, TaskParam param) {
        Param beanParam = (Param) param;
        String paramKey = TaskPrefixNamespace.PARAM + key;
        int oneDayLater = delayTime + 3600 * 24;
        log.info("bean task [{}] param expired time={}", key, oneDayLater);
        jedis.setex(paramKey, oneDayLater, JSONObject.toJSONString(beanParam));
    }

    @Override
    public Runnable taskBody(TaskHandler taskHandler, String message) {
        try {
            Jedis jedis = taskHandler.getRedisPool().getResource();

            String paramKey = getParamKey(message);
            String paramValue = jedis.get(paramKey);
            Param beanTaskDTO = getScheduleParam(paramValue);

            jedis.del(paramKey);
            jedis.close();

            Object beanObject = taskHandler.getApplicationContext().getBean(beanTaskDTO.getBean());
            Method method = beanObject.getClass()
                    .getMethod(beanTaskDTO.getMethod(), getParamsClass(beanTaskDTO.getParam()));

            log.info("Schedule task [{}], time={}", message, System.currentTimeMillis());
            return () -> {
                try {
                    method.invoke(beanObject, getParamsValue(beanTaskDTO.getParam()));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Data
    public static class Param extends TaskParam {
        private String bean;
        private String method;
        private List<Object> param;
    }

    private String getParamKey(String message) {
        String[] keyArr = message.split(SPLIT_STRING);
        keyArr[0] = TaskTypeEnum.PARAM.code;

        return String.join(SPLIT_STRING, keyArr);
    }

    private Param getScheduleParam(String value) {
        return JSONObject.parseObject(value, Param.class);
    }

    private Class[] getParamsClass(List<Object> params) {
        if (params == null || params.isEmpty()) {
            return new Class[]{};
        }

        return params.stream()
                .map(Object::getClass)
                .collect(Collectors.toList())
                .toArray(new Class[]{});
    }

    private Object[] getParamsValue(List<Object> params) {
        if (params == null || params.isEmpty()) {
            return new Class[]{};
        }
        return params.toArray();
    }
}