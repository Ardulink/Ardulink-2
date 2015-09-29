package org.zu.ardulink.protocol.custommessages;



public class SimpleCustomMessageMaker implements CustomMessageMaker {

	@Override
	public String getCustomMessage(String... args) {
		
		StringBuilder builder = new StringBuilder();
		if(args.length > 0) {
			for (int i = 0; i < args.length - 1; i++) {
				builder.append(args[i]);
				builder.append("/");
			}
			builder.append(args[args.length - 1]);
		}

		String retvalue = builder.toString(); 
		return retvalue;
	}
}
