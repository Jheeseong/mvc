package spring.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import spring.mvc.domain.Address;
import spring.mvc.domain.User;
import spring.mvc.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(value = "/users/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "member/createUserForm";
    }

    @PostMapping(value = "/member/new")
    public String create(@Valid UserForm form, BindingResult result) {

        if(result.hasErrors()) {
            return "member/createUserForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        User user = new User();
        user.setUsername(form.getUsername());
        user.setAddress(address);

        userService.join(user);

        return "redirect:/";
    }

    @GetMapping(value = "/users")
    public String list(Model model) {
        List<User> users = userService.findUsers();
        model.addAttribute("users", users);

        return "users/userList";
    }
}
