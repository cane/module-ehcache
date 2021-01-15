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
	protected org.ehcache.Cache wrapped = null;
	
	public EhcacheCacheAdaptor(org.ehcache.Cache wrap){
		if(null == wrap)
			throw new RuntimeException("Don't get cache instance of Ehcache.");

        if(logger.isDebug())
		    logger.debug("Create Cache instance {0}.", wrap.hashCode());

		wrapped = wrap;
	}
	
	public void cache(Cacheable target) {
        if(logger.isDebug())
            logger.debug("Caching object to cache for {0} ...", String.valueOf(target.getKey()));

		wrapped.put(target.getKey(), target.onCaching());
	}

	public <T> T restore(Object key) {
        if(logger.isDebug())
            logger.debug("Restore object from cache for {0} ...", String.valueOf(key));

		Object e = wrapped.get(key);

		if(null == e) return null;
		
		Cacheable t = (Cacheable)e;
		
		return (T)t.onRestored();
	}

	
	public void remove(Object key) {
		wrapped.remove(key);
	}

    public void removeAll(){
		wrapped.clear();
    }

	public boolean isAlive(Object key) {
        if(logger.isDebug())
            logger.debug("#isAlive, key is {0}, keyInCache is {1}.", key, wrapped.containsKey(key));
		
		if(!wrapped.containsKey(key))
			return false;
		
		Object o = wrapped.get(key);
		if(null == o)
			return false;

		return true;
	}

    public boolean isWrappedFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    public <T> T unwrap(Class<T> iface) {
        return iface.cast(wrapped);
    }
}
