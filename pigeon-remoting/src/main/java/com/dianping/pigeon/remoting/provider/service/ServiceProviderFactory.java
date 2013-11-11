/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.util.IpUtils;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public final class ServiceProviderFactory {

	private static ConcurrentHashMap<String, Object> services = new ConcurrentHashMap<String, Object>();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ServiceProviderFactory() {
	}

	public static void addServices(Map<String, Object> services, int port) throws ServiceException {
		for (String serviceName : services.keySet()) {
			addService(serviceName, services.get(serviceName), port);
		}
	}

	public static void addService(String serviceName, Object service, int port) throws ServiceException {
		if (!services.containsKey(serviceName)) {
			try {
				String autoRegister = configManager.getProperty(Constants.KEY_AUTO_REGISTER,
						Constants.DEFAULT_AUTO_REGISTER);
				if ("true".equalsIgnoreCase(autoRegister)) {
					String localip = RegistryManager.getInstance().getProperty(Constants.KEY_LOCAL_IP);
					if (localip == null || localip.length() == 0) {
						localip = IpUtils.getFirstLocalIp();
					}
					String serviceAddress = localip + ":" + port;
					String group = configManager.getProperty(Constants.KEY_GROUP, Constants.DEFAULT_GROUP);
					String weight = configManager.getProperty(Constants.KEY_WEIGHT, Constants.DEFAULT_WEIGHT);
					int intWeight = Integer.parseInt(weight);
					RegistryManager.getInstance().publishService(serviceName, group, serviceAddress, intWeight);
				}
			} catch (Exception e) {
				throw new ServiceException("", e);
			}
			services.putIfAbsent(serviceName, service);
		}
	}

	public static Object getService(String serviceName) {
		return services.get(serviceName);
	}

	public static Collection<String> getServiceNames() {
		return services.keySet();
	}

	public static boolean exists(String serviceName) {
		return services.containsKey(serviceName);
	}

	public static Map<String, Object> getAllServices() {
		return services;
	}

}