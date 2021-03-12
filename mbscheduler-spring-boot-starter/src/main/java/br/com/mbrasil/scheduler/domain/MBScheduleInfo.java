package br.com.mbrasil.scheduler.domain;

import lombok.Data;

@Data
public class MBScheduleInfo {

	private String ip;
	private String schedulerServerId;
	private String schedulerServerName;
	private String beanName;
	private String methodName;
	private String desc;
	private String cron;
	private Integer status;

}
