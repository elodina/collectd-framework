#!/bin/sh

export JAVA_HOME=/usr/lib/jvm/java-8-oracle

wget http://collectd.org/files/collectd-5.5.0.tar.bz2
tar jxf collectd-5.5.0.tar.bz2
cd collectd-5.5.0
./configure --with-java=$JAVA_HOME JAVA_CPPFLAGS="-I$JAVA_HOME/include -I$JAVA_HOME/include/linux" JAVA_CFLAGS="-I$JAVA_HOME/include -I$JAVA_HOME/include/linux" JAVA_LDFLAGS="-I$JAVA_HOME/include -I$JAVA_HOME/include/linux" JAVA_LIBS="-I$JAVA_HOME/include" --enable-java=force
make all install
ln -s /opt/collectd/sbin/collectd /usr/bin/collectd
