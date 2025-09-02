package com._oormthon.seasonthon.global.config.jwt;

import com._oormthon.seasonthon.domain.member.entity.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String key;
    private static final Long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60L * 24 * 7;

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN_EXPIRE_TIME);
    }

    private String generateToken(User user, Long expireTime) {
        Claims claims = Jwts.claims();
        claims.put("member_id", user.getMemberId());
        claims.put("kakao_id", user.getKakaoId());
        claims.put("nickname", user.getNickname());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public Long getId(String token) {
        return ((Number) parseClaims(token).get("id")).longValue();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}