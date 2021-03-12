package br.com.mbrasil.scheduler.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataCollect {

    private int ipCount;
    private int serverCount;
    private int beanCount;
    private int methodCount;
	
}
