package spring.mvc.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Hat")
@Getter @Setter
public class Hat extends Item {

    private String color;
    private String patten;
}
