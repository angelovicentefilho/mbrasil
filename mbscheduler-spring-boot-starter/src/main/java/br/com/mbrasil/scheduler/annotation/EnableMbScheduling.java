package br.com.mbrasil.scheduler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import br.com.mbrasil.scheduler.DoMake;
import br.com.mbrasil.scheduler.config.MBSchedulingConfiguration;
import br.com.mbrasil.scheduler.task.CronTaskRegister;
import br.com.mbrasil.scheduler.task.SchedulingConfig;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Import({ MBSchedulingConfiguration.class })
@ImportAutoConfiguration({ SchedulingConfig.class, CronTaskRegister.class, DoMake.class })
@ComponentScan("br.com.mbbrasil.scheduler.*")
public @interface EnableMbScheduling {

}
