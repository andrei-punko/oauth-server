package by.andd3dfx.auth.repository;

import by.andd3dfx.auth.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByLogin(String login);
}
