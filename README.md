# Ardulink-2 [![Build Status](https://travis-ci.org/Ardulink/Ardulink-2.svg?branch=master)](https://travis-ci.org/Ardulink/Ardulink-2)

This is the repository for Ardulink 2. Ardulink 2 is a complete, open source, java solution for the control and coordination of Arduino boards. This repository contains Ardulink Version 0.6.2 and all upcoming releases.
```java
	public static void main(String[] args) throws InterruptedException, IOException {
		Link link = Links.getDefault();
		DigitalPin pin = Pin.digitalPin(2);
		for (boolean power = true;; power = !power) {
			System.out.println("Send power: " + power);
			link.switchDigitalPin(pin, power);
			TimeUnit.SECONDS.sleep(2);
		}
	}
```
Recommended way of using Ardulink-2 inside your own application is declaring a dependency on “ardulink-core-base” using your favorite build system. When using maven please add 
```xml
<dependency>
    <groupId>org.ardulink</groupId>
    <artifactId>ardulink-core-base</artifactId>
    <version>2.1.0</version>
</dependency>
```
Without adding additional jars ```Links.getDefault()``` would throw an exception because no link type is available. For each link type a separate jar is available. So if you like to connect using a serial link you have to add the corresponding module.For serial link this is
```xml
<dependency>
    <groupId>org.ardulink</groupId>
    <artifactId>ardulink-core-serial-jssc</artifactId>
    <version>2.1.0</version>
</dependency>
```

To see all available link types and a how to use Ardulink with other build systems see https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.ardulink%22

see Ardulink site: www.ardulink.org

## Network Proxy Server

Arduino Ethernet Shields are quite expensive compared to Arduino itself. **Network Proxy Server** installed on PC is cheap way to connect your Arduinos via Ethernet.

### Minimum Requirements

You can run it almost any computer which is less than 20 years old. Following hardware is tested with latest [Debian](https://www.debian.org/):
- 128 MB RAM
- 2 GB HDD
- one USB 1.0 port
- Ethernet (PAN, LAN, WLAN or WAN)
- temporary OS installition media (CD-ROM, USB, Net-boot)

### Install steps
1. [Download *Debian*](https://www.debian.org/distrib/).
2. Install it following [Installation Guide](https://www.debian.org/releases/stable/installmanual).
3. After reboot login using the created username (*root* can't login by default).
4. Switch user to *root*: `su`
5. Install prerequisites: `apt-get install unzip default-jre-headless librxtx-java`
6. [Download Ardulink](http://www.ardulink.org/download/): `wget http://arduinopclink.altervista.org/wp-content/uploads/… …/ardulink-V… ….zip`
7. Unzip it to */var*-path: `unzip ardulink-v… ….zip -d /var`
8. Ensure that **APP_VERSION** is correct in file: `nano /var/ardulink/bin/ardulink-proxy.sh`
9. Change the script to executable: `chmod +x /var/ardulink/bin/ardulink-proxy.sh`
10. Link it to a service: `ln -s /var/ardulink/bin/ardulink-proxy.sh /etc/init.d/ardulink-proxy`
11. Start the service on boot: `update-rc.d ardulink-proxy defaults`
#### Testing
12. Start the service now: `service ardulink-proxy start` (or `reboot` the OS)
13. Upload **/var/ardulink/sketches/ArdulinkProtocol/ArdulinkProtocol.ino** sketch to your Arduino board (if not done already).
14. Attach Arduino to your server USB port. (First USB device gets usually */dev/ttyUSB0* port on Linux.)
15. Connect to your Arduino from other computer eg. using **Ardulink Console**:
    1. Run it using `java -jar ardulink-console-… ….jar`.
    2. In *Configuration* tab select *Type* **proxy**.
    3. Fill server IP address to *tcphost*.
    4. Search ports using magnifier icon.
    5. Press *Connect* button.
    6. Go to *Switch Panel*.
    7. Switch power pin 13 to *On* and Arduino LED should be activated.

### Other useful commands on server
- `service ardulink-proxy status` shows the service status.
- `service ardulink-proxy stop` stops the service.
- `service ardulink-proxy restart` quess?
- `tail /var/log/ardulink-proxy.log` shows last service log lines.
