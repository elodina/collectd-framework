# collectd-framework

## Build
```bash
git clone git@github.com:elodina/collectd-framework.git
cd collectd-framework
./build-collectd-api.sh
```

You will have `collectd-api.jar` file containing all stuff you need to collect data and produce it to kafka.

## Configure
All collectd configuration options are in collectd.conf. At first, load java plugin by:
```
LoadPlugin java
```
Then configure it:
```xml
<Plugin java>
    JVMArg "-verbose:jni"
    JVMArg "-Djava.class.path=/usr/share/collectd/java/collectd-api.jar"

    LoadPlugin "net.elodina.collectd.Kafka"
    <Plugin "Kafka">
        BrokerList "localhost:9092"
        BatchSize 1000
    </Plugin>
</Plugin>
```

As you see, with `java` plugin here we have to load and configure `Kafka` plugin. Options:
- BrokerList: list of kafka brokers separated with comma. Default: `"localhost:9292"`
- BatchSize: kafka producer batch size. Default: `1000`

## Run

### Without Docker

Prerequisites:
- JVM
- Collectd precompiled with java plugin (in rpm-based systems package name is `collectd-java.x86_64`)

Launching:

1. You should put `collectd-api.jar` file to `/usr/share/collectd/java/` or you can replace option from configuration step with your custom path.
2. Launch collectd deamon:
```bash
collectd -C collecd.conf
```


### With Docker
At first, build docker image:
```bash
docker build -t collectd .
```

It will contain all dependencies, including JVM, collectd-java and Kafka plugin.

Then run it:
```
docker run collectd
```
