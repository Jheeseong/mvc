package spring.mvc.domain.item;

import lombok.Getter;
import lombok.Setter;
import spring.mvc.domain.Category;
import spring.mvc.exception.NotEnoughStockException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany (mappedBy = "items")
    private List<Category> categoryList = new ArrayList<>();

    //비지니스 로직//
    public void addStock(int quantity) {
        stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int remainingStock = stockQuantity - quantity;
        if(remainingStock < 0) {
            throw new NotEnoughStockException("재고가 부족합니다.");
        }
        stockQuantity = remainingStock;
    }
}
