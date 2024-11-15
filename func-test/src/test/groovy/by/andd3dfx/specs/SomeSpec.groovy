package by.andd3dfx.specs

import by.andd3dfx.model.Role
import by.andd3dfx.model.User
import spock.lang.Specification

import static by.andd3dfx.configs.Configuration.mvpdAlice
import static by.andd3dfx.configs.Configuration.mvpdBob
import static by.andd3dfx.configs.Configuration.mvpdClara
import static by.andd3dfx.configs.Configuration.mvpdRole
import static by.andd3dfx.configs.Configuration.ottAlice
import static by.andd3dfx.configs.Configuration.ottBob
import static by.andd3dfx.configs.Configuration.ottClara
import static by.andd3dfx.configs.Configuration.ottRole

class SomeSpec extends Specification {

    def 'Generate oauth token & check it'() {
        when: 'generate token'
        def generateTokenResponse = generateToken(role, user)
        String accessToken = generateTokenResponse.responseData.access_token
        and: 'check token'
        def checkTokenResponse = checkToken(role, accessToken)

        then: 'generate token response status is 200'
        assert generateTokenResponse.status == 200
        and: 'token_type is bearer'
        assert generateTokenResponse.responseData.token_type == 'bearer'
        and: 'token scope is read'
        assert generateTokenResponse.responseData.scope == 'read'
        and: 'access_token present'
        assert generateTokenResponse.responseData.access_token != null
        and: 'refresh_token present'
        assert generateTokenResponse.responseData.refresh_token != null

        and: 'check token status is 200'
        assert checkTokenResponse.status == 200
        and: 'aud present'
        assert checkTokenResponse.responseData.aud == ['subscriptions', 'users']
        and: 'user_name present'
        assert checkTokenResponse.responseData.user_name == user.name
        and: 'scope present'
        assert checkTokenResponse.responseData.scope == ['read']
        and: 'authorities present'
        assert checkTokenResponse.responseData.authorities == ["ROLE_USER"]
        and: 'token is active'
        assert checkTokenResponse.responseData.active == true

        where:
        role     | user
        ottRole  | ottAlice
        ottRole  | ottBob
        ottRole  | ottClara
        mvpdRole | mvpdAlice
        mvpdRole | mvpdBob
        mvpdRole | mvpdClara
    }

    def 'Generate oauth token & refresh it'() {
        when: 'generate token'
        def generateTokenResponse = generateToken(role, user)
        String access_token = generateTokenResponse.responseData.access_token
        String refresh_token = generateTokenResponse.responseData.refresh_token
        and: 'refresh oauth token'
        def refreshTokenResponse = refreshToken(role, refresh_token)

        then: 'server returns 200 code (ok)'
        assert refreshTokenResponse.status == 200
        and: 'access_token present'
        assert refreshTokenResponse.responseData.access_token != null
        and: 'access_token changed'
        assert refreshTokenResponse.responseData.access_token != access_token
        and: 'refresh_token present'
        assert refreshTokenResponse.responseData.refresh_token != null
        and: 'refresh_token changed'
        assert refreshTokenResponse.responseData.refresh_token != refresh_token
        and: 'token_type is bearer'
        assert refreshTokenResponse.responseData.token_type == 'bearer'

        where:
        role     | user
        ottRole  | ottAlice
        ottRole  | ottBob
        ottRole  | ottClara
        mvpdRole | mvpdAlice
        mvpdRole | mvpdBob
        mvpdRole | mvpdClara
    }

    Object generateToken(Role role, User user) {
        role.restClient.auth.basic(role.basicAuthClient, role.basicAuthSecret)
        return role.restClient.post(
                path: '/oauth/token',
                body: [
                        grant_type: 'password',
                        username  : user.name,
                        password  : user.password
                ],
                requestContentType: 'application/x-www-form-urlencoded'
        )
    }

    Object checkToken(Role role, String accessToken) {
        role.restClient.auth.basic(role.basicAuthClient, role.basicAuthSecret)
        return role.restClient.post(
                path: '/oauth/check_token',
                query: [
                        token: accessToken,
                ],
        )
    }

    Object refreshToken(Role role, String refreshToken) {
        role.restClient.auth.basic(role.basicAuthClient, role.basicAuthSecret)
        return role.restClient.post(
                path: '/oauth/token',
                body: [
                        grant_type   : 'refresh_token',
                        refresh_token: refreshToken,
                ],
                requestContentType: 'application/x-www-form-urlencoded'
        )
    }
}
