-- Copyright 2013 project Ardulink http://www.ardulink.org/
 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
 
--     http://www.apache.org/licenses/LICENSE-2.0
 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-- This init.lua can be used to start a NodeMCU ready to receive/send
-- mqtt Ardulink messages on the right topics.
-- Use mqtt link with default topic prefix (or specify another one)
-- and with separatedTopics=true

-- The script is tested with a NodeMCU with the following configuration
-- custom build by frightanic.com
-- modules: file,gpio,mqtt,net,node,pwm,tmr,uart,wifi
-- build 2017-08-20 14:24

-- Actually this script sets an access point, however you can decomment
-- the following lines and comment the lines about access point, in order
-- to connect NomeMCU to an existent access point

--- WIFI AS CLIENT
-----------------------------------------------
--- Set Variables ---
-----------------------------------------------
--- WIFI CONFIGURATION ---
-- WIFI_CONFIG={}
-- WIFI_CONFIG.ssid = "joker"
-- WIFI_CONFIG.pwd = "avengers"
-- WIFI_SIGNAL_MODE = wifi.PHYMODE_N


--- IP CONFIG (Leave blank to use DHCP) ---
-- ESP8266_IP=""
-- ESP8266_NETMASK=""
-- ESP8266_GATEWAY=""
-----------------------------------------------

--- Connect to the wifi network ---
-- wifi.setmode(wifi.STATION) 
-- wifi.setphymode(WIFI_SIGNAL_MODE)
-- wifi.sta.config(WIFI_CONFIG) 
-- wifi.sta.connect()

-- if ESP8266_IP ~= "" then
--     wifi.sta.setip({ip=ESP8266_IP,netmask=ESP8266_NETMASK,gateway=ESP8266_GATEWAY})
-- end

-----------------------------------------------

--- Check the IP Address ---
-- print(wifi.sta.getip())
--- WIFI AS CLIENT END


-- WIFI AS ACCESS POINT
---------------------------------------
--- Set Variables ---
---------------------------------------
--- Set AP Configuration Variables ---
AP_CFG={}
--- SSID: 1-32 chars (set it as you wish)
AP_CFG.ssid="Ardulink"
--- Password: 8-64 chars. Minimum 8 Chars (set it as you wish)
AP_CFG.pwd="ardulink"
--- Authentication: AUTH_OPEN, AUTH_WPA_PSK, AUTH_WPA2_PSK, AUTH_WPA_WPA2_PSK
AP_CFG.auth=AUTH_OPEN
--- Channel: Range 1-14
AP_CFG.channel = 6
--- Hidden Network? True: 1, False: 0
AP_CFG.hidden = 0
--- Max Connections: Range 1-4
AP_CFG.max=4
--- WiFi Beacon: Range 100-60000
AP_CFG.beacon=100

--- Set AP IP Configuration Variables ---
AP_IP_CFG={}
AP_IP_CFG.ip="192.168.10.1"
AP_IP_CFG.netmask="255.255.255.0"
AP_IP_CFG.gateway="192.168.10.1"

--- Set AP DHCP Configuration Variables ---
--- There is no support for defining last DHCP IP ---
AP_DHCP_CFG={}
AP_DHCP_CFG.start="192.168.10.2"
---------------------------------------

--- Configure ESP8266 into AP Mode ---
wifi.setmode(wifi.SOFTAP)
--- Configure 802.11n Standard ---
wifi.setphymode(wifi.PHYMODE_N)

--- Configure WiFi Network Settings ---
wifi.ap.config(AP_CFG)
--- Configure AP IP Address ---
wifi.ap.setip(AP_IP_CFG)

--- Configure DHCP Service ---
wifi.ap.dhcp.config(AP_DHCP_CFG)
--- Start DHCP Service ---
wifi.ap.dhcp.start()
---------------------------------------
-- WIFI AS ACCESS POINT END

-- MQTT
MQTTBROKER={}
MQTTBROKER.ip="192.168.10.2"
MQTTBROKER.port=1883
m = mqtt.Client("NodeMCU-ID", 120);
m:on("message", function(client, topic, data)
    print("Message received on topic: " .. topic .. " Data: " .. data)

    -- if receive a not start listening mqtt topic
    if(string.find(topic, "system/listening") == nil) then
        -- if it is a set topic
        if(string.find(topic, "value/get") == nill) then
            pintype = string.sub(topic, 23, 23)
            pin = string.sub(topic, 24, -11)
            print("PINTYPE: " .. pintype)
            print("PIN:     " .. pin)
            if(pintype == "D") then
                pwm.close(pin)
                if(data == "true") then
                    print("DATA: HIGH")
                    gpio.mode(pin,gpio.OUTPUT) gpio.write(pin,gpio.HIGH)
                else
                    print("DATA: LOW");
                    gpio.mode(pin,gpio.OUTPUT) gpio.write(pin,gpio.LOW)
                end
            elseif(pintype == "A") then
                print("DATA: " .. data);
                -- comment the following row with the emulator since it hangs...
                pwm.setup(pin,1000,1023) pwm.start(pin) pwm.setduty(pin,data)
            else
                print("Message unknown");
            end
        end
    else
    -- it is  start/stop listening topic
        pintype = string.sub(topic, 40, 40)
        pin = string.sub(topic, 41, -11)
        print("PINTYPE: " .. pintype)
        print("PIN:     " .. pin)
        if(pintype == "D") then
            -- start/stop for digital
            if(data == "true") then
                print("Start Listening")
                topic = string.format("home/devices/ardulink/D%s/value/get",pin)
                local function pinCb(level)
                    print(string.format("Pin state changed publish event for %s", topic))
                    print("LEVEL: " .. level)
                    if(level == gpio.HIGH) then
                        m:publish(topic, "true", 0, 0)
                    else
                        m:publish(topic, "false", 0, 0)
                    end
                end
                gpio.mode(pin,gpio.INT)
                gpio.trig(pin,"both",pinCb)
            else
                print("Stop Listening")
                gpio.mode(pin,gpio.OUTPUT)
            end
        else
            print("Not supported")
        end
    end
end)

m:on("connect", function(client)

    -- it subscribes to topics like:
    -- home/devices/ardulink/D0/value/set
    -- home/devices/ardulink/A0/value/set
    -- home/devices/ardulink/system/listening/D0/value/set
    -- where D0 is digital PIN 0 and A0 is analog PIN 0
    m:subscribe("home/devices/ardulink/#", 0);
end)

m:connect(MQTTBROKER.ip, MQTTBROKER.port, false, true, function(client)
    print("Connected to the MQTT Broker");
end)

-- set all PINs to LOW
gpio.write(0, gpio.LOW);
gpio.write(1, gpio.LOW);
gpio.write(2, gpio.LOW);
gpio.write(3, gpio.LOW);
gpio.write(4, gpio.LOW);
gpio.write(5, gpio.LOW);
gpio.write(6, gpio.LOW);
gpio.write(7, gpio.LOW);
gpio.write(8, gpio.LOW);
gpio.write(9, gpio.LOW);
gpio.write(10, gpio.LOW);
print("Ardulink - NodeMCU init done.");
