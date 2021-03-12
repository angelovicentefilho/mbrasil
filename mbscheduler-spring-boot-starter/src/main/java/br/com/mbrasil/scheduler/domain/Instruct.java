package br.com.mbrasil.scheduler.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Instruct {

	private String ip;
	private String schedulerServerId;
	private String beanName;
	private String methodName;
	private String cron;
	private Integer status;

}
