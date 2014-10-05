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

package org.zu.ardulink.connection.bluetooth;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.zu.ardulink.connection.serial.AbstractSerialConnection;

public class BluetoothConnection extends AbstractSerialConnection {

	private Object lock = new Object();
	private ArdulinkDiscoveryListener listener = new ArdulinkDiscoveryListener(this, lock);

	private Map<String, ServiceRecord> ports = new HashMap<String, ServiceRecord>();

	private javax.microedition.io.Connection connection = null;
	private StreamConnectionNotifier streamConnNotifier = null;
	private StreamConnection streamConnection = null;

	//read string from spp client
	private InputStream inputStream = null;
	private OutputStream outputStream = null;

	@Override
	public List<String> getPortList() {
        LocalDevice localDevice;
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
        	throw new RuntimeException(e);
		}

		DiscoveryAgent agent = localDevice.getDiscoveryAgent();
		
		listener.reset();
        
        try {
            agent.startInquiry(DiscoveryAgent.GIAC, listener);
            synchronized(lock){
                lock.wait();
            }
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }            
   
        UUID[] uuidSet = new UUID[1];
        uuidSet[0]=new UUID(0x1101); // Serial Port Service
        
        int[] attrIDs =  new int[] {
               0x0100 // Service name
        };
        
        for (RemoteDevice device : listener.getDevices()) {
            
            
            try {
                agent.searchServices(attrIDs, uuidSet, device, listener);
                synchronized(lock){
                    lock.wait();
                }
            }
            catch (Exception e) {
            	throw new RuntimeException(e);
            }
        }
        
		return setToList(ports.keySet());
	}

	private <T> List<T> setToList(Set<T> set) {
		List<T> retvalue = new ArrayList<T>(set.size());
		Iterator<T> iterator = set.iterator();
		while (iterator.hasNext()) {
			T item = (T) iterator.next();
			retvalue.add(item);
		}
		return retvalue;
	}
	
	@Override
	public boolean connect(Object... params) {
		String deviceName = null;
		if(params == null || params.length != 1) {
			throw new RuntimeException("This connection accepts just a String device name.");
		}
		if(!(params[0] instanceof String)) {
			throw new RuntimeException("This connection accepts a just a String parameter");
		} else {
			deviceName =(String)params[0]; 
		}

		boolean retvalue = false;
		retvalue = connect(deviceName);
		return retvalue;
	}

	public boolean connect(String deviceName) {
		boolean retvalue = false;
		try {
			ServiceRecord serviceRecord = ports.get(deviceName);
			if(serviceRecord == null) {
				writeLog("The connection could not be made. Device not discovered");
			} else {
				String url = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
				if(url == null) {
					writeLog("The connection could not be made. Connection url not found");
				} else {
					connection = Connector.open(url);
					if(connection instanceof StreamConnectionNotifier) {
						streamConnNotifier = (StreamConnectionNotifier)connection;
						streamConnection = streamConnNotifier.acceptAndOpen();
					} else if(connection instanceof StreamConnection) {
						streamConnNotifier = null;
						streamConnection = (StreamConnection)connection;
					} else {
						throw new Exception("Connection class not known. " + connection.getClass().getCanonicalName());
					}

					//read string from spp client
					inputStream = streamConnection.openInputStream();
					outputStream = streamConnection.openOutputStream();
					setInputStream(inputStream);
					setOutputStream(outputStream);

					startReader();
					writeLog("connection on " + deviceName + " established");
					getContact().connected(getId(), deviceName);
					setConnected(true);
					retvalue = true;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			writeLog("The connection could not be made." + e.getMessage());
		}
		return retvalue;
	}
	
	@Override
	public boolean disconnect() {
		try {
			if(isConnected()) {
				stopReader();
				inputStream.close();
				outputStream.close();
				streamConnection.close();
				if(streamConnNotifier != null) {
					streamConnNotifier.close();	
				}
				setConnected(false);
			}
			getContact().disconnected(getId());
			writeLog("connection closed");
		}
		catch(Exception e) {
			e.printStackTrace();
			writeLog("disconnection fails");
		}
		finally {
			inputStream = null;
			outputStream = null;
			streamConnection = null;
			streamConnNotifier = null;
			connection = null;
		}
		return !isConnected();
	}
	
	public void setPorts(Map<String, ServiceRecord> ports) {
		this.ports = ports;
	}
}
