package br.com.mbrasil.scheduler.task;

import static java.util.Objects.isNull;

import javax.annotation.Resource;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import br.com.mbrasil.scheduler.common.Constants;

@Component("cronTaskRegister")
public class CronTaskRegister implements DisposableBean {

	@Resource(name = "taskScheduler")
	private TaskScheduler taskScheduler;

	public TaskScheduler getTaskScheduler() {
		return this.taskScheduler;
	}

	public void addCronTask(SchedulingRunnable task, String cronExpression) {
		if (isNull(Constants.scheduledTasks.get(task.taskId()))) {
			removeCronTask(task.taskId());
		}
		CronTask cronTask = new CronTask(task, cronExpression);
		Constants.scheduledTasks.put(task.taskId(), scheduleCronTask(cronTask));
	}

	private ScheduledTask scheduleCronTask(CronTask cronTask) {
		ScheduledTask task = new ScheduledTask();
		task.future = this.taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
		return task;
	}

	public void removeCronTask(String taskId) {
		ScheduledTask task = Constants.scheduledTasks.remove(taskId);
		if (isNull(task)) {
			return;
		}
		task.cancel();
	}

	@Override
	public void destroy() throws Exception {
		for (ScheduledTask task : Constants.scheduledTasks.values()) {
			task.cancel();
		}
		Constants.scheduledTasks.clear();
	}

}
