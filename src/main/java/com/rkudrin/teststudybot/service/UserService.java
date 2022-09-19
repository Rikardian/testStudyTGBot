package com.rkudrin.teststudybot.service;

import com.rkudrin.teststudybot.dict.RankDictionary;
import com.rkudrin.teststudybot.model.User;
import com.rkudrin.teststudybot.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Component
@Service
@Transactional
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUserById(Message message) {
        Chat chat = message.getChat();

        User user = new User();

        user.setChatId(message.getChatId());
        user.setUserName(chat.getUserName());
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setRank(RankDictionary.FIRST_RANK);
        user.setCurrentStudyStage(1);
        user.setRegisteredAt(LocalDateTime.now());
        user.setTotalStudyStage(1);
        user.setStudyPaid(false);

        userRepository.save(user);
        log.info("user saved: " + user);
    }

    public User findById(Long chatId) {
        return userRepository.findUserByChatId(chatId);
    }

    public void deleteUser(long chatId) {
        userRepository.deleteUserByChatId(chatId);
    }

    public boolean checkUserExist(long chatId) {
        try {
            User user = userRepository.findUserByChatId(chatId);
            return user.getChatId() != null;
        } catch (NullPointerException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public void updateUserCurrentStage(long chatId, String stage) {
        try {
            User user = userRepository.findUserByChatId(chatId);
            user.setCurrentStudyStage(Integer.parseInt(stage));
            userRepository.save(user);
        } catch (NullPointerException e) {
            log.error(e.getMessage());
        }
    }

    public void upUserTotalStage(User user) {
        int updatedStage = user.getCurrentStudyStage() + 1;
        user.setCurrentStudyStage(updatedStage);
        if (updatedStage > user.getTotalStudyStage()) {
            user.setTotalStudyStage(updatedStage);
        }
        userRepository.save(user);
    }

    public void updateUserRank(String rank, long chatId) {
        try {
            User user = userRepository.findUserByChatId(chatId);
            if (!user.getRank().equals(RankDictionary.THIRD_RANK)) {
                user.setRank(rank);
            }
            userRepository.save(user);
        } catch (NullPointerException e) {
            log.error(e.getMessage());
        }
    }
}
