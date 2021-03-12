package br.com.mbrasil.hellow.crons;

import br.com.mbrasil.scheduler.annotation.MBScheduled;

public class DemoHellowCron {

	@MBScheduled(cron = "0/3 * * * * *", desc = "Describle", autoStartup = true)
	public void runCronOn() {
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		System.out.println(">>> Test");
	}
	
	@MBScheduled(cron = "0/6 * * * * *", desc = "Describle", autoStartup = false)
	public void runCronOff() {
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		System.out.println(">>> Test");
	}
	
}
