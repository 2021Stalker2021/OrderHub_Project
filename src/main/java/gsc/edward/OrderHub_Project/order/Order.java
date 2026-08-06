package gsc.edward.OrderHub_Project.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

//    @Column(name = "order_number", nullable = false, unique = true)
//    private String orderNumber;

    @Column(name = "create_at")
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    public Order(List<OrderItem> items) {
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
        this.items.addAll(items);
//        this.orderNumber = UUID.randomUUID().toString();
    }
}
