/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
 */

package org.zu.ardulink.mail.server.links.configuration;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.annotation.XmlElement;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.convenience.Links;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 *         [adsense]
 *
 */
public class ALink {

	private String name;
	private boolean defaultLink;

	private Link link;

	@XmlElement(name = "name", required = true, nillable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "defaultLink", defaultValue = "false")
	public boolean isDefaultLink() {
		return defaultLink;
	}

	public void setDefaultLink(boolean defaultLink) {
		this.defaultLink = defaultLink;
	}

	// begin business methods

	public Link getLink() {
		if (link == null) {
			try {
				Configurer configurer = getConfigurer();
				if (configurer.getAttributes().contains("qos")) {
					ConfigAttribute qos = configurer.getAttribute("qos");
					qos.setValue(true);
				}
				link = Links.getLink(configurer);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return link;
	}

	private Configurer getConfigurer() {
		try {
			if (defaultLink) {
				name = "default";
				return Links.getDefaultConfigurer();
			} else {
				return LinkManager.getInstance().getConfigurer(new URI(name));
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// end business methods

}
