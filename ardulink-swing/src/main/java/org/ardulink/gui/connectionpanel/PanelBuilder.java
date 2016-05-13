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
package org.ardulink.gui.connectionpanel;

import java.net.URI;

import javax.swing.JPanel;

import org.ardulink.core.linkmanager.LinkManager.Configurer;

public interface PanelBuilder {

	/**
	 * Query if this PanelBuilder can create Panels for the passed URI.
	 * 
	 * @param uri
	 *            the URI to create a Panel for
	 * @return <code>true</code> if this PanelBuilder can create a Panel for the
	 *         passed URI
	 */
	boolean canHandle(URI uri);

	/**
	 * Creates a Panel that should intercate with the passed Configurer.
	 * 
	 * @param configurer
	 *            Configurer that was created for the supported URI
	 * @return a newly created Panel with components that can modify the
	 *         attributes found in the passed Configurer
	 */
	JPanel createPanel(Configurer configurer);

}