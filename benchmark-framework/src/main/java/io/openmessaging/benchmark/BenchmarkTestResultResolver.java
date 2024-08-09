package io.openmessaging.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.common.protocol.types.Field;

import java.io.File;
import java.io.FileFilter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class BenchmarkTestResultResolver {
    public static void main(String[] args) {
        File dir = new File("/Users/daojun/Desktop/performace_test_result");
        Map<String, Map<String, Map<String, List<TestResult>>>> result = new HashMap<>();
        System.out.println(dir.exists());
        for (File file : dir.listFiles(new Filter())) {
            String driverName = file.getName();
            Map<String, Map<String, List<TestResult>>> driverResult = new HashMap<>();
            for (File subFile : file.listFiles(new Filter())) {
                String rate = subFile.getName();
                Map<String, List<TestResult>> rateResult = new HashMap<>();
                for (File subSubFile : subFile.listFiles(new Filter())) {
                    String partitions = subSubFile.getName();
                    Map<String, List<TestResult>> partitionResult = new HashMap<>();
                    List<TestResult> topicResult = new ArrayList<>();
                    if (subSubFile.isDirectory()) {
                        for (File topicFile : subSubFile.listFiles(new Filter())) {
                            try {
                                TestResult testResult = parseTestResult(topicFile);
                                topicResult.add(testResult);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    partitionResult.put(partitions, topicResult);
                    rateResult.put(partitions, topicResult);
                }

                driverResult.put(rate, rateResult);
            }

            result.put(driverName, driverResult);
        }

        generateReport(result);

        System.out.println();

    }

    private static TestResult parseTestResult(File file) throws Exception {
        String str = FileUtils.readFileToString(file, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(str, TestResult.class);
    }

    private static class Filter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return !pathname.getAbsolutePath().contains("DS_Store");
        }
    }



    private static void generateReport(Map<String, Map<String, Map<String, List<TestResult>>>> result) {
        Map<String, Map<String, List<TestResult>>> kop = result.get("kop");
        Map<String, Map<String, List<TestResult>>> pulsar = result.get("pulsar");
        Map<String, Map<String, List<TestResult>>> kop331 = result.get("kop331");
        Map<String, Map<String, List<TestResult>>> pulsar331 = result.get("pulsar331");

        Map<String, Map<String, List<TestResult>>> kafka = result.get("kafka");
        Map<String, Map<String, List<TestResult>>> kafkaAck1 = result.get("kafka_ack1");
        Map<String, Map<String, List<TestResult>>> pulsarNoJournal = result.get("pulsar_no_journal");
        Map<String, Map<String, List<TestResult>>> pulsarNoJournal331 = result.get("pulsar_no_journal_331");

        List<String> rates = Arrays.asList("10k", "50k", "100k", "maxrate");
        for (String rate : rates) {
            System.out.println("### Publish rate: " + rate);
            Map<String, List<TestResult>> kopRate = kop.get(rate);
            Map<String, List<TestResult>> pulsarRate = pulsar.get(rate);
            Map<String, List<TestResult>> kop331Rate = kop331.get(rate);
            Map<String, List<TestResult>> pulsar331Rate = pulsar331.get(rate);

            Map<String, List<TestResult>> kafkaRate = kafka.get(rate);
            Map<String, List<TestResult>> kafkaAck1Rate = kafkaAck1.get(rate);
            Map<String, List<TestResult>> pulsarNoJournalRate = pulsarNoJournal.get(rate);
            Map<String, List<TestResult>> pulsarNoJournal331Rate = pulsarNoJournal331.get(rate);

            List<String> partitions = Arrays.asList("1-topic-1-partition", "1-topic-16-partition", "1-topic-64-partition", "1-topic-512-partition");
            for (String partition : partitions) {
                System.out.println("#### Topic partitions: " + partition);
                Map<Long, TestResult> kopPartition = kopRate.get(partition).stream().collect(Collectors.toMap(TestResult::getMessageSize, x -> x));
                Map<Long, TestResult> pulsarPartition = pulsarRate.get(partition).stream().collect(Collectors.toMap(TestResult::getMessageSize, x -> x));
                Map<Long, TestResult> kop331Partition = kop331Rate.get(partition).stream().collect(Collectors.toMap(TestResult::getMessageSize, x -> x));
                Map<Long, TestResult> pulsar331Partition = pulsar331Rate.get(partition).stream().collect(Collectors.toMap(TestResult::getMessageSize, x -> x));

                Map<Long, TestResult> kafkaPartiton = kafkaRate.get(partition).stream().collect(Collectors.toMap(TestResult::getMessageSize, x -> x));
                Map<Long, TestResult> kafkaAck1Partition = kafkaAck1Rate.get(partition).stream().collect(Collectors.toMap(TestResult::getMessageSize, x -> x));
                Map<Long, TestResult> pulsarNoJournalPartition = pulsarNoJournalRate.get(partition).stream().collect(Collectors.toMap(TestResult::getMessageSize, x -> x));
                Map<Long, TestResult> pulsarNoJournal331Partition = pulsarNoJournal331Rate.get(partition).stream().collect(Collectors.toMap(TestResult::getMessageSize, x -> x));

                Set<Long> payloadSizes = new TreeSet<>(kopPartition.keySet());
                payloadSizes.addAll(pulsarPartition.keySet());
                payloadSizes.addAll(kop331Partition.keySet());
                payloadSizes.addAll(pulsar331Partition.keySet());

                for (Long payloadSize : payloadSizes) {
                    TestResult kopResult = kopPartition.get(payloadSize);
                    TestResult pulsarResult = pulsarPartition.get(payloadSize);
                    TestResult kop331Result = kop331Partition.get(payloadSize);
                    TestResult pulsar331Result = pulsar331Partition.get(payloadSize);

                    TestResult kafkaResult = kafkaPartiton.get(payloadSize);
                    TestResult kafkaAck1Result = kafkaAck1Partition.get(payloadSize);
                    TestResult pulsarNoJournalResult = pulsarNoJournalPartition.get(payloadSize);
                    TestResult pulsarNoJournal331Result = pulsarNoJournal331Partition.get(payloadSize);

                    System.out.println("##### Payload size: " + payloadSize + " bytes");
                    System.out.println("| | AVG Publish Latency | P95 Publish Latency | P99 Publish Latency | P999 Publish Latency | Max Publish Latency | AVG Publish Rate | AVG E2E Latency | P95 E2E Latency | P99 E2E Latency | P999 E2E Latency | Max E2E Latency | AVG Consume Rate |");
                    System.out.println("| -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- |");
                    printResult(kopResult, "KOP");
                    printResult(pulsarResult, "Pulsar");
                    printResult(kop331Result, "KOP331");
                    printResult(pulsar331Result, "Pulsar331");

                    printResult(kafkaResult, "Kafka");
                    printResult(kafkaAck1Result, "Kafka Ack1");
                    printResult(pulsarNoJournalResult, "Pulsar No Journal");
                    printResult(pulsarNoJournal331Result, "Pulsar No Journal 331");
                }
            }
        }
    }

    public static void printResult(TestResult result, String driverName) {
        if (null == result) {
            System.out.println("|" + driverName + "| N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A |");
        } else {
            System.out.println("|" + driverName + "| " + trimDoubleValue(result.aggregatedPublishLatencyAvg) + " | "
                    + trimDoubleValue(result.aggregatedPublishLatency95pct) + " | " + trimDoubleValue(result.aggregatedPublishLatency99pct)
                    + " | " + trimDoubleValue(result.aggregatedPublishLatency999pct) + " | " + trimDoubleValue(result.aggregatedPublishLatencyMax)
                    + " | " + trimDoubleValue(getAvgPublishRate(result)) + " | " + trimDoubleValue(result.aggregatedEndToEndLatencyAvg)
                    + " | " + trimDoubleValue(result.aggregatedEndToEndLatency95pct) + " | " + trimDoubleValue(result.aggregatedEndToEndLatency99pct)
                    + " | " + trimDoubleValue(result.aggregatedEndToEndLatency999pct) + " | " + trimDoubleValue(result.aggregatedEndToEndLatencyMax)
                    + "|" + trimDoubleValue(getAvgConsumeRate(result)) + " |");
        }
    }

    public static double trimDoubleValue(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }


    public static double getAvgPublishRate(TestResult result) {
        return result.publishRate.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double getAvgConsumeRate(TestResult result) {
        return result.consumeRate.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
}
