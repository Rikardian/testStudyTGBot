package com.rkudrin.teststudybot.service;

import com.rkudrin.teststudybot.config.BotConfig;
import com.rkudrin.teststudybot.dict.ButtonDictionary;
import com.rkudrin.teststudybot.model.User;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final UserService userService;

    @Autowired
    public TelegramBot(BotConfig config, UserService userService) {
        this.config = config;
        this.userService = userService;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать работу"));
        listOfCommands.add(new BotCommand("/study", "Перейти к обучению"));
        listOfCommands.add(new BotCommand("/getmydata", "Получить ваши данные"));
        listOfCommands.add(new BotCommand("/deletemydata", "Удалить ваши данные"));
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
                case "/getmydata":
                    getUserData(chatId);
                    break;
                case "/deletemydata":
                    deleteUserData(chatId);
                    break;
                default:
                    messageExecute(getMessage(chatId, "Извини, я не знаю такую команду"));
            }
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(ButtonDictionary.DELETE_MY_DATA_AGREED_BUTTON)){
                try {
                    if (userService.findById(chatId).getChatId() != null){
                        userService.deleteUser(chatId);
                        messageExecute(getMessage(chatId, "Данные успешно удалены"));
                    }
                    else {
                        messageExecute(getMessage(chatId, "Данные уже были удалены"));
                    }
                } catch (Exception e){
                    log.error("Что-то пошло не так при удалении данных: " + e.getMessage());
                    System.out.println(e.getMessage());
                }
            }
            else if (callBackData.equals(ButtonDictionary.DELETE_MY_DATA_REFUSAL_BUTTON)){
                messageExecute(getMessage(chatId, "Запрос отклонён. Введите /help для получения списка возможных запросов"));
            }
            else {
                messageExecute(getMessage(chatId, "Что-то пошло не так. Введите /help для получения списка возможных запросов"));
            }

        }
    }

    private void deleteUserData(long chatId) {
        String answer = "Вы уверены, что хотите удалить все данные о себе? Вы не сможете вернуть эти изменения.";
        SendMessage message = getMessage(chatId, answer);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineAgreed = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineRefuse = new ArrayList<>();

        InlineKeyboardButton agreedButton = new InlineKeyboardButton();
        agreedButton.setText("Да, я согласен удалить данные без возможности возврата");
        agreedButton.setCallbackData(ButtonDictionary.DELETE_MY_DATA_AGREED_BUTTON);
        rowInLineAgreed.add(agreedButton);

        InlineKeyboardButton refuseButton = new InlineKeyboardButton();
        refuseButton.setText("Нет, отменить операцию");
        refuseButton.setCallbackData(ButtonDictionary.DELETE_MY_DATA_REFUSAL_BUTTON);
        rowInLineRefuse.add(refuseButton);

        rowsInLine.add(rowInLineAgreed);
        rowsInLine.add(rowInLineRefuse);
        keyboardMarkup.setKeyboard(rowsInLine);

        message.setReplyMarkup(keyboardMarkup);
        messageExecute(message);

    }

    private void getUserData(long chatId) {
        User user = userService.findById(chatId);

        try {
            messageExecute(getMessage(chatId, user.toString()));
        } catch (NullPointerException e){
            messageExecute(getMessage(chatId, "Данные не найдены, пройдите регистрацию"));
            log.error("Ошибка при получении данных пользователя + " + e.getMessage());
        } catch (Exception e){
            log.error("Ошибка при получении данных пользователя + " + e.getMessage());
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
        } catch (TelegramApiException e) {
            log.error("Произошла ошибка при отправке сообщения: " + e.getMessage());
        }
    }

    //Регистрация нового пользователя
    private void registerUser(Message message) {
        if (userService.findById(message.getChatId()) == null) {
            userService.createUserById(message);
        }
    }
}
