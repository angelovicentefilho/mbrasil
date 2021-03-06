package br.com.mbrasil.scheduler.service;

import static br.com.mbrasil.scheduler.common.Constants.Global.CHARSET_NAME;
import static br.com.mbrasil.scheduler.common.Constants.Global.LINE;
import static br.com.mbrasil.scheduler.common.Constants.Global.client;
import static br.com.mbrasil.scheduler.common.Constants.Global.path_root_server_ip;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;

import br.com.mbrasil.scheduler.common.Constants;
import br.com.mbrasil.scheduler.domain.ExecOrder;
import br.com.mbrasil.scheduler.task.ScheduledTask;
import br.com.mbrasil.scheduler.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatService {

	private ScheduledExecutorService ses;

	private static class SingletonHolder {
		private static final HeartbeatService INSTANCE = new HeartbeatService();
	}

	private HeartbeatService() {
	}

	public static HeartbeatService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void startFlushScheduleStatus() {
		ses = Executors.newScheduledThreadPool(1);
		ses.scheduleAtFixedRate(() -> {
			try {
				log.info("scheduler heart beat On-Site Inspection task");
				Map<String, ScheduledTask> scheduledTasks = Constants.scheduledTasks;
				Map<String, List<ExecOrder>> execOrderMap = Constants.execOrderMap;
				Set<String> beanNameSet = execOrderMap.keySet();
				for (String beanName : beanNameSet) {
					List<ExecOrder> execOrderList = execOrderMap.get(beanName);
					for (ExecOrder execOrder : execOrderList) {
						String taskId = execOrder.getBeanName() + "_" + execOrder.getMethodName();
						ScheduledTask scheduledTask = scheduledTasks.get(taskId);
						if (null == scheduledTask)
							continue;
						boolean cancelled = scheduledTask.isCancelled();
						String path_root_server_ip_clazz = StrUtil.joinStr(path_root_server_ip, LINE, "clazz", LINE,
								execOrder.getBeanName());
						String path_root_server_ip_clazz_method = StrUtil.joinStr(path_root_server_ip_clazz, LINE,
								"method", LINE, execOrder.getMethodName(), LINE, "value");
						ExecOrder oldExecOrder;
						byte[] bytes = client.getData().forPath(path_root_server_ip_clazz_method);
						if (null != bytes) {
							String oldJson = new String(bytes, CHARSET_NAME);
							oldExecOrder = JSON.parseObject(oldJson, ExecOrder.class);
						} else {
							oldExecOrder = new ExecOrder();
							oldExecOrder.setBeanName(execOrder.getBeanName());
							oldExecOrder.setMethodName(execOrder.getMethodName());
							oldExecOrder.setDesc(execOrder.getDesc());
							oldExecOrder.setCron(execOrder.getCron());
							oldExecOrder.setAutoStartup(execOrder.getAutoStartup());
						}
						oldExecOrder.setAutoStartup(!cancelled);
						if (null == Constants.Global.client.checkExists().forPath(path_root_server_ip_clazz_method))
							continue;
						String newJson = JSON.toJSONString(oldExecOrder);
						Constants.Global.client.setData().forPath(path_root_server_ip_clazz_method,
								newJson.getBytes(CHARSET_NAME));
						String path_root_ip_server_clazz_method_status = StrUtil.joinStr(path_root_server_ip_clazz,
								LINE, "method", LINE, execOrder.getMethodName(), "/status");
						if (null == Constants.Global.client.checkExists()
								.forPath(path_root_ip_server_clazz_method_status))
							continue;
						Constants.Global.client.setData().forPath(path_root_ip_server_clazz_method_status,
								(execOrder.getAutoStartup() ? "1" : "0").getBytes(CHARSET_NAME));
					}
				}
			} catch (Exception ignore) {
			}

		}, 300, 60, TimeUnit.SECONDS);
	}

}
