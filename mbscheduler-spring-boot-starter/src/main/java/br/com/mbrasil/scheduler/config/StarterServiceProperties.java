package br.com.mbrasil.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("mbrasil.scheduler")
@Getter
@Setter
public class StarterServiceProperties {

	private String zkAddress;
	private String scheduleServerId;
	private String scheduleServerName;
}
