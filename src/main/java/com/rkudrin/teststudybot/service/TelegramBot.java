package com.rkudrin.teststudybot.service;

import com.rkudrin.teststudybot.config.BotConfig;
import com.rkudrin.teststudybot.dict.ButtonDictionary;
import com.rkudrin.teststudybot.dict.RankDictionary;
import com.rkudrin.teststudybot.model.PaymentsData;
import com.rkudrin.teststudybot.model.User;
import com.rkudrin.teststudybot.repo.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButtonPollType;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private final UserRepository userRepository;

    @Autowired
    public TelegramBot(BotConfig config, UserRepository userRepository) {
        this.config = config;
        this.userRepository = userRepository;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать работу"));
        listOfCommands.add(new BotCommand("/study", "Перейти к обучению"));
        listOfCommands.add(new BotCommand("/getdata", "Получить ваши данные"));
        listOfCommands.add(new BotCommand("/deletedata", "Удалить ваши данные"));
        listOfCommands.add(new BotCommand("/help", "Информация об использовании бота"));
        listOfCommands.add(new BotCommand("/settings", "Поменять ваши настройки"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка настройки командного листа бота: " + e.getMessage());
        }
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

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    registerUser(message);
                    startCommandReceived(chatId, message.getChat().getFirstName());
                    break;
                default:
                    messageExecute(getMessage(chatId, "Извини, я не знаю такую команду"));
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = EmojiParser.parseToUnicode("Привет, " + firstName +
                "! Я помогу тебе на прохождении всего этапа твоего обучения. " + ":+1:" + "\n\n" +
                "Начнем?" + ":wink:");
        log.info("Replied to user " + firstName);

        SendMessage message = getMessage(chatId, answer);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        InlineKeyboardButton agreeButton = new InlineKeyboardButton();
        agreeButton.setText("Начнём!");
        agreeButton.setCallbackData(ButtonDictionary.STUDY_BUTTON);

        rowInLine.add(agreeButton);
        rowsInLine.add(rowInLine);
        keyboardMarkup.setKeyboard(rowsInLine);
        message.setReplyMarkup(keyboardMarkup);

        messageExecute(message);
    }

    //Формирует сообщение дял отправки
    private SendMessage getMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        return message;
    }

    //Отправляет сообщение
    private void messageExecute(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e){
            log.error("Произошла ошибка при отправке сообщения: " + e.getMessage());
        }
    }

    //Регистрация нового пользователя
    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()){

            Long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setUserName(chat.getUserName());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRank(RankDictionary.firstRank);
            user.setCurrentStudyStage(0);
            user.setRegisteredAt(LocalDateTime.now());
            user.setTotalStudyStage(0);
            user.setStudyPaid(false);

            userRepository.save(user);
            log.info("user saved: " + user);

        }
    }
}
