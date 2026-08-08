package gsc.edward.OrderHub_Project.order.repository;

import gsc.edward.OrderHub_Project.order.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findWithItemsById(Long id);
}
