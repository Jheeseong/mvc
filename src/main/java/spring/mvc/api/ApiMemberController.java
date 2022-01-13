package spring.mvc.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import spring.mvc.domain.User;
import spring.mvc.service.UserService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ApiMemberController {

    private final UserService userService;

    @PostMapping("/api/v1/members")
    public UserResponse saveUserV1(@RequestBody @Valid User user) {
        Long id = userService.join(user);
        String name = user.getUsername();

        return new UserResponse(id,name);
    }

    @Data
    static class UserResponse {
        private Long id;
        private String name;

        public UserResponse(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @PostMapping("/api/v2/members")
    public UserResponse saveUserV2(@RequestBody @Valid UserDto dto) {

        User user = new User();
        user.setUsername(dto.getName());

        Long id = userService.join(user);
        String name = dto.getName();
        return new UserResponse(id,name);
    }

    @Data
    static class UserDto{
        private Long id;
        private String name;
    }
}
