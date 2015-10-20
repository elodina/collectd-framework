./build-collectd-api.sh && \
  docker build -t collectd . && \
  docker run collectd
