package by.andd3dfx.auth.config;

import static java.time.temporal.ChronoUnit.MINUTES;

import by.andd3dfx.auth.domain.User;
import by.andd3dfx.auth.repository.UserRepository;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

@Component
public class CustomTokenEnhancer implements TokenEnhancer {

    @Autowired
    private UserRepository userRepository;

    @Value("${security.jwt.expiration-mins}")
    private int expirationMins;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        User user = userRepository.findByLogin(authentication.getName());
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("non_pii_id", user.getNonPiiId());
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        Date expirationDate = Date.from(Instant.now().plus(expirationMins, MINUTES));
        ((DefaultOAuth2AccessToken) accessToken).setExpiration(expirationDate);
        return accessToken;
    }
}
