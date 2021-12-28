package spring.mvc.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Top")
@Getter
@Setter
public class Top {

    private String size;
    private String color;
}
