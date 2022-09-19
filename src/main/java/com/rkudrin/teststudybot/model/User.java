package com.rkudrin.teststudybot.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
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

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public int getCurrentStudyStage() {
        return currentStudyStage;
    }

    public void setCurrentStudyStage(int currentStudyStage) {
        this.currentStudyStage = currentStudyStage;
    }

    public int getTotalStudyStage() {
        return totalStudyStage;
    }

    public void setTotalStudyStage(int totalStudyStage) {
        this.totalStudyStage = totalStudyStage;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public boolean isStudyPaid() {
        return isStudyPaid;
    }

    public void setStudyPaid(boolean studyPaid) {
        isStudyPaid = studyPaid;
    }

    public List<PaymentsData> getStudyPaidDates() {
        return studyPaidDates;
    }

    public void setStudyPaidDates(List<PaymentsData> studyPaidDates) {
        this.studyPaidDates = studyPaidDates;
    }

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
