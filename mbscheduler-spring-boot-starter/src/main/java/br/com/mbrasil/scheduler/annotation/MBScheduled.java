package br.com.mbrasil.scheduler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MBScheduled {

	String desc() default "predifinition";

	String cron() default "";

	boolean autoStartup() default true;

}
