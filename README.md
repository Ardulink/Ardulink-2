# Ardulink-2 [![Build Status](https://travis-ci.org/Ardulink/Ardulink-2.svg?branch=master)](https://travis-ci.org/Ardulink/Ardulink-2)

This is the repository for Ardulink 2. Ardulink 2 is a complete, open source, java solution for the control and coordination of Arduino boards. This repository contains Ardulink Version 0.6.2 and all upcoming releases.

		public static void main(String[] args) throws InterruptedException, IOException {
			Link link = Links.getDefault();
			DigitalPin pin = Pin.digitalPin(2);
			boolean power = true;
			while (true) {
				System.out.println("Send power:" + power);
				link.switchDigitalPin(pin, power);
				power = !power;
				TimeUnit.SECONDS.sleep(2);
			}
		}


see Ardulink site: www.ardulink.org


