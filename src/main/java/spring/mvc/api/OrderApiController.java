package spring.mvc.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.mvc.domain.*;
import spring.mvc.repository.OrderRepository;
import spring.mvc.repository.api.OrderJpaRepository;
import spring.mvc.repository.api.OrderQueryDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderJpaRepository orderJpaRepository;

    //v1 : 엔티티 직접 노출//
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getUser().getUsername();
            order.getDelivery().getAddress();
        }
        return all;
    }

    //2. DTO 사용//
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getUser().getUsername();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
    //3. join fetch 사용
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {

        List<Order> orders = orderRepository.findUserDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }
    //4. JPA를 사용한 DTO
    @GetMapping("/api/v4/simple-orders")
    public List<OrderQueryDto> ordersV4() {
        return orderJpaRepository.findQueryDto();
    }
}
