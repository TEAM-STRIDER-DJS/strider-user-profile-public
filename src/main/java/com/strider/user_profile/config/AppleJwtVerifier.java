package com.strider.user_profile.config;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AppleJwtVerifier {
    @Value("${provider.apple.jwks-uri}")
    private String appleJwksUri;
    @Value("${provider.apple.issuer}")
    private String issuer;
    @Value("${provider.apple.client-id}")
    private String clientId; // audience


    private final RestTemplate restTemplate = new RestTemplate();
    private volatile JWKSet cachedJwkSet;
    private volatile long lastFetchTimeMs = 0L;


    public JWTClaimsSet verify(String identityToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(identityToken);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();


// iss, aud, exp 체크
            if (!issuer.equals(claims.getIssuer()))
                throw new IllegalArgumentException("Invalid issuer");
            if (!claims.getAudience().contains(clientId))
                throw new IllegalArgumentException("Invalid audience");
            if (new Date().after(claims.getExpirationTime()))
                throw new IllegalArgumentException("Expired token");


// 서명 검증
            JWKSet jwkSet = loadAppleKeys();
            JWK jwk = jwkSet.getKeyByKeyId(jwt.getHeader().getKeyID());
            if (jwk == null) throw new IllegalArgumentException("No matching Apple JWK");
            JWSVerifier verifier = new RSASSAVerifier(((RSAKey) jwk).toRSAPublicKey());
            if (!jwt.verify(verifier)) throw new IllegalArgumentException("Invalid signature");


            return claims;
        } catch (Exception e) {
            throw new RuntimeException("Apple token verify failed", e);
        }
    }


    private synchronized JWKSet loadAppleKeys() throws java.io.IOException, com.nimbusds.jose.JOSEException, ParseException {
        long now = System.currentTimeMillis();
        if (cachedJwkSet != null && (now - lastFetchTimeMs) < 60000) {
            return cachedJwkSet; // 60s 캐시
        }
        String json = restTemplate.getForObject(appleJwksUri, String.class);
        cachedJwkSet = JWKSet.parse(Objects.requireNonNull(json));
        lastFetchTimeMs = now;
        return cachedJwkSet;
    }
}
