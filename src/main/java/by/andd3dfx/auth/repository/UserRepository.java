package by.andd3dfx.auth.repository;

import by.andd3dfx.auth.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    User findByLogin(String login);
}
