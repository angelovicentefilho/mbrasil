package br.com.mbrasil.scheduler.task;

import java.util.concurrent.ScheduledFuture;

public class ScheduledTask {

	volatile ScheduledFuture<?> future;

	public void cancel() {
		ScheduledFuture<?> future = this.future;
		if (future == null) {
			return;
		}
		future.cancel(true);
	}

	public boolean isCancelled() {
		ScheduledFuture<?> future = this.future;
		if (future == null) {
			return true;
		}
		return future.isCancelled();
	}

}
