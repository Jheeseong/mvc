package spring.mvc.service;

import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.mvc.domain.*;
import spring.mvc.domain.item.Item;
import spring.mvc.repository.ItemRepository;
import spring.mvc.repository.OrderRepository;
import spring.mvc.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    //주문//
    @Transactional
    public Long order(Long userId, Long itemId, int count) {
        User user = userRepository.findOne(userId);
        Item item = itemRepository.findOne(itemId);

        Delivery delivery = new Delivery();
        delivery.setAddress(user.getAddress());
        delivery.setStatus(DeliveryStatus.READY);

        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(),count);

        Order order = Order.createOrder(user,delivery,orderItem);

        orderRepository.save(order);

        return order.getId();
    }
    //주문 취소//
    @Transactional
    public void orderCancel(Long orderId) {
        Order order = orderRepository.findOne(orderId);

        order.cancel();
    }

    //검색
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
    }
}
