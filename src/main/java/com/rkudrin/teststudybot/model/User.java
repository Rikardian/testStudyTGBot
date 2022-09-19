package com.rkudrin.teststudybot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    @Id
    private Long chatId;

    private String firstName;

    private String lastName;

    private String userName;

    private LocalDateTime registeredAt;

    private int currentStudyStage;

    private int totalStudyStage;

    private String rank;

    private boolean isStudyPaid;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PaymentsData> studyPaidDates;

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public String toString() {
        return "Ваше имя: " + firstName + " " + lastName + '\n' +
                "Ваш никнейм: " + userName + '\n' +
                "Вы зарегестрировались  " + registeredAt.format(formatter) + "\n" +
                "Ваш ранк: " + rank + "\n" +
                "Ваш текущий этап обучения: " + currentStudyStage + "\n" +
                "Ваши пройденные этапы обучения: " + totalStudyStage + "\n" +
                "Ваше обучение " + (isStudyPaid ? "Оплачено" : "Не оплачено");
    }
}
