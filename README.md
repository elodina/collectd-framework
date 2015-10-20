# collectd-framework

## Build
```bash
git clone git@github.com:elodina/collectd-framework.git
cd collectd-framework
./build-collectd-api.sh
```

## Configure
All collectd configuration options are in collectd.conf, you can configure kafka plugin in section java:
```xml
    LoadPlugin "net.elodina.collectd.Kafka"
    <Plugin "Kafka">
        BrokerList "192.168.99.1:9092"
        BatchSize 10
    </Plugin>
```

## Run
At first, build docker image:
```bash
docker build -t collectd .
```

Then run it:
```
docker run collectd
```
