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
package org.canedata.module.test.ehcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.logging.LogManager;

import org.canedata.cache.Cache;
import org.canedata.core.cache.StringCacheableWrapped;
import org.canedata.module.ehcache.EhcacheProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Sun Yat-ton
 * @version 1.00.000 2011-8-29
 */
public class TestProvider {
	static EhcacheProvider provider = new EhcacheProvider();
	
	@BeforeClass
	public static void init(){
		LogManager lm = LogManager.getLogManager();
		try {
			lm.readConfiguration(TestProvider.class
					.getResourceAsStream("/logging.properties"));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		provider.setCacheName("sampleCache");
	}
	
	@Test
	public void p(){
		Cache cache = provider.getCache("default");
		assertNotNull(cache);
		
		cache.cache(new StringCacheableWrapped("1", false, "1"));
		
		StringCacheableWrapped c = (StringCacheableWrapped)cache.restore("1");
		assertEquals(c.getContent(), "1");
		
		try {
			Thread.sleep(9999);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertTrue(!cache.isAlive("1"));
		
		
	}
	
	@Test
	public void match(){
		Cache cache = provider.getCache("test:a");
		assertNotNull(cache);
	}
	
}
