package spring.mvc.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class UserForm {

    @NotEmpty(message = "회원이름은 필수 입니다.")
    private String Username;

    private String city;
    private String street;
    private String zipcode;
}
