package by.andd3dfx.auth.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import by.andd3dfx.auth.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserRepositoryTest {

    private User entity1 = buildUser("strange login");
    private User entity2 = buildUser("strange login 2");
    private User entity3 = buildUser("strange login 3");

    @Autowired
    UserRepository userRepository;

    @Before
    public void setUp() {
        userRepository.save(entity1);
        userRepository.save(entity2);
        userRepository.save(entity3);
    }

    @Test
    public void findByLogin() {
        User result = userRepository.findByLogin(entity2.getLogin());

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), is(entity2.getName()));
        assertThat(result.getLogin(), is(entity2.getLogin()));
        assertThat(result.getPassword(), is(entity2.getPassword()));
        assertThat(result.getNonPiiId(), is(entity2.getNonPiiId()));
    }

    private User buildUser(String login) {
        User entity = new User();
        entity.setName("some-name");
        entity.setLogin(login);
        entity.setPassword("passwd");
        entity.setNonPiiId("non-pii-id");
        return entity;
    }
}
