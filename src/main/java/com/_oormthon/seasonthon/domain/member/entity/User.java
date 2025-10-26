package com._oormthon.seasonthon.domain.member.entity;

import com._oormthon.seasonthon.domain.member.enums.School;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Column(unique = true)
    private Long kakaoId;

    @Column(name = "profile_image")
    private String profileImage;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private School school;

    private Integer grade;

    public void updateProfile(String email, String nickname, String profileImage) {
        this.email = Optional.ofNullable(email).orElse(this.email);
        this.nickname = Optional.ofNullable(nickname).orElse(this.nickname);
        this.profileImage = Optional.ofNullable(profileImage).orElse(this.profileImage);
    }
}