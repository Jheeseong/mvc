package spring.mvc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.mvc.domain.Address;
import spring.mvc.domain.User;
import spring.mvc.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ApiMemberController {

    private final UserService userService;
    // 회원등록 - 엔티티를 직접 매핑
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
    //회원등록 - DTO를 매핑
    @PostMapping("/api/v2/members")
    public UserResponse saveUserV2(@RequestBody @Valid UserDto dto) {

        User user = new User();
        user.setUsername(dto.getName());

        Long id = userService.join(user);
        String name = dto.getName();
        return new UserResponse(id,name);
    }

    @Data
    @AllArgsConstructor
    static class UserDto{
        private String name;

    }

    //회원 수정
    @PutMapping("/api/v2/members/{id}")
    public UpdateUser updateUserV2(@PathVariable("id") Long id,
                                   @RequestBody @Valid UserDto dto){
        userService.update(id, dto.getName());
        User userFind = userService.findOne(id);
        return new UpdateUser(userFind.getId(),userFind.getUsername());
    }

    @Data
    @AllArgsConstructor
    static class UpdateUser {
        private Long id;
        private String name;
    }

    //회원 조회 - 엔티티 직접 조회
    @GetMapping("/api/v1/members")
    public List<User> UsersV1() {
        return userService.findUsers();
    }

    //회원 조회 - DTO 조회
    @GetMapping("/api/v2/members")
    public Result UsersV2() {

        List<User> findUsers = userService.findUsers();

        List<UserDto> collect = findUsers.stream()
                .map(u -> new UserDto(u.getUsername()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
}
