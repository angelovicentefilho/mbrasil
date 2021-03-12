package br.com.mbrasil.scheduler.domain;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExecOrder {

	@JSONField(serialize = false)
	private Object bean;
	private String beanName;
	private String methodName;
	private String desc;
	private String cron;
	private Boolean autoStartup;
}
