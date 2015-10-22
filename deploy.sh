#!/bin/sh
sudo yum install -y java-1.8.0-openjdk.x86_64 java-1.8.0-openjdk-devel.x86_64 collectd-java.x86_64
sudo cp collectd-api.jar /usr/share/collectd/java/
sudo collectd -C collectd.conf
