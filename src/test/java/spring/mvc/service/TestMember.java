package spring.mvc.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import spring.mvc.domain.User;
import spring.mvc.repository.UserRepository;
import spring.mvc.service.UserService;


import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class TestMember {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;

    @Test
    public void SignIn() throws Exception {
            //given
            User user = new User();
            user.setUsername("kim");

            //when
            Long saveId = userService.join(user);

            //then
            assertEquals(user,userRepository.findOne(saveId));
        }

    @Test(expected = IllegalStateException.class)
    public void duplicateMember() throws Exception {
        //given
        User user1 = new User();
        user1.setUsername("kim");

        User user2 = new User();
        user2.setUsername("kim");
        //when
        userService.join(user1);
        userService.join(user2);
        //then
        fail("예외가 발생해야 한다.");
    }
}

