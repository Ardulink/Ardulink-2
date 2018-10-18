#!/bin/sh
### BEGIN INIT INFO
# Provides:          ardulink-proxy
# Required-Start:    $local_fs $syslog $network
# Required-Stop:     $local_fs $syslog $network
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     false
# Short-Description: ARDUlink Network Proxy Server
# Description:       ARDUlink Network Proxy Server runs in Java Virtual Machine and proxies ARDUlink Arduino connection. Default port is 4478.
#	To install this service (daemon) on Debian or Ubuntu follow these steps...
#	1. Login as root                su (Debian) sudo su (Ubuntu)
#	2. Install prerequisites        apt-get install unzip default-jre-headless librxtx-java
#	3. Download desired version     wget http://arduinopclink.altervista.org/wp-content/uploads/<...>/ardulink-V<...>.zip
#	4. Unzip it to /var             unzip ardulink-v<...>.zip -d /var
#	5. Fix APP_VERSION in file nano /var/ardulink/bin/ardulink-proxy.sh
#	5. Make this executable         chmod +x /var/ardulink/bin/ardulink-proxy.sh
#	6. Link it to a service         ln -s /var/ardulink/bin/ardulink-proxy.sh /etc/init.d/ardulink-proxy
#	7. Run on boot                  update-rc.d ardulink-proxy defaults
#	That's it! Start the service or reboot the OS. Following are managing commands...
#	- Disable on boot               update-rc.d ardulink-proxy disable
#	- Check service status          service ardulink-proxy status
#	- Start service manually        service ardulink-proxy start
#	- Stop service manually         service ardulink-proxy stop
#	- Restart service manually      service ardulink-proxy restart
#	- View the last log lines       tail /var/log/ardulink-proxy.log
#	If this file has syntax error ensure that TABs aren't replaced by Spaces.
### END INIT INFO
APP_VERSION="2.0.1"
APP_NAME=ardulink-proxy
SERVICE_NAME="ARDUlink Network Proxy Server"
JAR_COMMAND="/var/ardulink/lib/ardulink-networkproxyserver-$APP_VERSION.jar start"
PID_FILE=/var/run/$APP_NAME.pid
LOG=/var/log/$APP_NAME.log
case $1 in
	restart)
		if [ -f $PID_FILE ]; then
			PID=$(cat $PID_PATH_NAME);
			echo "Stopping $SERVICE_NAME on process ID $PID..."
			kill $PID;
			echo "$SERVICE_NAME stopped.";
			rm $PID_FILE
			echo "Starting $SERVICE_NAME..."
			nohup java -jar $JAR_COMMAND >> $LOG 2>&1&
						echo $! > $PID_FILE
			echo "$SERVICE_NAME started."
		else
			echo "$SERVICE_NAME is not running."
		fi
	;;
	start)
		echo "Starting $SERVICE_NAME..."
		if [ ! -f $PID_FILE ]; then
			nohup java -jar $JAR_COMMAND >> $LOG 2>&1&
						echo $! > $PID_FILE
			echo "$SERVICE_NAME started."
		else
			echo "$SERVICE_NAME is already running."
		fi
	;;
	stop)
		if [ -f $PID_FILE ]; then
			PID=$(cat $PID_FILE);
			echo "Stopping $SERVICE_NAME on process ID $PID..."
			kill $PID;
			echo "$SERVICE_NAME stopped."
			rm $PID_FILE
		else
			echo "$SERVICE_NAME is not running."
		fi
	;;
esac
