package br.com.jtech.starter.redis.schedule.listener;

import br.com.jtech.starter.redis.schedule.enums.TaskTypeEnum;
import br.com.jtech.starter.redis.schedule.task.Task;
import br.com.jtech.starter.redis.schedule.task.TaskHandler;
import br.com.jtech.starter.redis.schedule.utils.KeyParserUtils;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

@Slf4j
public class KeyExpiredListener extends JedisPubSub {
	private TaskHandler taskHandler;

	public KeyExpiredListener(TaskHandler taskHandler) {
		this.taskHandler = taskHandler;
	}

	@Override
	public void onMessage(String channel, String message) {
		try {
			TaskTypeEnum type = KeyParserUtils.parserKey(message);
			Task task = type.getTaskClass();

			if (task == null) {
				return;
			}

			Runnable runnable = task.taskBody(taskHandler, message);
			taskHandler.getTaskPool().execute(runnable);
		} catch (Exception e) {
			log.info("Schedule task error, key={}, message={}", message, e.getMessage());
			e.printStackTrace();
		} finally {
			taskHandler.remove(message);
		}
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		log.info("Subscribe [{}] success", channel);
	}
}