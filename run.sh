cd collectd-api && \
  gradle jar && \
  cd .. && \
  cp collectd-api/build/libs/collectd-api.jar . && \
  docker build -t collectd . && \
  docker run collectd
