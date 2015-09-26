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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.zu.ardulink.mail.server.contentmanagement.IContentManager;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ACommand {
	
	private String name;
	private List<String> mailContentHooks;
	private List<String> values;
	private String className;
	private List<String> aLinkNames;
	
	private IContentManager contentManager = null;
	
	@XmlElement(name="name", required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="mailContentHooks")
	public List<String> getMailContentHooks() {
		return mailContentHooks;
	}
	public void setMailContentHooks(List<String> mailContentHooks) {
		this.mailContentHooks = mailContentHooks;
	}
	
	@XmlElement(name="values")
	public List<String> getValues() {
		return values;
	}
	public void setValues(List<String> values) {
		this.values = values;
	}
	
	@XmlElement(name="className")
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}

	@XmlElement(name="aLinkNames")
	public List<String> getALinkNames() {
		return aLinkNames;
	}
	public void setALinkNames(List<String> aLinkNames) {
		this.aLinkNames = aLinkNames;
	}
	
	
	// begin business methods
	
	public boolean isForContent(String content) {
		
		return getContentManager().isForContent(content, mailContentHooks);
	}
	
	private IContentManager getContentManager() {
		
		if(contentManager == null) {
			try {
				contentManager = (IContentManager)this.getClass().getClassLoader().loadClass(className).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return contentManager;
	}
	
	public String execute(String content) {
		return getContentManager().execute(content, values, mailContentHooks, aLinkNames);
	}

	// end business methods
	
}
