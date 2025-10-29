package com._oormthon.seasonthon.global.config.oauth;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

        private final UserRepository userRepository;

        @Override
        @Transactional
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                OAuth2User oAuth2User = super.loadUser(userRequest);

                Map<String, Object> attributes = oAuth2User.getAttributes();
                Long kakaoId = Optional.ofNullable(attributes.get("id"))
                                .filter(Number.class::isInstance)
                                .map(Number.class::cast)
                                .map(Number::longValue)
                                .orElseThrow(() -> new OAuth2AuthenticationException(
                                        new OAuth2Error("invalid_user_info"), "Kakao 'id' is missing"
                                ));

                Map<String, Object> kakaoAccount = castMap(attributes.get("kakao_account"));
                Map<String, Object> kakaoProfile = kakaoAccount != null ? castMap(kakaoAccount.get("profile")) : null;

                String email = kakaoAccount != null
                        ? (String) kakaoAccount.get("email") : null;
                String nickname = kakaoProfile != null ? (String) kakaoProfile.get("nickname") : null;

                String profileImage = Optional.ofNullable(kakaoProfile)
                        .map(p -> (String) Optional.ofNullable(p.get("profile_image_url"))
                                .orElse("https://t1.kakaocdn.net/account_images/default_profile.jpeg"))
                        .orElse("https://t1.kakaocdn.net/account_images/default_profile.jpeg");

                userRepository.findByKakaoId(kakaoId).ifPresentOrElse(
                        user -> {
                                updateUserIfChanged(user, email, nickname, profileImage);
                        },
                        () -> {
                        User newUser = User.builder()
                                        .kakaoId(kakaoId)
                                        .email(email)
                                        .nickname(nickname)
                                        .profileImage(profileImage)
                                        .build();
                        userRepository.save(newUser);
                });

                String nameAttributeKey = Optional.ofNullable(
                        userRequest.getClientRegistration()
                                .getProviderDetails()
                                .getUserInfoEndpoint()
                                .getUserNameAttributeName()
                ).filter(key -> !key.isBlank()).orElse("id");

                return new DefaultOAuth2User(
                                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                                attributes,
                                nameAttributeKey
                );
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> castMap(Object object) {
                return object instanceof Map ? (Map<String, Object>) object : null;
        }

        private void updateUserIfChanged(User user, String email, String nickname, String profileImage) {
                String newEmail = Objects.equals(email, user.getEmail()) || !hasText(email) ? user.getEmail() : email;
                String newNickname = Objects.equals(nickname, user.getNickname()) || !hasText(nickname) ? user.getNickname() : nickname;
                String newProfileImage = Objects.equals(profileImage, user.getProfileImage()) || !hasText(profileImage) ? user.getProfileImage() : profileImage;

                if (!Objects.equals(newEmail, user.getEmail())
                        || !Objects.equals(newNickname, user.getNickname())
                        || !Objects.equals(newProfileImage, user.getProfileImage())) {
                        user.updateProfile(newEmail, newNickname, newProfileImage);
                }
        }
}
