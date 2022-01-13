package spring.mvc.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Top")
@Getter
@Setter
public class Top extends Item {

    private String size;
    private String color;

    //정적 펙토리 메서드//
    public static Top createTop(String name, int price, int stockQuantity, String size, String color){
        Top top = new Top();
        top.setName(name);
        top.setPrice(price);
        top.setStockQuantity(stockQuantity);
        top.setSize(size);
        top.setColor(color);

        return top;
    }
}
