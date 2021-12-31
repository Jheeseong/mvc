package spring.mvc.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderSearch {

    private String UserName;
    private OrderStatus orderStatus;
}
