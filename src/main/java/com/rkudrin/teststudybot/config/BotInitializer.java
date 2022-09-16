package com.rkudrin.teststudybot.config;

import com.rkudrin.teststudybot.service.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
public class BotInitializer {

    TelegramBot bot;

    @Autowired
    public BotInitializer(TelegramBot bot) {
        this.bot = bot;
    }

    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try{
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e){
            log.error("Произошла ошибка " + e.getMessage());
        }
    }
}