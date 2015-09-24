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

import org.zu.ardulink.Link;
import org.zu.ardulink.connection.Connection;

public class ALink {
	
	private String name;
	private boolean defaultLink;
	private String protocolName;
	private String aConnectionName;
	private List<AParameter> connectParameters;
	private int waitSecondsAfterConnection;
	
	private Link link;
	private AConnection aConnection;
	
	@XmlElement(name="name", required=true, nillable=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="defaultLink", defaultValue="false")
	public boolean isDefaultLink() {
		return defaultLink;
	}
	public void setDefaultLink(boolean defaultLink) {
		this.defaultLink = defaultLink;
	}
	
	@XmlElement(name="protocolName")
	public String getProtocolName() {
		return protocolName;
	}
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}
	
	@XmlElement(name="aConnectionName")
	public String getAConnectionName() {
		return aConnectionName;
	}
	public void setAConnectionName(String aConnectionName) {
		this.aConnectionName = aConnectionName;
	}
	
	@XmlElement(name="connectParameters", required=false)
	public List<AParameter> getConnectParameters() {
		return connectParameters;
	}
	public void setConnectParameters(List<AParameter> connectParameters) {
		this.connectParameters = connectParameters;
	}
	
	@XmlElement(name="waitSecondsAfterConnection", required=false)
	public int getWaitSecondsAfterConnection() {
		return waitSecondsAfterConnection;
	}
	public void setWaitSecondsAfterConnection(int waitSecondsAfterConnection) {
		this.waitSecondsAfterConnection = waitSecondsAfterConnection;
	}
	
	// begin business methods
	
	public Link getLink() {
		
		if(link == null) {
			initLink();
		}
		return link;
	}

	private void initLink() {
		if(link == null) {
			if(defaultLink) {
				link = Link.getDefaultInstance();
				name = link.getName();
			} else {
				Connection connection = getConnection();
				link = Link.createInstance(name, protocolName, connection);
			}
		}
	}
	
	private Connection getConnection() {
		if(aConnection == null) {
			aConnection = ConfigurationFacade.getAConnection(aConnectionName);
		}
		return aConnection.getConnection();
	}
	
	// end business methods

}
