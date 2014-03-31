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

import net.sf.ehcache.Element;

import org.canedata.cache.Cache;
import org.canedata.cache.Cacheable;
import org.canedata.core.logging.LoggerFactory;
import org.canedata.logging.Logger;

/**
 * 
 * @author Sun Yat-ton
 * @version 1.00.000 2011-8-29
 */
public class EhcacheCacheAdaptor implements Cache {
	protected Logger logger = LoggerFactory.getLogger(EhcacheCacheAdaptor.class);
	protected net.sf.ehcache.Cache wrapped = null;
	
	public EhcacheCacheAdaptor(net.sf.ehcache.Cache wrap){
		if(null == wrap)
			throw new RuntimeException("Don't get cache instance of Ehcache.");

        if(logger.isDebug())
		    logger.debug("Create Cache instance {0}.", wrap.hashCode());
		
		wrapped = wrap;
	}
	
	public void cache(Cacheable target) {
		wrapped.put(new Element(target.getKey(), target.onCaching()));
	}

	public <T> T restore(Object key) {
		Element e = wrapped.get(key);

		if(null == e) return null;
		
		Cacheable t = (Cacheable)e.getObjectValue();
		
		return (T)t.onRestored();
	}

	
	public void remove(Object key) {
		wrapped.remove(key);
	}

	public boolean isAlive(Object key) {
        if(logger.isDebug())
            logger.debug("#isAlive, key is {0}, keyInCache is {1}.", key, wrapped.isKeyInCache(key));
		
		if(!wrapped.isKeyInCache(key))
			return false;
		
		Element o = wrapped.get(key);
		if(null == o)
			return false;
		
		return !o.isExpired();
	}

}
