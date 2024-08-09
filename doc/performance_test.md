# KoP性能测试

## 1. 测试范围

KoP, Pulsar, Kafka, KoP Proxy

## 2. 测试工具

[OpenMessaging benchmark](https://github.com/ascentstream/benchmark)

## 3. 测试环境

- 硬件配置
    - CPU: VCPU 4
    - Memory: 32GB
    - 网络: 1Gbps
    - 磁盘: 1TB SSD * 2 (Read: MB/s, Write: MB/s)
- 软件配置
    - OS: CentOS 7.9
    - Java: Oracle JDK 17
    - Pulsar: [2.10.7.1](https://github.com/ascentstream/pulsar/releases/tag/v2.10.7.1)
    - Kafka: 3.4.1
    - KoP: [2.10.7.1](https://github.com/ascentstream/asp-kop/releases/tag/v2.10.7.1)
    - Bookkeeper: 4.14.8
    - Zookeeper: 3.9.2
    - [OpenMessaging benchmark](https://github.com/ascentstream/benchmark) 0.0.1

## 4. 测试部署

- 硬件需求
    - 5台机器，每台机器配置[如上](#3-测试环境)
- 部署方式
    - Pulsar/KoP

  ![pulsar.png](./Pulsar_KoP.png)

    - Kafka

  ![kafka.png](./kafka.png)

## 5. 相关核心配置

- Durability Level
    - Level1:
        - KoP/Pulsar:
            - Replication: SYNC(ensemble=3, writeQuorum=3, ackQuorum=2)
            - Bookkeeper(ASYNC):
                - Enable journaling
                    - journalWriteData=true
                    - journalSyncData=false
                - Disable journaling
                    - journalWriteData=false
                    - journalSyncData=false
        - Kafka:
            - Replication: SYNC(ack=all, min.insync.replicas=2)
            - Log: ASYNC(log.flush.interval.messages=10000, log.flush.interval.ms=1000)
    - Level2:
        - KoP/Pulsar:
        - Replication: ASYNC(ensemble=3, writeQuorum=3, ackQuorum=1)
        - Bookkeeper(ASYNC):
            - Enable journaling
                - journalWriteData=true
                - journalSyncData=false
            - Disable journaling
                - journalWriteData=false
                - journalSyncData=false
        - Kafka:
            - Replication: ASYNC(ack=1, min.insync.replicas=2)
            - Log: ASYNC(log.flush.interval.messages=10000, log.flush.interval.ms=1000)
- Pulsar
    - Broker.conf

      ```properties
      managedLedgerNewEntriesCheckDelayInMillis=0
      bookkeeperNumberOfChannelsPerBookie=64
  
      # use sync replication mode
      managedLedgerDefaultEnsembleSize=3
      managedLedgerDefaultWriteQuorum=3
      managedLedgerDefaultAckQuorum=2
      ```
    - Bookkeeper.conf

      ```properties
      journalPageCacheFlushIntervalMSec=1000
  
      # disable/enable journaling
      journalWriteData=false/true
      journalSyncData=false
      ```
    - Broker JVM options

      ```text
      -Xmx6G -Xms6G -XX:MaxDirectMemorySize=6G
      ```
    - Bookkeeper JVM options

      ```text
      -Xmx6G -Xms6G -XX:MaxDirectMemorySize=6G
      ```
    - Client Settings

      ```properties
      # Producer
      batchingPartitionSwitchFrequencyByPublishDelay=2
      # Consumer
      maxTotalReceiverQueueSizeAcrossPartitions=5000000
      ```
- KoP
    - 基本设置如Pulsar
    - KoP设置

      ```properties
      entryFormat=kafka
      ```
- Kafka
    - Broker.conf

      ```properties
      ```
    - Broker JVM options

      ```text
      -Xmx12G -Xms12G -XX:MaxDirectMemorySize=12G
      ```
- Benchmark
    - Driver config
        - [Pulsar](../driver-pulsar/pulsar_asp_perf.yaml)
        - [KoP](../driver-kop/kafka_to_kafka_asp_perf.yaml)
        - [Kafka](../driver-kafka/kafka-ack-all-nofsync.yaml)

## 6. 测试场景

- 场景

| 场景 | Topics | Partitions per topic | Producers | Subscriptions | Consumers per subscription | Message Size | Workload                                                                                                                     |
|----|--------|----------------------|-----------|---------------|----------------------------|--------------|------------------------------------------------------------------------------------------------------------------------------|
| 1  | 1      | 1                    | 1         | 1             | 1                          | 100B         | [1-topic-1-partition-1p-1c-100b](../workloads/asp/max/1-topic-1-partition/max-1-topic-1-partition-1p-1c-100b.yaml)           |
| 2  | 1      | 1                    | 1         | 1             | 1                          | 1KB          | [1-topic-1-partition-1p-1c-1kb](../workloads/asp/max/1-topic-1-partition/max-1-topic-1-partition-1p-1c-1kb.yaml)             |
| 3  | 1      | 1                    | 1         | 1             | 1                          | 64KB         | [1-topic-1-partition-1p-1c-64kb](../workloads/asp/max/1-topic-1-partition/max-1-topic-1-partition-1p-1c-64kb.yaml)           |
| 4  | 1      | 16                   | 1         | 1             | 1                          | 100B         | [1-topic-16-partition-1p-1c-100b](../workloads/asp/max/1-topic-16-partition/max-1-topic-16-partition-1p-1c-100b.yaml)        |
| 5  | 1      | 16                   | 1         | 1             | 1                          | 1KB          | [1-topic-16-partition-1p-1c-1kb](../workloads/asp/max/1-topic-16-partition/max-1-topic-16-partition-1p-1c-1kb.yaml)          |
| 6  | 1      | 16                   | 1         | 1             | 1                          | 64KB         | [1-topic-16-partition-1p-1c-64kb](../workloads/asp/max/1-topic-16-partition/max-1-topic-16-partition-1p-1c-64kb.yaml)        |
| 7  | 1      | 64                   | 4         | 1             | 4                          | 100B         | [1-topic-64-partition-4p-4c-100b](../workloads/asp/max/1-topic-64-partition/max-1-topic-64-partition-4p-4c-100b.yaml)        |
| 8  | 1      | 64                   | 4         | 1             | 4                          | 1KB          | [1-topic-64-partition-4p-4c-1kb](../workloads/asp/max/1-topic-64-partition/max-1-topic-64-partition-4p-4c-1kb.yaml)          |
| 9  | 1      | 64                   | 4         | 1             | 4                          | 64KB         | [1-topic-64-partition-4p-4c-64kb](../workloads/asp/max/1-topic-64-partition/max-1-topic-64-partition-4p-4c-64kb.yaml)        |
| 10 | 1      | 512                  | 16        | 1             | 16                         | 100B         | [1-topic-512-partition-16p-16c-100b](../workloads/asp/max/1-topic-512-partition/max-1-topic-512-partition-16p-16c-100b.yaml) |
| 11 | 1      | 512                  | 16        | 1             | 16                         | 1KB          | [1-topic-512-partition-16p-16c-1kb](../workloads/asp/max/1-topic-512-partition/max-1-topic-512-partition-16p-16c-1kb.yaml)   |
| 12 | 1      | 512                  | 16        | 1             | 16                         | 64KB         | [1-topic-512-partition-16p-16c-64kb](../workloads/asp/max/1-topic-512-partition/max-1-topic-512-partition-16p-16c-64kb.yaml) |

- 后续规划

增加Catch-up read测试

- 测试步骤
    - 启动Pulsar/KoP/Kafka集群
    - 启动benchmark driver
    - 运行测试场景
    - 结果分析
- 关注指标
    - 吞吐量
        - Publish Throughput(Message, Byte)
        - Consume Throughput(Message, Byte)
    - 延迟:
        - Publish Latency(AVG, MAX, P90, P99, P999)
        - End-to-End Latency(AVG, MAX, P90, P99, P999)

## 7. 测试数据

- 场景1
    - Pulsar
        - Enable journaling
            - Benchmark result:
            - CPU:
            - Memory:
            - Disk:
        - Disable journaling
            - Benchmark result:
            - CPU:
            - Memory:
            - Disk:
    - KoP
        - Enable journaling
            - Benchmark result:
            - CPU:
            - Memory:
            - Disk:
        - Disable journaling
            - Benchmark result:
            - CPU:
            - Memory:
            - Disk:
    - Kafka
        - Benchmark result:
        - CPU:
        - Memory:
        - Disk:

## 8. 原始结果数据

## 9. 优化过后的结果数据

### Publish rate: 10k

#### Topic partitions: 1-topic-1-partition

##### Payload size: 16384 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.02                | 2.59                | 2.89                | 20.7                 | 59.18               | 10001.48         | 2.73            | 3.0             | 5.0             | 27.0             | 71.0            | 10001.48         |
| Pulsar                | 1.78                | 2.48                | 2.65                | 2.94                 | 20.92               | 10001.71         | 2.43            | 3.0             | 3.0             | 4.0              | 24.0            | 10001.71         |
| KOP331                | 1.77                | 2.47                | 2.68                | 11.52                | 89.14               | 10002.25         | 2.38            | 3.0             | 4.0             | 15.0             | 90.0            | 10002.26         |
| Pulsar331             | 1.48                | 2.26                | 2.4                 | 2.73                 | 38.39               | 10002.03         | 2.16            | 3.0             | 3.0             | 4.0              | 50.0            | 10002.04         |
| Kafka                 | 228.2               | 242.9               | 282.19              | 295.16               | 305.21              | 7859.15          | 228.49          | 243.0           | 282.0           | 295.0            | 306.0           | 7859.15          |
| Kafka Ack1            | 1.33                | 1.26                | 17.18               | 57.43                | 136.5               | 10001.6          | 3.21            | 5.0             | 48.0            | 82.0             | 180.0           | 10001.6          |
| Pulsar No Journal     | 1.04                | 1.32                | 1.41                | 14.55                | 140.83              | 10002.53         | 1.65            | 2.0             | 2.0             | 18.0             | 141.0           | 10002.5          |
| Pulsar No Journal 331 | 0.9                 | 1.16                | 1.23                | 33.01                | 169.42              | 10003.29         | 1.45            | 2.0             | 2.0             | 44.0             | 181.0           | 10003.26         |

#### Topic partitions: 1-topic-16-partition

##### Payload size: 16384 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 1.95                | 2.57                | 2.77                | 9.37                 | 57.45               | 10001.69         | 2.65            | 3.0             | 4.0             | 16.0             | 61.0            | 10001.7          |
| Pulsar                | 1.91                | 2.51                | 2.64                | 3.19                 | 25.57               | 10001.58         | 2.46            | 3.0             | 3.0             | 4.0              | 31.0            | 10001.57         |
| KOP331                | 1.65                | 2.37                | 2.59                | 4.98                 | 85.02               | 10002.07         | 2.34            | 3.0             | 4.0             | 11.0             | 86.0            | 10002.08         |
| Pulsar331             | 1.58                | 2.25                | 2.47                | 2.83                 | 77.78               | 10001.47         | 2.06            | 3.0             | 3.0             | 4.0              | 79.0            | 10001.46         |
| Kafka                 | 6.25                | 3.46                | 68.2                | 966.65               | 2564.93             | 10001.93         | 6.61            | 4.0             | 70.0            | 969.0            | 2568.01         | 10001.94         |
| Kafka Ack1            | 1.46                | 1.22                | 1.35                | 173.26               | 1034.79             | 10001.36         | 5.97            | 2.0             | 61.0            | 830.0            | 1890.01         | 10001.35         |
| Pulsar No Journal     | 1.11                | 1.44                | 1.55                | 1.97                 | 102.21              | 10002.86         | 1.62            | 2.0             | 2.0             | 3.0              | 103.0           | 10002.88         |
| Pulsar No Journal 331 | 0.98                | 1.32                | 1.4                 | 1.63                 | 148.13              | 10003.62         | 1.62            | 2.0             | 2.0             | 2.0              | 150.0           | 10003.62         |

#### Topic partitions: 1-topic-64-partition

##### Payload size: 16384 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.03                | 2.65                | 2.91                | 12.23                | 74.14               | 10002.04         | 3.23            | 4.0             | 6.0             | 38.0             | 387.0           | 10002.05         |
| Pulsar                | 1.93                | 2.53                | 2.68                | 3.22                 | 41.14               | 10001.71         | 2.49            | 3.0             | 3.0             | 4.0              | 41.0            | 10001.69         |
| KOP331                | 1.73                | 2.46                | 2.74                | 7.67                 | 82.11               | 10002.25         | 3.29            | 5.0             | 6.0             | 30.0             | 172.0           | 10002.24         |
| Pulsar331             | 1.59                | 2.25                | 2.48                | 2.87                 | 34.33               | 10001.85         | 2.06            | 3.0             | 3.0             | 4.0              | 35.0            | 10001.85         |
| Kafka                 | 2.76                | 3.98                | 19.1                | 94.13                | 525.42              | 10001.95         | 3.17            | 4.0             | 21.0            | 116.0            | 528.0           | 10001.93         |
| Kafka Ack1            | 0.95                | 1.24                | 1.36                | 32.37                | 212.07              | 10002.42         | 2.35            | 3.0             | 12.0            | 70.0             | 217.0           | 10002.45         |
| Pulsar No Journal     | 1.17                | 1.51                | 1.62                | 1.98                 | 93.11               | 10004.01         | 1.64            | 2.0             | 2.0             | 3.0              | 98.0            | 10004.01         |
| Pulsar No Journal 331 | 0.99                | 1.33                | 1.42                | 1.7                  | 116.88              | 10003.27         | 1.74            | 2.0             | 2.0             | 3.0              | 1115.01         | 10003.25         |

#### Topic partitions: 1-topic-512-partition

##### Payload size: 16384 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.32                | 2.96                | 3.78                | 35.9                 | 155.09              | 10002.87         | 13.66           | 20.0            | 26.0            | 98.0             | 518.0           | 10002.86         |
| Pulsar                | 1.95                | 2.56                | 2.75                | 4.83                 | 73.8                | 10002.55         | 2.52            | 3.0             | 3.0             | 6.0              | 74.0            | 10002.55         |
| KOP331                | 2.21                | 2.81                | 7.0                 | 79.57                | 167.53              | 10001.99         | 14.33           | 24.0            | 53.0            | 251.0            | 695.0           | 10001.85         |
| Pulsar331             | 1.59                | 2.25                | 2.5                 | 6.21                 | 98.66               | 10002.22         | 2.07            | 3.0             | 3.0             | 8.0              | 102.0           | 10002.22         |
| Kafka                 | 8.29                | 14.11               | 64.48               | 224.34               | 327.21              | 10002.38         | 9.04            | 16.0            | 70.0            | 229.0            | 330.0           | 10002.37         |
| Kafka Ack1            | 1.29                | 1.58                | 11.19               | 37.57                | 221.77              | 10004.02         | 5.43            | 13.0            | 31.0            | 73.0             | 251.0           | 10004.03         |
| Pulsar No Journal     | 1.23                | 1.59                | 1.78                | 4.4                  | 115.74              | 10003.31         | 1.71            | 2.0             | 2.0             | 6.0              | 118.0           | 10003.31         |
| Pulsar No Journal 331 | 1.0                 | 1.32                | 1.42                | 4.28                 | 90.31               | 10003.78         | 1.63            | 2.0             | 2.0             | 7.0              | 94.0            | 10003.77         |

### Publish rate: 100k

#### Topic partitions: 1-topic-1-partition

##### Payload size: 100 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.09                | 2.7                 | 2.88                | 11.8                 | 35.27               | 100023.63        | 2.27            | 3.0             | 3.0             | 16.0             | 40.0            | 100023.63        |
| Pulsar                | 1.41                | 2.27                | 2.35                | 6.76                 | 37.65               | 100019.97        | 2.08            | 3.0             | 3.0             | 9.0              | 39.0            | 100019.97        |
| KOP331                | 2.1                 | 2.73                | 2.93                | 14.47                | 69.78               | 100024.56        | 2.3             | 3.0             | 3.0             | 19.0             | 70.0            | 100024.56        |
| Pulsar331             | 1.17                | 2.21                | 2.35                | 3.69                 | 20.38               | 100027.54        | 1.84            | 3.0             | 3.0             | 5.0              | 33.0            | 100027.54        |
| Kafka                 | 2.12                | 2.71                | 10.69               | 58.81                | 83.41               | 100023.3         | 2.13            | 3.0             | 11.0            | 59.0             | 84.0            | 100023.3         |
| Kafka Ack1            | 0.89                | 1.44                | 1.51                | 14.47                | 63.66               | 100049.33        | 1.51            | 2.0             | 2.0             | 46.0             | 71.0            | 100049.76        |
| Pulsar No Journal     | 0.87                | 1.24                | 1.27                | 32.31                | 138.69              | 100043.99        | 1.51            | 2.0             | 2.0             | 35.0             | 139.0           | 100044.33        |
| Pulsar No Journal 331 | 0.8                 | 1.22                | 1.26                | 1.49                 | 85.34               | 100040.36        | 1.43            | 2.0             | 2.0             | 2.0              | 85.0            | 100040.36        |

##### Payload size: 1024 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.23                | 2.93                | 3.26                | 23.27                | 92.1                | 100017.31        | 2.82            | 4.0             | 5.0             | 30.0             | 98.0            | 100017.5         |
| Pulsar                | 1.81                | 2.6                 | 2.72                | 7.36                 | 51.25               | 100022.7         | 2.63            | 3.0             | 4.0             | 10.0             | 53.0            | 100022.95        |
| KOP331                | 2.11                | 2.88                | 3.1                 | 13.19                | 49.83               | 100022.36        | 2.61            | 3.0             | 4.0             | 23.0             | 50.0            | 100022.67        |
| Pulsar331             | 1.52                | 2.48                | 2.6                 | 6.04                 | 40.15               | 100020.4         | 2.34            | 3.0             | 3.0             | 9.0              | 51.0            | 100020.23        |
| Kafka                 | 438.27              | 478.66              | 514.0               | 633.16               | 658.53              | 73615.82         | 438.5           | 479.0           | 514.0           | 633.0            | 659.0           | 73615.82         |
| Kafka Ack1            | 1.17                | 1.74                | 1.85                | 38.67                | 63.11               | 100041.02        | 2.49            | 3.0             | 23.0            | 58.0             | 79.0            | 100057.96        |
| Pulsar No Journal     | 1.1                 | 1.53                | 1.6                 | 1.71                 | 86.78               | 100046.72        | 1.84            | 2.0             | 2.0             | 3.0              | 88.0            | 100046.72        |
| Pulsar No Journal 331 | 0.99                | 1.41                | 1.47                | 1.59                 | 114.0               | 100046.29        | 1.79            | 2.0             | 2.0             | 3.0              | 114.0           | 100046.29        |

#### Topic partitions: 1-topic-16-partition

##### Payload size: 100 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.13                | 2.74                | 2.94                | 14.15                | 56.78               | 100020.76        | 3.12            | 3.0             | 6.0             | 183.0            | 725.0           | 100020.76        |
| Pulsar                | 1.52                | 2.37                | 2.47                | 6.1                  | 46.59               | 100024.84        | 1.55            | 2.0             | 2.0             | 7.0              | 46.0            | 100024.84        |
| KOP331                | 2.07                | 2.72                | 2.91                | 9.63                 | 65.41               | 100018.91        | 2.86            | 3.0             | 4.0             | 176.0            | 329.0           | 100019.09        |
| Pulsar331             | 1.43                | 2.34                | 2.44                | 4.36                 | 92.95               | 100017.44        | 1.47            | 2.0             | 2.0             | 5.0              | 93.0            | 100017.44        |
| Kafka                 | 1.39                | 2.08                | 2.4                 | 36.75                | 87.36               | 100019.34        | 1.73            | 2.0             | 4.0             | 61.0             | 419.0           | 100019.34        |
| Kafka Ack1            | 0.89                | 1.44                | 1.52                | 19.75                | 49.44               | 100048.26        | 1.56            | 2.0             | 2.0             | 42.0             | 245.0           | 100048.26        |
| Pulsar No Journal     | 0.85                | 1.28                | 1.34                | 1.44                 | 84.74               | 100037.75        | 1.01            | 1.0             | 1.0             | 1.0              | 85.0            | 100037.75        |
| Pulsar No Journal 331 | 0.85                | 1.27                | 1.33                | 1.43                 | 110.37              | 100045.67        | 1.01            | 1.0             | 1.0             | 1.0              | 111.0           | 100045.33        |

##### Payload size: 1024 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.26                | 3.0                 | 3.24                | 14.82                | 61.28               | 100018.74        | 2.89            | 4.0             | 4.0             | 25.0             | 131.0           | 100018.74        |
| Pulsar                | 1.93                | 2.74                | 2.89                | 6.82                 | 40.64               | 100020.95        | 2.54            | 3.0             | 3.0             | 9.0              | 41.0            | 100021.11        |
| KOP331                | 2.07                | 2.9                 | 3.13                | 15.27                | 133.04              | 100024.92        | 2.72            | 4.0             | 5.0             | 25.0             | 135.0           | 100024.88        |
| Pulsar331             | 1.67                | 2.59                | 2.71                | 4.93                 | 98.31               | 100020.13        | 2.34            | 3.0             | 3.0             | 6.0              | 98.0            | 100019.94        |
| Kafka                 | 3.74                | 2.19                | 22.15               | 584.63               | 1280.28             | 100019.67        | 4.01            | 3.0             | 23.0            | 587.0            | 1283.01         | 100019.87        |
| Kafka Ack1            | 1.18                | 1.73                | 1.85                | 31.33                | 381.46              | 100036.57        | 3.07            | 3.0             | 19.0            | 223.0            | 625.0           | 100036.6         |
| Pulsar No Journal     | 1.18                | 1.62                | 1.7                 | 1.82                 | 64.16               | 100050.51        | 1.45            | 2.0             | 2.0             | 2.0              | 65.0            | 100050.16        |
| Pulsar No Journal 331 | 1.05                | 1.48                | 1.56                | 1.68                 | 93.5                | 100051.29        | 1.06            | 1.0             | 2.0             | 2.0              | 95.0            | 100050.97        |

#### Topic partitions: 1-topic-64-partition

##### Payload size: 100 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.14                | 2.77                | 3.06                | 15.22                | 57.15               | 100022.05        | 3.49            | 3.0             | 5.0             | 379.0            | 820.0           | 100021.93        |
| Pulsar                | 1.52                | 2.31                | 2.41                | 7.82                 | 57.16               | 100023.55        | 1.6             | 2.0             | 2.0             | 9.0              | 57.0            | 100023.64        |
| KOP331                | 2.15                | 2.77                | 3.03                | 17.69                | 135.94              | 100023.88        | 3.72            | 3.0             | 7.0             | 390.0            | 528.0           | 100023.9         |
| Pulsar331             | 1.2                 | 2.2                 | 2.37                | 4.98                 | 52.31               | 100024.19        | 1.27            | 2.0             | 2.0             | 6.0              | 52.0            | 100024.19        |
| Kafka                 | 1.66                | 2.4                 | 8.03                | 51.55                | 81.12               | 100021.36        | 2.98            | 2.0             | 20.0            | 421.0            | 1251.01         | 100021.36        |
| Kafka Ack1            | 0.92                | 1.43                | 1.53                | 29.81                | 77.17               | 100054.55        | 2.76            | 2.0             | 13.0            | 336.0            | 1506.01         | 100054.68        |
| Pulsar No Journal     | 0.84                | 1.27                | 1.33                | 1.49                 | 90.54               | 100058.4         | 1.01            | 1.0             | 1.0             | 2.0              | 90.0            | 100058.4         |
| Pulsar No Journal 331 | 0.83                | 1.26                | 1.32                | 1.58                 | 108.7               | 100055.04        | 1.02            | 1.0             | 1.0             | 2.0              | 108.0           | 100054.91        |

##### Payload size: 1024 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.28                | 2.96                | 3.74                | 21.3                 | 76.95               | 100029.71        | 2.99            | 4.0             | 7.0             | 71.0             | 363.0           | 100029.66        |
| Pulsar                | 1.68                | 2.46                | 2.58                | 9.2                  | 81.91               | 100023.61        | 1.63            | 2.0             | 3.0             | 11.0             | 83.0            | 100023.49        |
| KOP331                | 2.23                | 2.92                | 3.46                | 22.7                 | 85.72               | 100027.06        | 3.01            | 4.0             | 7.0             | 93.0             | 435.0           | 100027.14        |
| Pulsar331             | 1.34                | 2.33                | 2.52                | 5.55                 | 84.93               | 100023.97        | 1.29            | 2.0             | 2.0             | 7.0              | 84.0            | 100023.82        |
| Kafka                 | 2.51                | 2.75                | 20.68               | 185.71               | 572.27              | 100021.95        | 2.74            | 3.0             | 23.0            | 188.0            | 567.0           | 100022.07        |
| Kafka Ack1            | 1.05                | 1.64                | 1.82                | 34.74                | 79.29               | 100049.74        | 2.05            | 3.0             | 9.0             | 59.0             | 205.0           | 100049.74        |
| Pulsar No Journal     | 1.0                 | 1.42                | 1.51                | 2.01                 | 136.51              | 100057.22        | 1.03            | 1.0             | 1.0             | 3.0              | 136.0           | 100057.22        |
| Pulsar No Journal 331 | 0.92                | 1.35                | 1.44                | 1.73                 | 98.86               | 100057.86        | 1.02            | 1.0             | 1.0             | 2.0              | 99.0            | 100057.59        |

#### Topic partitions: 1-topic-512-partition

##### Payload size: 100 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 2.72                | 3.95                | 14.98               | 59.99                | 108.25              | 100030.27        | 5.4             | 6.0             | 40.0            | 469.0            | 993.0           | 100029.81        |
| Pulsar                | 1.75                | 2.4                 | 3.02                | 29.81                | 117.85              | 100025.99        | 1.78            | 2.0             | 4.0             | 32.0             | 121.0           | 100025.84        |
| KOP331                | 3.14                | 4.37                | 39.31               | 107.48               | 170.48              | 100035.71        | 9.42            | 10.0            | 249.0           | 502.0            | 1011.0          | 100035.57        |
| Pulsar331             | 1.48                | 2.34                | 2.55                | 25.95                | 87.73               | 100028.43        | 1.5             | 2.0             | 3.0             | 28.0             | 97.0            | 100028.49        |
| Kafka                 | 13.69               | 17.55               | 26.9                | 72.87                | 149.78              | 100032.97        | 14.81           | 18.0            | 30.0            | 447.0            | 1165.01         | 100033.06        |
| Kafka Ack1            | 1.3                 | 2.28                | 3.16                | 23.48                | 77.36               | 100065.82        | 4.73            | 5.0             | 16.0            | 470.0            | 906.0           | 100065.73        |
| Pulsar No Journal     | 0.95                | 1.36                | 1.47                | 19.34                | 103.36              | 100058.3         | 1.07            | 1.0             | 2.0             | 21.0             | 104.0           | 100058.23        |
| Pulsar No Journal 331 | 0.94                | 1.37                | 1.48                | 12.95                | 74.0                | 100056.62        | 1.05            | 1.0             | 2.0             | 15.0             | 74.0            | 100055.59        |

##### Payload size: 1024 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | 3.18                | 5.13                | 17.01               | 68.48                | 158.04              | 100090.36        | 4.72            | 8.0             | 31.0            | 206.0            | 564.0           | 100089.06        |
| Pulsar                | 1.97                | 2.52                | 3.97                | 35.05                | 122.99              | 100027.65        | 1.94            | 2.0             | 5.0             | 37.0             | 123.0           | 100027.67        |
| KOP331                | 3.69                | 6.01                | 35.52               | 108.78               | 205.08              | 100080.15        | 5.54            | 9.0             | 58.0            | 272.0            | 544.0           | 100080.45        |
| Pulsar331             | 1.73                | 2.47                | 3.05                | 34.63                | 120.23              | 100030.62        | 1.7             | 2.0             | 4.0             | 37.0             | 120.0           | 100030.82        |
| Kafka                 | 7.88                | 14.6                | 26.06               | 81.79                | 243.45              | 100046.78        | 7.99            | 15.0            | 27.0            | 89.0             | 511.0           | 100046.79        |
| Kafka Ack1            | 1.53                | 2.75                | 4.27                | 31.73                | 115.11              | 100124.58        | 4.48            | 6.0             | 15.0            | 73.0             | 308.0           | 100125.21        |
| Pulsar No Journal     | 1.03                | 1.45                | 1.59                | 21.06                | 118.95              | 100080.35        | 1.08            | 1.0             | 2.0             | 23.0             | 117.0           | 100080.31        |
| Pulsar No Journal 331 | 1.02                | 1.44                | 1.58                | 25.75                | 82.58               | 100072.75        | 1.08            | 1.0             | 2.0             | 28.0             | 84.0            | 100072.4         |

### Publish rate: maxrate

#### Topic partitions: 1-topic-1-partition

##### Payload size: 1024 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar                | 1442.48             | 2485.36             | 2883.31             | 3361.55              | 4136.54             | 181834.4         | 1479.44         | 2579.01         | 2919.01         | 3363.01          | 4138.02         | 181832.33        |
| KOP331                | 205.48              | 235.82              | 2238.24             | 3615.18              | 60000.25            | 123842.2         | 389681.94       | 575078.4        | 578338.81       | 579301.37        | 579407.87       | 15437.01         |
| Pulsar331             | 1449.76             | 3649.09             | 3890.83             | 4490.21              | 4520.19             | 181607.15        | 159690.15       | 559448.06       | 569462.78       | 570843.14        | 570843.14       | 12251.49         |
| Kafka                 | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Kafka Ack1            | 113.33              | 123.34              | 129.95              | 186.61               | 218.15              | 284675.12        | 62311.23        | 133937.15       | 141913.09       | 144116.73        | 144210.94       | 158114.73        |
| Pulsar No Journal     | 1059.99             | 1075.3              | 1120.3              | 1218.21              | 1270.73             | 247505.18        | 1064.32         | 1087.01         | 1123.01         | 1197.01          | 1206.01         | 247504.78        |
| Pulsar No Journal 331 | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |

#### Topic partitions: 1-topic-16-partition

##### Payload size: 1024 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar                | 1338.96             | 3804.16             | 4151.33             | 5764.57              | 7842.85             | 180142.59        | 5823.04         | 4265.02         | 170926.08       | 233148.42        | 240251.9        | 177516.07        |
| KOP331                | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar331             | 1402.65             | 3827.53             | 4218.72             | 7782.21              | 8863.3              | 181935.3         | 3493.13         | 3902.01         | 50053.12        | 385867.78        | 393273.34       | 174583.07        |
| Kafka                 | 74.93               | 90.33               | 375.54              | 1298.63              | 2373.04             | 429784.78        | 77.28           | 93.0            | 381.0           | 1295.01          | 2375.01         | 429788.41        |
| Kafka Ack1            | 49.16               | 58.84               | 552.08              | 1657.96              | 3163.78             | 653632.18        | 47985.16        | 201379.84       | 222826.49       | 234328.06        | 237793.28       | 346139.38        |
| Pulsar No Journal     | 388.91              | 1119.8              | 1618.22             | 2399.89              | 2651.33             | 564113.82        | 861.51          | 1214.01         | 2111.01         | 179554.3         | 296450.05       | 530104.4         |
| Pulsar No Journal 331 | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |

#### Topic partitions: 1-topic-64-partition

##### Payload size: 1024 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar                | 1431.05             | 3901.97             | 4051.74             | 4589.69              | 4933.66             | 181914.68        | 1509.89         | 3925.01         | 4116.02         | 4661.02          | 8347.01         | 181912.62        |
| KOP331                | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar331             | 1432.83             | 3869.79             | 3971.98             | 4104.18              | 4370.91             | 182865.13        | 1498.82         | 3893.01         | 4012.01         | 4345.02          | 5037.02         | 182773.48        |
| Kafka                 | 208.71              | 386.93              | 1337.73             | 6063.71              | 8213.89             | 611939.34        | 225.62          | 477.0           | 1481.01         | 6060.03          | 8213.02         | 612202.98        |
| Kafka Ack1            | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar No Journal     | 475.59              | 1219.6              | 1373.7              | 1528.02              | 1904.88             | 536442.67        | 1192.56         | 1279.01         | 1924.01         | 279904.26        | 307488.77       | 529179.49        |
| Pulsar No Journal 331 | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |

#### Topic partitions: 1-topic-512-partition

##### Payload size: 1024 bytes

|                       | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |
|-----------------------|---------------------|---------------------|---------------------|----------------------|---------------------|------------------|-----------------|-----------------|-----------------|------------------|-----------------|------------------|
| KOP                   | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar                | 1433.65             | 3909.9              | 4086.82             | 4737.12              | 5262.46             | 183038.79        | 1492.05         | 3919.01         | 4112.02         | 4835.01          | 46677.25        | 183059.85        |
| KOP331                | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar331             | 1438.34             | 3901.82             | 4066.77             | 4407.14              | 7983.33             | 181923.11        | 1590.29         | 3915.01         | 4108.02         | 60322.05         | 136535.04       | 181921.42        |
| Kafka                 | 705.4               | 1946.65             | 3180.49             | 4922.65              | 10657.53            | 678635.0         | 710.35          | 1952.01         | 3187.01         | 4931.01          | 10675.01        | 678633.26        |
| Kafka Ack1            | 190.09              | 718.63              | 1133.08             | 1891.77              | 4976.64             | 677913.86        | 13540.88        | 57882.11        | 82197.5         | 93270.01         | 105154.05       | 640765.12        |
| Pulsar No Journal     | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |
| Pulsar No Journal 331 | N/A                 | N/A                 | N/A                 | N/A                  | N/A                 | N/A              | N/A             | N/A             | N/A             | N/A              | N/A             | N/A              |

## 10. 参考文档

- [StreamNative: Kafka-Pulsar Performance Test Report](https://github.com/streamnative/openmessaging-benchmark/blob/master/blog/benchmarking-pulsar-kafka-a-more-accurate-perspective-on-pulsar-performance.pdf)
- [Confluent: Kafka fastest messaging system](https://www.confluent.io/blog/kafka-fastest-messaging-system/)

