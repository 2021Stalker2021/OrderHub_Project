package oleborn.orderservice.order.metrics.aspect;

import oleborn.orderservice.order.metrics.annotation.BusinessMetric;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BusinessMetricAspect {

    private final MeterRegistry meterRegistry;

    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

    @Around("@annotation(metric)")
    public Object measure(
            ProceedingJoinPoint joinPoint,
            BusinessMetric metric
    ) throws Throwable {

        long startTime = System.nanoTime();

        String status = "success";

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            status = "error";
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;

            recordMetrics(metric, joinPoint, duration, status);
        }
    }

    private void recordMetrics(
            BusinessMetric metric,
            ProceedingJoinPoint joinPoint,
            long duration, String status
    ) {

        try {

            String metricName = metric.value();

            String className = joinPoint.getTarget().getClass().getSimpleName();

            Tags alltags = Tags.of("status", status, "class", className);

            alltags = addCustomTags(alltags, metric.tags());

            Counter.builder("%s.total".formatted(metricName))
                    .tags(alltags)
                    .description("Total calls")
                    .register(meterRegistry)
                    .increment();

            String timerKey = buildTimerKey(metricName, className, metric.tags(), status);

            Timer timer = timers.computeIfAbsent(timerKey, key -> {

                        Tags timerTags = Tags.of("class", className, "status", status);

                        timerTags = addCustomTags(timerTags, metric.tags());

                        return Timer.builder("%s.duration".formatted(metricName))
                                .tags(timerTags)
                                .description("Execution duration")
                                .publishPercentileHistogram()
                                .sla(Duration.ofMillis(50), Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofSeconds(2))
                                .register(meterRegistry);
                    }
            );

            timer.record(duration, TimeUnit.NANOSECONDS);
        }  catch (Exception e) {

            log.warn("Запись метрик завершена с ошибкой: {}", e.getMessage());
        }
    }

    private String buildTimerKey(
            String metricName,
            String className,
            String[] tags,
            String status
    ) {
        StringBuilder key = new StringBuilder();
        key.append(metricName).append(".").append(className);

        if (tags != null && tags.length > 0) {

            String[] sorted = tags.clone();

            Arrays.sort(sorted);

            for (String tag : sorted) {
                key.append(".").append(tag);
            }
        }

        key.append(".").append(status);

        return key.toString();
    }

    private Tags addCustomTags(
            Tags alltags,
            String[] tags
    ) {
        if (tags == null || tags.length == 0) {
            return alltags;
        }

        Tags result = alltags;

        for (String tag : tags) {
            int index = tag.indexOf('=');

            if (index > 0) {

                String key = tag.substring(0, index).trim();
                String value = tag.substring(index + 1).trim();

                result = result.and(key, value);
            }
        }

        return result;
    }
}
