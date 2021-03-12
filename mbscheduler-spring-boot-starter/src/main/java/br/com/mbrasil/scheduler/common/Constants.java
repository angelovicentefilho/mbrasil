package br.com.mbrasil.scheduler.common;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationContext;

import br.com.mbrasil.scheduler.domain.ExecOrder;
import br.com.mbrasil.scheduler.task.ScheduledTask;

public class Constants {

	public static final Map<String, List<ExecOrder>> execOrderMap = new ConcurrentHashMap<>();
	public static final Map<String, ScheduledTask> scheduledTasks = new ConcurrentHashMap<>(16);

	public static class Global {
		public static ApplicationContext applicationContext;
		public static final String LINE = "/";
		public static String CHARSET_NAME = "utf-8";
		public static int schedulePoolSize = 8;
		public static String ip;
		public static String zkAddress;
		public static String schedulerServerId;
		public static String schedulerServerName;
		public static CuratorFramework client;
		public static String path_root = "/br/com/mbrasil/scheduler";
		public static String path_root_exec = path_root + "/exec";
		public static String path_root_server;
		public static String path_root_server_ip;
		public static String path_root_server_ip_clazz;
		public static String path_root_server_ip_clazz_method;
		public static String path_root_server_ip_clazz_method_status;
	}

	public static class InstructStatus {
		public final static Integer stop = 0;
		public final static Integer start = 1;
		public final static Integer refresh = 2;
	}

}
