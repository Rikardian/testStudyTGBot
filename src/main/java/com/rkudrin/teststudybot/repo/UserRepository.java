package com.rkudrin.teststudybot.repo;

import com.rkudrin.teststudybot.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
     User findUserByChatId(Long id);
     void deleteUserByChatId(Long id);
}
