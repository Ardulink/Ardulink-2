#! /bin/sh

java -cp lib/ardulink.jar:lib/RXTXcomm.jar org.zu.ardulink.connection.proxy.NetworkProxyServer $1 $2
