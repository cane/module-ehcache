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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.canedata.cache.Cache;
import org.canedata.cache.CacheProvider;
import org.canedata.core.logging.LoggerFactory;
import org.canedata.core.util.StringUtils;
import org.canedata.logging.Logger;

/**
 * Allow Cache Name using regular expressions.
 * 
 * @author Sun Yat-ton
 * @version 1.00.000 2011-8-29
 */
public class EhcacheProvider implements CacheProvider {
	private static final Logger logger = LoggerFactory
			.getLogger(EhcacheProvider.class);
	private final Map<String, Cache> caches = Collections
			.synchronizedMap(new HashMap<String, Cache>(1));

	protected String configFile = null;
	private String cacheName = "default";
	private static final String VENDOR = "Cane team";
	private static final String NAME = "Cache provider for ehcache";
	private final Map<String, Object> extras = new HashMap<String, Object>();

	protected static net.sf.ehcache.CacheManager CACHE_MANAGER;
	
	public EhcacheProvider(){
		
	}
	
	public EhcacheProvider(String cacheName){
		setCacheName(cacheName);
	}
	
	public EhcacheProvider(String cacheName, String config){
		setCacheName(cacheName);
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

	public Cache getCache(String schema) {
		return getCache(schema, null);
	}

	public Cache getCache(String schema, Map<String, Object> strategies) {
        if(logger.isDebug())
            logger.debug(
				"Get cache from EhCacheManager, schema {0} ignored use {1}.",
				schema, getCacheName());

		//schema entity.factory.name:entity.schema:entity.name
		if (strategies != null && !strategies.isEmpty())
			extras.putAll(strategies);

		Cache cache = caches.get(schema);
		if (null == cache) {
			net.sf.ehcache.Cache ehcache = getManager().getCache(matchCacheName(schema));

			if (null == ehcache)
				throw new RuntimeException("Don't get Ehcache from "
						+ getConfigFile() + " by " + getCacheName() + ".");

			cache = new EhcacheCacheAdaptor(ehcache);
			
			caches.put(schema, cache);
		}

		return cache;
	}

	private String matchCacheName(String schema){
		if(StringUtils.isBlank(schema)) return getCacheName();
		
		String[] ns = getManager().getCacheNames();
		for(String n:ns){
			if(schema.matches(n)){
                if(logger.isDebug())
				    logger.debug("Schema {0} matched cache named {1}.", schema, n);
				
				return n;
			}
		}

        if(logger.isDebug())
		    logger.debug("Schema {0} not matched cache, use default cache {1}.", schema, getCacheName());

		return getCacheName();
	}
	
	private CacheManager getManager() {
		if (null == CACHE_MANAGER) {
			if (StringUtils.isBlank(getConfigFile())) {
                CACHE_MANAGER = CacheManager.create(getClass()
						.getClassLoader().getResource("ehcache.xml"));
			} else {
                CACHE_MANAGER = CacheManager.create(getClass()
						.getClassLoader().getResource(getConfigFile()));
			}
		}

		return CACHE_MANAGER;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

}
