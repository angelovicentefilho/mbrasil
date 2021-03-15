package br.com.jtech.starter.redis.schedule.enums;

import java.util.Objects;

import br.com.jtech.starter.redis.schedule.task.Task;
import br.com.jtech.starter.redis.schedule.task.impl.BeanTask;
import br.com.jtech.starter.redis.schedule.task.impl.SimpleTask;

public enum TaskTypeEnum {

	RUNNABLE("runnable", new SimpleTask()),

	BEAN("bean", new BeanTask()),

	IGNORE("ignore", null),

	PARAM("param", null);

	public String code;

	public Task object;

	TaskTypeEnum(String code, Task task) {
		this.code = code;
		this.object = task;
	}

	public Task getTaskClass() {
		return this.object;
	}

	public static TaskTypeEnum getType(String code) {
		if (Objects.isNull(code) || code.isBlank()) {
			return IGNORE;
		}
		for (TaskTypeEnum task : TaskTypeEnum.class.getEnumConstants()) {
			if (code.equalsIgnoreCase(task.code)) {
				return task;
			}
		}
		return IGNORE;
	}
}
