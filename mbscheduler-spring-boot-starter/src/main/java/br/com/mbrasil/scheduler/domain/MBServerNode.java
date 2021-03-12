package br.com.mbrasil.scheduler.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MBServerNode {

	private String scheduleServerId;
	private String scheduleServerName;
}
