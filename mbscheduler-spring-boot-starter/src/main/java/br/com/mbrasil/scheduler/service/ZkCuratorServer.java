package br.com.mbrasil.scheduler.service;

import static br.com.mbrasil.scheduler.common.Constants.Global.CHARSET_NAME;
import static br.com.mbrasil.scheduler.common.Constants.Global.LINE;
import static br.com.mbrasil.scheduler.common.Constants.Global.path_root;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSON;

import br.com.mbrasil.scheduler.common.Constants;
import br.com.mbrasil.scheduler.domain.Instruct;
import br.com.mbrasil.scheduler.task.CronTaskRegister;
import br.com.mbrasil.scheduler.task.SchedulingRunnable;
import br.com.mbrasil.scheduler.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZkCuratorServer {

	public static CuratorFramework getClient(String connectString) {
		if (null != Constants.Global.client)
			return Constants.Global.client;
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
		client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
			switch (connectionState) {
			case CONNECTED:
				log.info("schedule init server connected {}", connectString);
				break;
			case RECONNECTED:
				break;
			default:
				break;
			}
		});
		client.start();
		Constants.Global.client = client;
		return client;
	}

	public static void addTreeCacheListener(final ApplicationContext applicationContext, final CuratorFramework client,
			String path) throws Exception {
		TreeCache treeCache = new TreeCache(client, path);
		treeCache.start();
		treeCache.getListenable().addListener((curatorFramework, event) -> {
			if (null == event.getData())
				return;
			byte[] eventData = event.getData().getData();
			if (null == eventData || eventData.length < 1)
				return;
			String json = new String(eventData, CHARSET_NAME);
			if ("".equals(json) || json.indexOf("{") != 0 || json.lastIndexOf("}") + 1 != json.length())
				return;
			Instruct instruct = JSON.parseObject(new String(event.getData().getData(), CHARSET_NAME),
					Instruct.class);
			switch (event.getType()) {
			case NODE_ADDED:
			case NODE_UPDATED:
				if (Constants.Global.ip.equals(instruct.getIp())
						&& Constants.Global.schedulerServerId.equals(instruct.getSchedulerServerId())) {
					CronTaskRegister cronTaskRegistrar = applicationContext
							.getBean("cronTaskRegister", CronTaskRegister.class);
					boolean isExist = applicationContext.containsBean(instruct.getBeanName());
					if (!isExist)
						return;
					Object scheduleBean = applicationContext.getBean(instruct.getBeanName());
					String path_root_server_ip_clazz_method_status = StrUtil.joinStr(path_root, Constants.Global.LINE,
							"server", Constants.Global.LINE, instruct.getSchedulerServerId(), Constants.Global.LINE,
							"ip", LINE, instruct.getIp(), LINE, "clazz", LINE, instruct.getBeanName(), LINE, "method",
							LINE, instruct.getMethodName(), "/status");
					Integer status = instruct.getStatus();
					switch (status) {
					case 0:
						cronTaskRegistrar.removeCronTask(instruct.getBeanName() + "_" + instruct.getMethodName());
						setData(client, path_root_server_ip_clazz_method_status, "0");
						log.info("schedule task stop {} {}", instruct.getBeanName(), instruct.getMethodName());
						break;
					case 1:
						cronTaskRegistrar.addCronTask(
								new SchedulingRunnable(scheduleBean, instruct.getBeanName(), instruct.getMethodName()),
								instruct.getCron());
						setData(client, path_root_server_ip_clazz_method_status, "1");
						log.info("schedule task start {} {}", instruct.getBeanName(), instruct.getMethodName());
						break;
					case 2:
						cronTaskRegistrar.removeCronTask(instruct.getBeanName() + "_" + instruct.getMethodName());
						cronTaskRegistrar.addCronTask(
								new SchedulingRunnable(scheduleBean, instruct.getBeanName(), instruct.getMethodName()),
								instruct.getCron());
						setData(client, path_root_server_ip_clazz_method_status, "1");
						log.info("schedule task refresh {} {}", instruct.getBeanName(), instruct.getMethodName());
						break;
					}
				}
				break;
			case NODE_REMOVED:
				break;
			default:
				break;
			}
		});
	}

	public static void createNode(CuratorFramework client, String path) throws Exception {
		List<String> pathChild = new ArrayList<>();
		pathChild.add(path);
		while (path.lastIndexOf(Constants.Global.LINE) > 0) {
			path = path.substring(0, path.lastIndexOf(Constants.Global.LINE));
			pathChild.add(path);
		}
		for (int i = pathChild.size() - 1; i >= 0; i--) {
			Stat stat = client.checkExists().forPath(pathChild.get(i));
			if (null == stat) {
				client.create().creatingParentsIfNeeded().forPath(pathChild.get(i));
			}
		}
	}

	public static void createNodeSimple(CuratorFramework client, String path) throws Exception {
		if (null == client.checkExists().forPath(path)) {
			client.create().creatingParentsIfNeeded().forPath(path);
		}
	}

	public static void deleteNodeSimple(CuratorFramework client, String path) throws Exception {
		if (null != client.checkExists().forPath(path)) {
			client.delete().deletingChildrenIfNeeded().forPath(path);
		}
	}

	public static void setData(CuratorFramework client, String path, String data) throws Exception {
		if (null == client.checkExists().forPath(path))
			return;
		client.setData().forPath(path, data.getBytes(CHARSET_NAME));
	}

	public static byte[] getData(CuratorFramework client, String path) throws Exception {
		return client.getData().forPath(path);
	}

	public static void deleteDataRetainNode(CuratorFramework client, String path) throws Exception {
		if (null != client.checkExists().forPath(path)) {
			client.delete().forPath(path);
		}
	}

	public static void appendPersistentData(CuratorFramework client, String path, String data) throws Exception {
		PersistentEphemeralNode node = new PersistentEphemeralNode(client, PersistentEphemeralNode.Mode.EPHEMERAL, path,
				data.getBytes(CHARSET_NAME));
		node.start();
		node.waitForInitialCreate(3, TimeUnit.SECONDS);
	}

	public static void deletingChildrenIfNeeded(CuratorFramework client, String path) throws Exception {
		if (null == client.checkExists().forPath(path))
			return;
		client.delete().deletingChildrenIfNeeded().forPath(path);
	}
}
