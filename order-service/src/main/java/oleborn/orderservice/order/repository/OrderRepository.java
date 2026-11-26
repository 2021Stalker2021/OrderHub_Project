package oleborn.orderservice.order.repository;

import oleborn.orderservice.order.dictionary.OrderStatus;
import oleborn.orderservice.order.domain.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findWithItemsById(Long id);

    List<Order> findByStatus(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);
}
