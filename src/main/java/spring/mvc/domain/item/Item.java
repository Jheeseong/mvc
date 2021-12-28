package spring.mvc.domain.item;

import lombok.Getter;
import lombok.Setter;
import spring.mvc.domain.Category;

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
}
