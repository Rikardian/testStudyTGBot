package com.rkudrin.teststudybot.service;

import com.rkudrin.teststudybot.config.BotConfig;
import com.rkudrin.teststudybot.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private final UserRepository userRepository;

    @Autowired
    public TelegramBot(BotConfig config, UserRepository userRepository) {
        this.config = config;
        this.userRepository = userRepository;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

    }
}
