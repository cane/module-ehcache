/*
 * Copyright (c) 2013 CaneData.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.canedata.module.ehcache;

import org.canedata.cache.Cache;
import org.canedata.cache.CacheProvider;
import org.canedata.cache.Cacheable;
import org.canedata.core.logging.LoggerFactory;
import org.canedata.core.util.StringUtils;
import org.canedata.logging.Logger;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.event.EventType;
import org.ehcache.xml.XmlConfiguration;

import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allow Cache Name using regular expressions.
 * 
 * @author Sun Yat-ton
 * @version 1.00.000 2011-8-29
 */
public class EhcacheProvider implements CacheProvider {
	private static final Logger logger = LoggerFactory
			.getLogger(EhcacheProvider.class);
	private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>(1);

	protected String configFile = null;
	private String defaultCacheName = "default";
	private static final String VENDOR = "Cane team";
	private static final String NAME = "Cache provider for ehcache";
	private final Map<String, Object> extras = new HashMap<String, Object>();

	protected org.ehcache.CacheManager cacheManager;
	
	public EhcacheProvider(){
		
	}
	
	public EhcacheProvider(String cacheName){
		setDefaultCacheName(cacheName);
	}

	public EhcacheProvider(String defaultCacheName, String config){
		setDefaultCacheName(defaultCacheName);
		setConfigFile(config);
	}

	public String getName() {
		return NAME;
	}

	public String getVendor() {
		return VENDOR;
	}

	public int getVersion() {
		return 1;
	}

	public Map<String, Object> getExtras() {
		return extras;
	}

	public Object getExtra(String key) {
		return extras.get(key);
	}

	@Override
	public EhcacheProvider setExtra(String key, Object val) {
		extras.put(key, val);

		return this;
	}

	public Cache getCache(String schema) {
		return getCache(schema, null);
	}

	private java.util.concurrent.locks.ReentrantLock lock = new ReentrantLock();
	public Cache getCache(String schema, Map<String, Object> strategies) {
		//schema entity.factory.name:entity.schema:entity.name
		if (strategies != null && !strategies.isEmpty())
			extras.putAll(strategies);

		String _schema = matchCacheName(schema);
		//MongoEntityFactory.this.getName().concat(":")
		//							.concat(getSchema()).concat(":").concat(getName())

		if(logger.isDebug())
			logger.debug(
					"Getting cache from EhCacheManager, expected cache is {0}, matched to {1}  ...",
					schema, _schema);

		lock.lock();
		Cache cache = caches.get(_schema);
		if (null == cache) {
			org.ehcache.Cache ehcache = getManager().getCache(_schema, String.class, Object.class);

			if (null == ehcache)
				throw new RuntimeException("Don't get Ehcache from "
						+ getConfigFile() + " by " + getDefaultCacheName() + ".");

			cache = new EhcacheCacheAdaptor(ehcache);
			
			caches.put(_schema, cache);
		}
		lock.unlock();

		return cache;
	}

	private Set<String> cacheNames = null;
	private String matchCacheName(String schema){
		if(StringUtils.isBlank(schema)) return getDefaultCacheName();
		if (cacheNames == null)
			cacheNames = getManager().getRuntimeConfiguration().getCacheConfigurations().keySet();

		if(cacheNames.contains(schema)) {
			Optional<String> n = cacheNames.stream().filter(i -> {
				return schema.equals(i);
			}).findFirst();
			if(n.isPresent())
				return n.get();
		}


        if(logger.isDebug())
		    logger.debug("Schema {0} not matched cache, use default cache {1}.", schema, getDefaultCacheName());

		return getDefaultCacheName();
	}
	
	private CacheManager getManager() {
		if (null == cacheManager) {
			URL myUrl = null;
			if (StringUtils.isBlank(getConfigFile())) {
				myUrl = getClass().getClassLoader().getResource("ehcache.xml");

			} else {
				myUrl = getClass()
						.getClassLoader().getResource(getConfigFile());
			}

			Configuration xmlConfig = new XmlConfiguration(myUrl);
			cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
			cacheManager.init();
		}

		return cacheManager;
	}

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public String getDefaultCacheName() {
		return defaultCacheName;
	}

	public void setDefaultCacheName(String name) {
		this.defaultCacheName = name;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
}
