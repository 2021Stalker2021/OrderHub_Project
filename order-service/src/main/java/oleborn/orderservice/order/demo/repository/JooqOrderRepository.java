package oleborn.orderservice.order.demo.repository;

import oleborn.assistant_core.jooq.tables.records.OrderItemsRecord;
import oleborn.assistant_core.jooq.tables.records.OrdersRecord;
import oleborn.orderservice.order.Order;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static oleborn.assistant_core.jooq.tables.OrderItems.ORDER_ITEMS;
import static oleborn.assistant_core.jooq.tables.Orders.ORDERS;

@Repository
@RequiredArgsConstructor
public class JooqOrderRepository {

    private final DSLContext dsl;

    public Order save(Order order) {

        OrdersRecord ordersRecord = dsl.newRecord(ORDERS);
        ordersRecord.setStatus(order.getStatus().name());
        OffsetDateTime offsetDateTime = order.getCreatedAt().atOffset(ZoneOffset.UTC);
        ordersRecord.setCreateAt(offsetDateTime);
        ordersRecord.setOrderNumber(order.getOrderNumber());

        ordersRecord.store();

        Long orderId = ordersRecord.getId();

        order.setId(orderId);

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<OrderItemsRecord> orderItems = order.getItems().stream()
                    .map(item ->
                            {
                                OrderItemsRecord record = dsl.newRecord(ORDER_ITEMS);
                                record.setOrderId(orderId);
                                record.setProductId(item.getProductId());
                                record.setQuantity(item.getQuantity());
                                record.setPrice(item.getPrice());
                                record.setProductName(item.getProductName());
                                return record;
                            }
                            )
                    .toList();

            dsl.batchInsert(orderItems).execute();
        }
        return order;
    }
}
