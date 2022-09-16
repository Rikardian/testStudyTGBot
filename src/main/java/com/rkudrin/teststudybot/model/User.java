package com.rkudrin.teststudybot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", registeredAt=" + registeredAt +
                ", currentStudyStage=" + currentStudyStage +
                ", totalStudyStage=" + totalStudyStage +
                ", rank='" + rank + '\'' +
                ", isStudyPaid=" + isStudyPaid +
                '}';
    }
}
