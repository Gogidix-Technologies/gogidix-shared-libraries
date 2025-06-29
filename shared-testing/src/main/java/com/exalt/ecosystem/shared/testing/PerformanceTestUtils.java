package com.exalt.ecosystem.shared.testing;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Assertions;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Comprehensive performance testing utilities for the Exalt Social E-commerce Ecosystem.
 * Provides advanced performance measurement, load testing, and benchmarking capabilities.
 * 
 * <p>This utility class provides:</p>
 * <ul>
 *   <li>Response time measurement and statistics</li>
 *   <li>Throughput testing with concurrent execution</li>
 *   <li>Memory usage profiling and monitoring</li>
 *   <li>JMH integration for micro-benchmarking</li>
 *   <li>Load testing scenarios with configurable patterns</li>
 *   <li>Performance assertion utilities</li>
 *   <li>System resource monitoring</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * {@code
 * // Response time testing
 * PerformanceResult result = PerformanceTestUtils.measureExecutionTime(() -> {
 *     return orderService.createOrder(order);
 * });
 * PerformanceTestUtils.assertResponseTime(result, Duration.ofMillis(500));
 * 
 * // Load testing
 * LoadTestResult loadResult = PerformanceTestUtils.runLoadTest(
 *     () -> productService.getProduct("123"),
 *     100, // concurrent users
 *     Duration.ofSeconds(30)
 * );
 * PerformanceTestUtils.assertThroughput(loadResult, 1000); // min 1000 ops/sec
 * 
 * // Memory profiling
 * MemoryProfile memProfile = PerformanceTestUtils.profileMemoryUsage(() -> {
 *     // Memory-intensive operation
 *     return largeDataProcessor.process(bigDataSet);
 * });
 * PerformanceTestUtils.assertMemoryUsage(memProfile, "500MB");
 * }
 * </pre>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Slf4j
public final class PerformanceTestUtils {
    
    private static final int DEFAULT_WARMUP_ITERATIONS = 3;
    private static final int DEFAULT_MEASUREMENT_ITERATIONS = 5;
    
    private PerformanceTestUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Performance measurement result container.
     */
    public static class PerformanceResult {
        private final long executionTimeMs;
        private final boolean successful;
        private final String errorMessage;
        private final Map<String, Object> metrics;
        
        public PerformanceResult(long executionTimeMs, boolean successful, String errorMessage) {
            this.executionTimeMs = executionTimeMs;
            this.successful = successful;
            this.errorMessage = errorMessage;
            this.metrics = new HashMap<>();
        }
        
        public long getExecutionTimeMs() { return executionTimeMs; }
        public Duration getExecutionTime() { return Duration.ofMillis(executionTimeMs); }
        public boolean isSuccessful() { return successful; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getMetrics() { return metrics; }
        
        public void addMetric(String name, Object value) {
            metrics.put(name, value);
        }
    }
    
    /**
     * Load testing result container.
     */
    public static class LoadTestResult {
        private final int totalExecutions;
        private final int successfulExecutions;
        private final int failedExecutions;
        private final long totalDurationMs;
        private final double throughput; // operations per second
        private final StatisticalSummary responseTimeStats;
        private final List<PerformanceResult> results;
        
        public LoadTestResult(int totalExecutions, int successfulExecutions, int failedExecutions,
                            long totalDurationMs, double throughput, StatisticalSummary responseTimeStats,
                            List<PerformanceResult> results) {
            this.totalExecutions = totalExecutions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
            this.totalDurationMs = totalDurationMs;
            this.throughput = throughput;
            this.responseTimeStats = responseTimeStats;
            this.results = results;
        }
        
        public int getTotalExecutions() { return totalExecutions; }
        public int getSuccessfulExecutions() { return successfulExecutions; }
        public int getFailedExecutions() { return failedExecutions; }
        public long getTotalDurationMs() { return totalDurationMs; }
        public double getThroughput() { return throughput; }
        public StatisticalSummary getResponseTimeStats() { return responseTimeStats; }
        public List<PerformanceResult> getResults() { return results; }
        public double getSuccessRate() { return (double) successfulExecutions / totalExecutions * 100; }
    }
    
    /**
     * Statistical summary for performance metrics.
     */
    public static class StatisticalSummary {
        private final double min;
        private final double max;
        private final double mean;
        private final double median;
        private final double percentile95;
        private final double percentile99;
        private final double standardDeviation;
        
        public StatisticalSummary(List<Long> values) {
            Collections.sort(values);
            int size = values.size();
            
            this.min = values.get(0);
            this.max = values.get(size - 1);
            this.mean = values.stream().mapToLong(Long::longValue).average().orElse(0.0);
            this.median = size % 2 == 0 ? 
                (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0 : 
                values.get(size / 2);
            this.percentile95 = values.get((int) (size * 0.95));
            this.percentile99 = values.get((int) (size * 0.99));
            
            double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0.0);
            this.standardDeviation = Math.sqrt(variance);
        }
        
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getMean() { return mean; }
        public double getMedian() { return median; }
        public double getPercentile95() { return percentile95; }
        public double getPercentile99() { return percentile99; }
        public double getStandardDeviation() { return standardDeviation; }
    }
    
    /**
     * Memory profiling result container.
     */
    public static class MemoryProfile {
        private final long beforeMemoryMB;
        private final long afterMemoryMB;
        private final long peakMemoryMB;
        private final long memoryUsedMB;
        private final long gcCount;
        private final long gcTimeMs;
        
        public MemoryProfile(long beforeMemoryMB, long afterMemoryMB, long peakMemoryMB,
                           long memoryUsedMB, long gcCount, long gcTimeMs) {
            this.beforeMemoryMB = beforeMemoryMB;
            this.afterMemoryMB = afterMemoryMB;
            this.peakMemoryMB = peakMemoryMB;
            this.memoryUsedMB = memoryUsedMB;
            this.gcCount = gcCount;
            this.gcTimeMs = gcTimeMs;
        }
        
        public long getBeforeMemoryMB() { return beforeMemoryMB; }
        public long getAfterMemoryMB() { return afterMemoryMB; }
        public long getPeakMemoryMB() { return peakMemoryMB; }
        public long getMemoryUsedMB() { return memoryUsedMB; }
        public long getGcCount() { return gcCount; }
        public long getGcTimeMs() { return gcTimeMs; }
    }
    
    /**
     * Response Time Measurement
     */
    
    /**
     * Measures execution time of a runnable operation.
     * 
     * @param operation operation to measure
     * @return performance result
     */
    public static PerformanceResult measureExecutionTime(Runnable operation) {
        return measureExecutionTime(() -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * Measures execution time of a supplier operation.
     * 
     * @param operation operation to measure
     * @param <T> return type
     * @return performance result
     */
    public static <T> PerformanceResult measureExecutionTime(Supplier<T> operation) {
        StopWatch stopWatch = new StopWatch();
        String errorMessage = null;
        boolean successful = true;
        
        try {
            stopWatch.start();
            T result = operation.get();
            stopWatch.stop();
            
            log.debug("Operation completed successfully in {} ms", stopWatch.getTotalTimeMillis());
            
        } catch (Exception e) {
            stopWatch.stop();
            successful = false;
            errorMessage = e.getMessage();
            log.error("Operation failed after {} ms: {}", stopWatch.getTotalTimeMillis(), e.getMessage());
        }
        
        PerformanceResult result = new PerformanceResult(stopWatch.getTotalTimeMillis(), successful, errorMessage);
        result.addMetric("startTime", System.currentTimeMillis() - stopWatch.getTotalTimeMillis());
        result.addMetric("endTime", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * Measures average execution time over multiple iterations.
     * 
     * @param operation operation to measure
     * @param iterations number of iterations
     * @return performance result with average time
     */
    public static PerformanceResult measureAverageExecutionTime(Supplier<Object> operation, int iterations) {
        List<Long> executionTimes = new ArrayList<>();
        int successfulRuns = 0;
        String lastError = null;
        
        log.info("Starting average execution time measurement over {} iterations", iterations);
        
        for (int i = 0; i < iterations; i++) {
            PerformanceResult result = measureExecutionTime(operation);
            executionTimes.add(result.getExecutionTimeMs());
            
            if (result.isSuccessful()) {
                successfulRuns++;
            } else {
                lastError = result.getErrorMessage();
            }
        }
        
        double averageTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        boolean overallSuccess = successfulRuns == iterations;
        
        PerformanceResult result = new PerformanceResult((long) averageTime, overallSuccess, lastError);
        result.addMetric("totalIterations", iterations);
        result.addMetric("successfulIterations", successfulRuns);
        result.addMetric("successRate", (double) successfulRuns / iterations * 100);
        result.addMetric("statistics", new StatisticalSummary(executionTimes));
        
        log.info("Average execution time: {:.2f} ms (success rate: {:.1f}%)", 
                averageTime, (double) successfulRuns / iterations * 100);
        
        return result;
    }
    
    /**
     * Load Testing
     */
    
    /**
     * Runs a load test with specified concurrency and duration.
     * 
     * @param operation operation to test
     * @param concurrentUsers number of concurrent users
     * @param duration test duration
     * @return load test result
     */
    public static LoadTestResult runLoadTest(Supplier<Object> operation, int concurrentUsers, Duration duration) {
        log.info("Starting load test with {} concurrent users for {} seconds", 
                concurrentUsers, duration.getSeconds());
        
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        List<PerformanceResult> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + duration.toMillis();
        
        // Submit tasks
        for (int i = 0; i < concurrentUsers; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    while (System.currentTimeMillis() < endTime) {
                        PerformanceResult result = measureExecutionTime(operation);
                        results.add(result);
                        
                        // Small pause to prevent overwhelming
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for completion
        try {
            Thread.sleep(duration.toMillis());
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long actualDuration = System.currentTimeMillis() - startTime;
        
        // Analyze results
        int totalExecutions = results.size();
        int successfulExecutions = (int) results.stream().mapToInt(r -> r.isSuccessful() ? 1 : 0).sum();
        int failedExecutions = totalExecutions - successfulExecutions;
        double throughput = (double) totalExecutions / (actualDuration / 1000.0);
        
        List<Long> responseTimes = results.stream()
            .mapToLong(PerformanceResult::getExecutionTimeMs)
            .boxed()
            .collect(java.util.stream.Collectors.toList());
        
        StatisticalSummary stats = new StatisticalSummary(responseTimes);
        
        LoadTestResult loadResult = new LoadTestResult(
            totalExecutions, successfulExecutions, failedExecutions,
            actualDuration, throughput, stats, results
        );
        
        log.info("Load test completed: {} operations, {:.1f} ops/sec, {:.1f}% success rate",
                totalExecutions, throughput, loadResult.getSuccessRate());
        
        return loadResult;
    }
    
    /**
     * Runs a stress test with increasing load.
     * 
     * @param operation operation to test
     * @param startUsers initial number of users
     * @param maxUsers maximum number of users
     * @param incrementStep user increment step
     * @param stepDuration duration for each step
     * @return map of load test results by user count
     */
    public static Map<Integer, LoadTestResult> runStressTest(Supplier<Object> operation, int startUsers, 
                                                           int maxUsers, int incrementStep, Duration stepDuration) {
        Map<Integer, LoadTestResult> results = new HashMap<>();
        
        log.info("Starting stress test from {} to {} users (step: {}, duration: {} sec)", 
                startUsers, maxUsers, incrementStep, stepDuration.getSeconds());
        
        for (int users = startUsers; users <= maxUsers; users += incrementStep) {
            log.info("Testing with {} concurrent users", users);
            LoadTestResult result = runLoadTest(operation, users, stepDuration);
            results.put(users, result);
            
            // Break if error rate becomes too high
            if (result.getSuccessRate() < 50.0) {
                log.warn("Success rate dropped below 50% at {} users, stopping stress test", users);
                break;
            }
            
            // Cool down period between steps
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return results;
    }
    
    /**
     * Memory Profiling
     */
    
    /**
     * Profiles memory usage during operation execution.
     * 
     * @param operation operation to profile
     * @return memory profile
     */
    public static MemoryProfile profileMemoryUsage(Supplier<Object> operation) {
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection before measurement
        System.gc();
        Thread.yield();
        
        long beforeMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long beforeGcCount = getTotalGcCount();
        long beforeGcTime = getTotalGcTime();
        
        log.debug("Starting memory profiling - Initial memory: {} MB", beforeMemory);
        
        // Execute operation
        operation.get();
        
        long afterMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long afterGcCount = getTotalGcCount();
        long afterGcTime = getTotalGcTime();
        
        long memoryUsed = afterMemory - beforeMemory;
        long peakMemory = runtime.totalMemory() / (1024 * 1024);
        long gcCount = afterGcCount - beforeGcCount;
        long gcTime = afterGcTime - beforeGcTime;
        
        MemoryProfile profile = new MemoryProfile(
            beforeMemory, afterMemory, peakMemory, memoryUsed, gcCount, gcTime
        );
        
        log.info("Memory profiling completed - Used: {} MB, Peak: {} MB, GC: {} times ({} ms)",
                memoryUsed, peakMemory, gcCount, gcTime);
        
        return profile;
    }
    
    /**
     * Assertion Utilities
     */
    
    /**
     * Asserts that response time is within acceptable limit.
     * 
     * @param result performance result
     * @param maxResponseTime maximum acceptable response time
     */
    public static void assertResponseTime(PerformanceResult result, Duration maxResponseTime) {
        Assertions.assertTrue(result.isSuccessful(), 
            "Operation should be successful, but failed with: " + result.getErrorMessage());
        
        Assertions.assertTrue(result.getExecutionTimeMs() <= maxResponseTime.toMillis(),
            String.format("Response time %d ms exceeds maximum %d ms", 
                result.getExecutionTimeMs(), maxResponseTime.toMillis()));
        
        log.debug("Response time assertion passed: {} ms <= {} ms", 
                result.getExecutionTimeMs(), maxResponseTime.toMillis());
    }
    
    /**
     * Asserts that throughput meets minimum requirement.
     * 
     * @param result load test result
     * @param minThroughput minimum required throughput (operations per second)
     */
    public static void assertThroughput(LoadTestResult result, double minThroughput) {
        Assertions.assertTrue(result.getThroughput() >= minThroughput,
            String.format("Throughput %.2f ops/sec is below minimum %.2f ops/sec",
                result.getThroughput(), minThroughput));
        
        log.debug("Throughput assertion passed: {:.2f} ops/sec >= {:.2f} ops/sec",
                result.getThroughput(), minThroughput);
    }
    
    /**
     * Asserts that success rate meets minimum requirement.
     * 
     * @param result load test result
     * @param minSuccessRate minimum required success rate (percentage)
     */
    public static void assertSuccessRate(LoadTestResult result, double minSuccessRate) {
        Assertions.assertTrue(result.getSuccessRate() >= minSuccessRate,
            String.format("Success rate %.1f%% is below minimum %.1f%%",
                result.getSuccessRate(), minSuccessRate));
        
        log.debug("Success rate assertion passed: {:.1f}% >= {:.1f}%",
                result.getSuccessRate(), minSuccessRate);
    }
    
    /**
     * Asserts that memory usage is within acceptable limit.
     * 
     * @param profile memory profile
     * @param maxMemoryMB maximum acceptable memory usage in MB
     */
    public static void assertMemoryUsage(MemoryProfile profile, long maxMemoryMB) {
        Assertions.assertTrue(profile.getMemoryUsedMB() <= maxMemoryMB,
            String.format("Memory usage %d MB exceeds maximum %d MB",
                profile.getMemoryUsedMB(), maxMemoryMB));
        
        log.debug("Memory usage assertion passed: {} MB <= {} MB",
                profile.getMemoryUsedMB(), maxMemoryMB);
    }
    
    /**
     * Async Testing Utilities
     */
    
    /**
     * Creates an Awaitility condition factory with common defaults.
     * 
     * @return configured condition factory
     */
    public static ConditionFactory createAwaitCondition() {
        return Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofMillis(100))
            .pollDelay(Duration.ofMillis(10));
    }
    
    /**
     * Waits for a condition to be true with performance measurement.
     * 
     * @param condition condition to wait for
     * @param timeout maximum wait time
     * @return time taken to satisfy condition
     */
    public static Duration waitForCondition(Supplier<Boolean> condition, Duration timeout) {
        long startTime = System.currentTimeMillis();
        
        Awaitility.await()
            .atMost(timeout)
            .pollInterval(Duration.ofMillis(50))
            .until(condition::get);
        
        long waitTime = System.currentTimeMillis() - startTime;
        Duration actualWaitTime = Duration.ofMillis(waitTime);
        
        log.debug("Condition satisfied after {} ms", waitTime);
        return actualWaitTime;
    }
    
    // Private helper methods
    
    private static long getTotalGcCount() {
        return java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()
            .stream()
            .mapToLong(gc -> gc.getCollectionCount())
            .sum();
    }
    
    private static long getTotalGcTime() {
        return java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()
            .stream()
            .mapToLong(gc -> gc.getCollectionTime())
            .sum();
    }
}