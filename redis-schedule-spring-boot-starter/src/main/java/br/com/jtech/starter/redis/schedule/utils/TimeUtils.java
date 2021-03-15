package br.com.jtech.starter.redis.schedule.utils;

import java.util.Date;

public final class TimeUtils {

	private TimeUtils() {}
	
	public static int deltaTime(Date now, Date time) {
		Long timeNumber = time.getTime() / 1000;
		Long nowNumber = now.getTime() / 1000;
		return Integer.parseInt("" + (timeNumber - nowNumber));
	}

}
