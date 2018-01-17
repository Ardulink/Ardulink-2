# Ardulink-2 [![Build Status](https://travis-ci.org/Ardulink/Ardulink-2.svg?branch=master)](https://travis-ci.org/Ardulink/Ardulink-2)

This is the repository for Ardulink 2. Ardulink 2 is a complete, open source, java solution for the control and coordination of Arduino boards. This repository contains Ardulink Version 0.6.2 and all upcoming releases.

	public static void main(String[] args) throws InterruptedException, IOException {
		Link link = Links.getDefault();
		DigitalPin pin = Pin.digitalPin(2);
		for (boolean power = true;; power = !power) {
			System.out.println("Send power: " + power);
			link.switchDigitalPin(pin, power);
			TimeUnit.SECONDS.sleep(2);
		}
	}

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


