package br.com.mbrasil.scheduler.task;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchedulingRunnable implements Runnable {

	private Object bean;
	private String beanName;
	private String methodName;

	public SchedulingRunnable(Object bean, String beanName, String methodName) {
		this.bean = bean;
		this.beanName = beanName;
		this.methodName = methodName;
	}

	@Override
	public void run() {
		try {
			Method method = bean.getClass().getDeclaredMethod(methodName);
			ReflectionUtils.makeAccessible(method);
			method.invoke(bean);
		} catch (Exception e) {
			log.error("Middleware schedule err!", e);
		}
	}

	public String taskId() {
		return beanName + "_" + methodName;
	}

}
