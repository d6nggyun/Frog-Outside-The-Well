package com._oormthon.seasonthon.global.config.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.repository.UserRepository;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        System.out.println("Kakao Attributes: " + attributes);
        Long kakaoId = (Long) attributes.get("id");

        userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .build();
            return userRepository.save(newUser);
        });

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "id");
    }
}
