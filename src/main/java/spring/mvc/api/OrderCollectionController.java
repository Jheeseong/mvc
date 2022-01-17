package spring.mvc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.mvc.domain.*;
import spring.mvc.repository.OrderRepository;
import spring.mvc.repository.api.OrderCollectionRepository;
import spring.mvc.repository.api.OrderJpaRepository;
import spring.mvc.repository.api.OrderListQueryDto;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderCollectionController {

    private final OrderRepository orderRepository;
    private final OrderCollectionRepository orderCollectionRepository;

    //1. 엔티티 노출//
    @GetMapping("/api/v1/orders")
    public List<Order> OrderV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getUser().getUsername();
            order.getDelivery().getAddress();
            List<OrderItem> orderItemList = order.getOrderItemList();
            orderItemList.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    //2. DTO 변환//
    @GetMapping("/api/v2/orders")
    public List<OrderDto> OrdersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());

        return result;
    }

    //3. DTO 변환 - join fetch//
    @GetMapping("/api/v3/orders")
    public List<OrderDto> OrderV3() {
        List<Order> orders = orderRepository.findAllOrderItem();
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());
        return result;
    }

    //3.1 DTO 변환 - join fetch//
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> OrderV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findUserDelivery();
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());
        return result;
    }

    //4. JPA에서 DTO 조회
    @GetMapping("/api/v4/orders")
    public List<OrderListQueryDto> OrderV4() {
        List<OrderListQueryDto> result = orderCollectionRepository.findOrderQueryDtos();
        return result;
    }

    //5. JPA에서 DTO 조회 - 컬렉션 조회 최적화
    @GetMapping("/api/v5/orders")
    public List<OrderListQueryDto> OrderV5() {
        List<OrderListQueryDto> result = orderCollectionRepository.findAllByDto_optimization();
        return result;
    }
    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getUser().getUsername();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItemList().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
