package spring.mvc.repository.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderCollectionRepository {

    private final EntityManager em;

    public List<OrderListQueryDto> findOrderQueryDtos() {
        List<OrderListQueryDto> result = findOrders();
        result.forEach(o ->{
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderListQueryDto> findOrders() {
        return em.createQuery("select new spring.mvc.repository.api.OrderListQueryDto(o.id, m.username, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.user m" +
                " join o.delivery d",OrderListQueryDto.class)
                .getResultList();
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em. createQuery("select new spring.mvc.repository.api.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
}
