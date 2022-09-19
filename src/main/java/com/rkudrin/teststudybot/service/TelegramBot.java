package com.rkudrin.teststudybot.service;

import com.rkudrin.teststudybot.config.BotConfig;
import com.rkudrin.teststudybot.dict.ButtonDictionary;
import com.rkudrin.teststudybot.dict.MainDictionary;
import com.rkudrin.teststudybot.dict.RankDictionary;
import com.rkudrin.teststudybot.model.User;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
        listOfCommands.add(new BotCommand("/pay", "Оплатить обучение"));
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
                case ButtonDictionary.START_BUTTON:
                    registerUser(message);
                    startCommandReceived(chatId, message.getChat().getFirstName());
                    break;
                case ButtonDictionary.GET_DATA_BUTTON:
                    getUserData(chatId);
                    break;
                case ButtonDictionary.DELETE_DATA_BUTTON:
                    deleteUserData(chatId);
                    break;
                case ButtonDictionary.HELP_BUTTON:
                    messageExecute(getMessage(chatId, MainDictionary.HELP_TEXT));
                    break;
                case ButtonDictionary.SETTINGS_BUTTON:
                    settingCommandReceived(chatId);
                    break;
                case ButtonDictionary.STUDY_BUTTON:
                    studyCommandReceived(chatId);
                    break;
                case ButtonDictionary.PAY_BUTTON:
                    messageExecute(getMessage(chatId, "Оплата курса пока не предусмотрена."));
                    break;
                default:
                    //Тут считываются ответы на задачи, пока не понял, как это правильно реализовать.
                    if (checkIfNumOrNot(messageText)) {
                        checkAnswer(message);
                    } else {
                        messageExecute(getMessage(chatId, "Извини, я не знаю такую команду"));
                    }
            }
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callBackData) {
                case ButtonDictionary.DELETE_MY_DATA_AGREED_BUTTON:
                    try {
                        if (userService.checkUserExist(chatId)) {
                            userService.deleteUser(chatId);
                            messageExecute(getMessage(chatId, "Данные успешно удалены"));
                        } else {
                            messageExecute(getMessage(chatId, "Данные уже были удалены"));
                        }
                    } catch (Exception e) {
                        log.error("Что-то пошло не так при удалении данных: " + e.getMessage());
                        System.out.println(e.getMessage());
                    }
                    break;
                case ButtonDictionary.DELETE_MY_DATA_REFUSAL_BUTTON:
                    messageExecute(getMessage(chatId, "Запрос отклонён. Введите /help для получения списка возможных запросов"));
                    break;
                case ButtonDictionary.STUDY_BUTTON:
                    studyCommandReceived(chatId);
                    break;
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                    messageExecute(getMessage(chatId, "Вы перешли на " + callBackData + " этап обучения"));
                    userService.updateUserCurrentStage(chatId, callBackData);
                    break;
                default:
                    messageExecute(getMessage(chatId, "Что-то пошло не так. Введите /help для получения списка возможных запросов"));
            }

        }
    }

    private void checkAnswer(@NotNull Message message) {
        int answer = Integer.parseInt(message.getText());
        long chatId = message.getChatId();
        int stage = userService.findById(chatId).getCurrentStudyStage();

        SendMessage correctAnswerMessage = getMessage(chatId, MainDictionary.CORRECT_ANSWER);
        SendMessage incorrectAnswerMessage = getMessage(chatId, MainDictionary.INCORRECT_ANSWER);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Продолжить");
        button.setCallbackData(ButtonDictionary.STUDY_BUTTON);

        firstRow.add(button);
        rowsInLine.add(firstRow);
        keyboardMarkup.setKeyboard(rowsInLine);
        correctAnswerMessage.setReplyMarkup(keyboardMarkup);

        switch (stage) {
            case 1:
                if (answer == 4) {
                    userStageUp(chatId);
                    messageExecute(correctAnswerMessage);
                } else {
                    messageExecute(incorrectAnswerMessage);
                }
                break;
            case 2:
                if (answer == 6) {
                    userStageUp(chatId);
                    messageExecute(correctAnswerMessage);
                } else {
                    messageExecute(incorrectAnswerMessage);
                }
                break;
            case 3:
                if (answer == 0) {
                    userStageUp(chatId);
                    messageExecute(correctAnswerMessage);
                    userService.updateUserRank(RankDictionary.SECOND_RANK, chatId);
                } else {
                    messageExecute(incorrectAnswerMessage);
                }
                break;
            case 4:
                if (answer == 25) {
                    userStageUp(chatId);
                    messageExecute(correctAnswerMessage);
                } else {
                    messageExecute(incorrectAnswerMessage);
                }
                break;
            case 5:
                if (answer == 18) {
                    userStageUp(chatId);
                    messageExecute(correctAnswerMessage);
                } else {
                    messageExecute(incorrectAnswerMessage);
                }
                break;
            case 6:
                if (answer == 993) {
                    userStageUp(chatId);
                    messageExecute(getMessage(chatId, "Поздарвляю! Вы завершили обучение! Теперь вы готовы творить..."));
                    userService.updateUserRank(RankDictionary.THIRD_RANK, chatId);
                } else {
                    messageExecute(incorrectAnswerMessage);
                }
                break;
            default:
                messageExecute(getMessage(chatId, "Что-то пошло не так. Попробуй еще раз или свяжись с наставником"));
        }
    }

    private void userStageUp(long chatId) {
        try {
            User user = userService.findById(chatId);
            userService.upUserTotalStage(user);
        } catch (Exception e) {
            userNotFoundException(e, chatId);
        }
    }

    private boolean checkIfNumOrNot(String messageText) {
        try {
            int num = Integer.parseInt(messageText);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void studyCommandReceived(long chatId) {
        try {
            int stage = userService.findById(chatId).getCurrentStudyStage();

            generateNextTask(stage, chatId);
        } catch (Exception e) {
            userNotFoundException(e, chatId);
        }

    }

    private void generateNextTask(int stage, long chatId) {
        SendMessage message = getMessage(chatId, "");

        switch (stage) {
            case 1 -> {
                message.setText("Задание номер " + stage + ":\n\n" +
                        "Сколько будет 2 + 2 ?\n\n" +
                        MainDictionary.HOW_TO_ANSWER_TEXT);
                messageExecute(message);
            }
            case 2 -> {
                message.setText("Задание номер " + stage + ":\n\n" +
                        "Сколько будет 2 + 2 * 2 ?\n\n" +
                        MainDictionary.HOW_TO_ANSWER_TEXT);
                messageExecute(message);
            }
            case 3 -> {
                message.setText("Задание номер " + stage + ":\n\n" +
                        "Сколько будет 0 + 0 ?\n\n" +
                        MainDictionary.HOW_TO_ANSWER_TEXT);
                messageExecute(message);
            }
            case 4 -> {
                message.setText("Задание номер " + stage + ":\n\n" +
                        "Сколько будет 5 * 5 ?\n\n" +
                        MainDictionary.HOW_TO_ANSWER_TEXT);
                messageExecute(message);
            }
            case 5 -> {
                message.setText("Задание номер " + stage + ":\n\n" +
                        "Сколько будет 2 + 4 * 4 ?\n\n" +
                        MainDictionary.HOW_TO_ANSWER_TEXT);
                messageExecute(message);
            }
            case 6 -> {
                message.setText("Задание номер " + stage + ":\n\n" +
                        "Сколько будет 1000 - 7 ?\n\n" +
                        MainDictionary.HOW_TO_ANSWER_TEXT);
                messageExecute(message);
            }
            case 7 ->
                messageExecute(getMessage(chatId, "Вы уже завершили обучение. Вы можете вернуться к предыдущим этапам обучения с помощью команды /settings."));
            default -> {
                message.setText("Что-то пошло не так. Попробуйте позже и сообщите об этом ментору");
                messageExecute(message);
            }
        }
    }

    private void settingCommandReceived(long chatId) {
        try {
            int stage = userService.findById(chatId).getTotalStudyStage();
            SendMessage message = getMessage(chatId, MainDictionary.SETTINGS_TEXT);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
            List<InlineKeyboardButton> firstRow = new ArrayList<>();
            List<InlineKeyboardButton> secondRow = new ArrayList<>();

            for (int i = 1; i < stage + 1; i++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i));
                button.setCallbackData(String.valueOf(i));
                if (i % 2 == 0)
                    secondRow.add(button);
                else
                    firstRow.add(button);
            }
            rowsInLine.add(firstRow);
            rowsInLine.add(secondRow);
            keyboardMarkup.setKeyboard(rowsInLine);

            message.setReplyMarkup(keyboardMarkup);

            messageExecute(message);
        } catch (NullPointerException e) {
            userNotFoundException(e, chatId);
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
        } catch (NullPointerException e) {
            userNotFoundException(e, chatId);
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

    private void userNotFoundException(Exception e, long id) {
        log.error(MainDictionary.USER_NF_EXCEPTION + e.getMessage());
        messageExecute(getMessage(id, MainDictionary.USER_NOT_FOUND_TEXT));
    }
}
