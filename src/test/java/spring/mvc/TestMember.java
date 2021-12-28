package spring.mvc;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import spring.mvc.domain.User;
import spring.mvc.domain.UserRepository;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestMember {

    @Autowired
    UserRepository userRepository;
    @Test
    @Transactional
    @Rollback(false)
    public void testMember() {
        User user = new User();
        user.setUsername("memberA");

        Long savedId = userRepository.save(user);

        User findUser = userRepository.find(savedId);

        assertThat(findUser.getId()).isEqualTo(user.getId());

        assertThat(findUser.getUsername()).isEqualTo(user.getUsername());

        assertThat(findUser).isEqualTo(user);

    }
}
