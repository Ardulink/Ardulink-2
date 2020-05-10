/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.core.linkmanager.providers;

import static org.ardulink.core.linkmanager.Classloaders.moduleClassloader;

import java.util.Collection;
import java.util.ServiceLoader;

import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.util.Lists;
import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FactoriesViaServiceLoader implements FactoriesProvider {

	@Override
	@LapsedWith(module = LapsedWith.JDK9, value = "ServiceLoader#stream")
	public Collection<LinkFactory> loadLinkFactories() {
		return Lists.newArrayList(ServiceLoader.load(LinkFactory.class,
				moduleClassloader()).iterator());
	}

}
