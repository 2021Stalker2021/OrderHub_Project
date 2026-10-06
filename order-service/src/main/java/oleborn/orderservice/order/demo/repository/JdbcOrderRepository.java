package oleborn.orderservice.order.demo.repository;

import oleborn.orderservice.order.domain.Order;
import oleborn.orderservice.order.domain.OrderItem;
import oleborn.orderservice.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository("jdbcOrderRepository")
@RequiredArgsConstructor
public class JdbcOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SET_ORDER = """
            INSERT INTO orders (status, create_at, order_number)
            VALUES (?, ?, ?)
            """;

    private static final String SET_ORDER_ITEMS = """
            INSERT INTO order_items (order_id, product_id, product_name, quantity, price)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final RowMapper<Order> orderRowMapper =
            (rs, rowNum) -> {
                Order order = new Order();

                order.setId(rs.getLong("id"));
                order.setStatus(OrderStatus.valueOf(rs.getString("status")));
                order.setOrderNumber(rs.getString("order_number"));
                order.setCreatedAt(rs.getTimestamp("create_at").toInstant());
                return order;
            };

    private final RowMapper<OrderItem> itemRowMapper =
            (rs, rowNum) -> {
                OrderItem item = new OrderItem();
                item.setId(rs.getLong("id"));
                item.setProductId(rs.getLong("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                return item;
            };

    @Transactional
    public Order save(Order order) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SET_ORDER, Statement.RETURN_GENERATED_KEYS);

                    ps.setString(1, order.getStatus().toString());
                    ps.setTimestamp(2, Timestamp.from(order.getCreatedAt()));
                    ps.setString(3, order.getOrderNumber());

                    return ps;
                },
                keyHolder
        );

        Map<String, Object> keys = keyHolder.getKeys();

        Long orderId = null;

        if (keys != null) {
            if (keys.containsKey("order_id")) {
                orderId = ((Number) keys.get("id")).longValue();
            }

            for (Object value : keys.values()) {
                if (value instanceof Number) {
                    orderId = ((Number) value).longValue();
                    break;
                }
            }
        }

        if (orderId == null) {
            throw new RuntimeException("Order id not found");
        }

        order.setId(orderId);

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            Long finalOrderId = orderId;

            List<Object[]> batchArgs = order.getItems().stream()
                    .map(
                            item -> new Object[]{
                                    finalOrderId,
                                    item.getProductId(),
                                    item.getProductName(),
                                    item.getQuantity(),
                                    item.getPrice()
                            }
                    )
                    .toList();

            jdbcTemplate.batchUpdate(SET_ORDER_ITEMS, batchArgs);
        }

        return order;
    }
}