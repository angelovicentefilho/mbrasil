package br.com.mbrasil.scheduler.config;

import static br.com.mbrasil.scheduler.common.Constants.Global.LINE;
import static br.com.mbrasil.scheduler.common.Constants.Global.client;
import static br.com.mbrasil.scheduler.common.Constants.Global.path_root;
import static br.com.mbrasil.scheduler.common.Constants.Global.path_root_server;
import static br.com.mbrasil.scheduler.common.Constants.Global.path_root_server_ip;
import static br.com.mbrasil.scheduler.common.Constants.Global.schedulerServerId;
import static br.com.mbrasil.scheduler.common.Constants.Global.schedulerServerName;
import static java.util.Objects.isNull;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.alibaba.fastjson.JSON;

import br.com.mbrasil.scheduler.annotation.MBScheduled;
import br.com.mbrasil.scheduler.common.Constants;
import br.com.mbrasil.scheduler.domain.ExecOrder;
import br.com.mbrasil.scheduler.service.HeartbeatService;
import br.com.mbrasil.scheduler.service.ZkCuratorServer;
import br.com.mbrasil.scheduler.task.CronTaskRegister;
import br.com.mbrasil.scheduler.task.SchedulingRunnable;
import br.com.mbrasil.scheduler.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MBSchedulingConfiguration
		implements ApplicationContextAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

	private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Constants.Global.applicationContext = applicationContext;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
		if (this.nonAnnotatedClasses.contains(targetClass))
			return bean;
		Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
		if (isNull(methods)) {
			return bean;
		}
		for (Method method : methods) {
			MBScheduled annoScheduled = AnnotationUtils.findAnnotation(method, MBScheduled.class);
			if (isNull(annoScheduled) || isEmpty(method)) {
				continue;
			}
			List<ExecOrder> execOrderList = Constants.execOrderMap.computeIfAbsent(beanName, k -> new ArrayList<>());
			ExecOrder execOrder = new ExecOrder();
			execOrder.setBean(bean);
			execOrder.setBeanName(beanName);
			execOrder.setMethodName(method.getName());
			execOrder.setDesc(annoScheduled.desc());
			execOrder.setCron(annoScheduled.cron());
			execOrder.setAutoStartup(annoScheduled.autoStartup());
			execOrderList.add(execOrder);
			this.nonAnnotatedClasses.add(targetClass);
		}
		return bean;
	}

	private boolean isEmpty(Method method) {
		return 0 == method.getDeclaredAnnotations().length;
	}
	

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
            initConfig(applicationContext);
            initServer(applicationContext);
            initTask(applicationContext);
            initNode();
            HeartbeatService.getInstance().startFlushScheduleStatus();
            log.info("schedule init config???server???task???node???heart done!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initConfig(ApplicationContext applicationContext) {
        try {
            StarterServiceProperties properties = applicationContext.getBean("starterAutoConfig", StarterAutoConfig.class).getProperties();
            Constants.Global.zkAddress = properties.getZkAddress();
            Constants.Global.schedulerServerId = properties.getScheduleServerId();
            Constants.Global.schedulerServerName = properties.getScheduleServerName();
            InetAddress id = InetAddress.getLocalHost();
            Constants.Global.ip = id.getHostAddress();
        } catch (Exception e) {
            log.error("schedule init config error???", e);
            throw new RuntimeException(e);
        }
    }

    private void initServer(ApplicationContext applicationContext) {
        try {
            CuratorFramework client = ZkCuratorServer.getClient(Constants.Global.zkAddress);
            path_root_server = StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId);
            path_root_server_ip = StrUtil.joinStr(path_root_server, LINE, "ip", LINE, Constants.Global.ip);
            ZkCuratorServer.deletingChildrenIfNeeded(client, path_root_server_ip);
            ZkCuratorServer.createNode(client, path_root_server_ip);
            ZkCuratorServer.setData(client, path_root_server, schedulerServerName);
            ZkCuratorServer.createNodeSimple(client, Constants.Global.path_root_exec);
            ZkCuratorServer.addTreeCacheListener(applicationContext, client, Constants.Global.path_root_exec);
        } catch (Exception e) {
            log.error("schedule init server error???", e);
            throw new RuntimeException(e);
        }
    }

    private void initTask(ApplicationContext applicationContext) {
        CronTaskRegister cronTaskRegistrar = applicationContext.getBean("cronTaskRegister", CronTaskRegister.class);
        Set<String> beanNames = Constants.execOrderMap.keySet();
        for (String beanName : beanNames) {
            List<ExecOrder> execOrderList = Constants.execOrderMap.get(beanName);
            for (ExecOrder execOrder : execOrderList) {
                if (!execOrder.getAutoStartup()) continue;
                SchedulingRunnable task = new SchedulingRunnable(execOrder.getBean(), execOrder.getBeanName(), execOrder.getMethodName());
                cronTaskRegistrar.addCronTask(task, execOrder.getCron());
            }
        }
    }

    private void initNode() throws Exception {
        Set<String> beanNames = Constants.execOrderMap.keySet();
        for (String beanName : beanNames) {
            List<ExecOrder> execOrderList = Constants.execOrderMap.get(beanName);
            for (ExecOrder execOrder : execOrderList) {
                String path_root_server_ip_clazz = StrUtil.joinStr(path_root_server_ip, LINE, "clazz", LINE, execOrder.getBeanName());
                String path_root_server_ip_clazz_method = StrUtil.joinStr(path_root_server_ip_clazz, LINE, "method", LINE, execOrder.getMethodName());
                String path_root_server_ip_clazz_method_status = StrUtil.joinStr(path_root_server_ip_clazz, LINE, "method", LINE, execOrder.getMethodName(), "/status");
                ZkCuratorServer.createNodeSimple(client, path_root_server_ip_clazz);
                ZkCuratorServer.createNodeSimple(client, path_root_server_ip_clazz_method);
                ZkCuratorServer.createNodeSimple(client, path_root_server_ip_clazz_method_status);
                ZkCuratorServer.appendPersistentData(client, path_root_server_ip_clazz_method + "/value", JSON.toJSONString(execOrder));
                ZkCuratorServer.setData(client, path_root_server_ip_clazz_method_status, execOrder.getAutoStartup() ? "1" : "0");
            }
        }
    }

}
