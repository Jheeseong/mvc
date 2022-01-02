package spring.mvc.controller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TopForm {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private String color;
    private String size;
}
