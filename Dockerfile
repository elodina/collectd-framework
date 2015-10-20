FROM debian

RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
RUN echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886

RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections

RUN apt-get update && apt-get install -y \
  build-essential \
  librdkafka-dev \
  libpthread-stubs0-dev \
  zlib1g-dev \
  wget

RUN apt-get install -y \
  oracle-java8-installer \
  oracle-java8-set-default

COPY build.sh /tmp/

RUN cd /tmp && ./build.sh
COPY collectd.conf /opt/collectd/etc/
COPY collectd-api.jar /usr/share/collectd/java/

CMD ["/opt/collectd/sbin/collectd", "-f"]
