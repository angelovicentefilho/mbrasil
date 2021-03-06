package br.com.mbrasil.scheduler.task;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import br.com.mbrasil.scheduler.common.Constants;

@Configuration("schedulingConfig")
public class SchedulingConfig {

	@Bean("taskScheduler")
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(Constants.Global.schedulePoolSize);
		taskScheduler.setRemoveOnCancelPolicy(true);
		taskScheduler.setThreadNamePrefix("MBrasilMiddlewareScheduleThreadPool-");
		return taskScheduler;
	}

}
