package br.com.jtech.starter.redis.schedule.utils;

import br.com.jtech.starter.redis.schedule.enums.TaskTypeEnum;

public class KeyParserUtils {

	  private final static String SPLIT_STRING = "::";

	    public static TaskTypeEnum parserKey (String message) {
	        String[] keyArr = message.split(SPLIT_STRING);
	        if (keyArr.length <= 1) {
	            return TaskTypeEnum.IGNORE;
	        }

	        return TaskTypeEnum.getType(keyArr[0]);
	    }
	
}
