package br.com.mbrasil.hellow.tasks;

import org.springframework.stereotype.Component;

import br.com.mbrasil.scheduler.annotation.MBScheduled;

@Component
public class DemoTaskHellow {

	@MBScheduled(cron = "0/3 * * * * *", desc = "Description", autoStartup = true)
	public void taskMethodOne() {
		try {
			Thread.sleep(100);
		} catch (Exception e) {

		}
		System.out.println("taskMethod:01");
	}
	
}
