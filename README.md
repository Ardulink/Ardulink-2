Ardulink-1
========

This is the repository for Ardulink 1. Ardulink 1 is a complete, open source, java solution for the control and coordination of Arduino boards. This repository contains Ardulink Version 0.6.1 – MAGNUM PI and of course all the previous releases.

Ardulink is heavily re engineerd in order to adhere to several international IoT standards. So, please, see Ardulink-2 repository for the last release.

Of course Ardulink-1 is still a good point to start if you are searching for a simpler solution.

	public static void main(String[] args) {
		try {
			Link link = Link.getDefaultInstance();

			List<String> portList = link.getPortList();
			if(portList != null && portList.size() > 0) {
				String port = portList.get(0);
				System.out.println("Connecting on port: " + port);
				boolean connected = link.connect(port);
				System.out.println("Connected:" + connected);
				Thread.sleep(2000); // Wait some seconds for Arduino reboot
				int power = IProtocol.HIGH;
				while(true) {
					System.out.println("Send power:" + power);
					link.sendPowerPinSwitch(2, power);
					if(power == IProtocol.HIGH) {
						power = IProtocol.LOW;
					} else {
						power = IProtocol.HIGH;
					}
					Thread.sleep(2000);
				}
			} else {
				System.out.println("No port found!");
			}
						
		}
		catch(Exception e) {
			e.printStackTrace();
		}


see Ardulink site: www.ardulink.org


