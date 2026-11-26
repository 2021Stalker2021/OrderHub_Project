package oleborn.orderservice.order.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oleborn.orderservice.order.dictionary.OrderStatus;
import oleborn.orderservice.order.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMetricsBinder implements MeterBinder {

    private final OrderRepository orderRepository;

    @Override
    public void bindTo(MeterRegistry registry) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            Gauge.builder("orders.pending.count", orderRepository,
                            orderRepository -> orderRepository.countByStatus(orderStatus))
                    .tags("order.status", orderStatus.name())
                    .description("Orders pending count for status: " + orderStatus)
                    .register(registry);
        }
    }
}
