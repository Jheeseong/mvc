package spring.mvc.repository.api;

import lombok.Data;
import spring.mvc.domain.Address;
import spring.mvc.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderListQueryDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    public OrderListQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
