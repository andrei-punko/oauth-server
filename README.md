
# Spring Boot app for securing a REST API with JSON Web Token (JWT)
![Java CI with Gradle](https://github.com/andrei-punko/oauth-server/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/andrei-punko/oauth-server/actions/workflows/gradle.yml)
[![Branches](.github/badges/branches.svg)](https://github.com/andrei-punko/oauth-server/actions/workflows/gradle.yml)

## Prerequisites
- Maven 3
- JDK 17

Use https://jwt.io to decode your generated token  
Use https://base64encode.org to encode/decode to/from Base64

## How to build jar
    ./gradlew clean build

## How to build Docker image
    docker build ./ -t oauth-service-app

## How to run jars
```bash
java -jar ./build/libs/auth-server-0.1-SNAPSHOT.jar --auth.profile=mvpd
java -jar ./build/libs/auth-server-0.1-SNAPSHOT.jar --auth.profile=ott
```

## How to run in Docker
    docker-compose up

## How to run both services in Docker containers with rebuild of images
    docker-compose up --build --force-recreate --no-deps

## Configuration info
* Client: `entitlements`
* Secret:
  * `ott` (for `auth.profile=ott`)
  * `mvpd` (for `auth.profile=mvpd`)
* Username / password:
  * `alice/password`
  * `bob/password`
  * `clara/password`

See [this article](https://alexbilbie.com/guide-to-oauth-2-grants) for info about different types of OAuth 2.0 grants

## Authorization Grant cases

### 1. Authorisation Code Grant
Authorize using login form on authorization server, get authorization code, exchange it to access token.
Very familiar if you’ve ever signed into an application using your Facebook or Google account.

#### Flow, part one: authorize using login form on authorization server

Use link: http://localhost:9090/oauth/authorize?client_id=entitlements&state=abc-123&response_type=code&redirect_uri=http://localhost:9191/x&node-name=hulu

The `state` here is CSRF token. This parameter is optional but recommended. You should store the value of the CSRF token in the user’s session to be validated when they return.

The user will then be asked to login to the authorization server and approve the client.

Put `bob/ott` login/password pair into form and click `Approve`

(The login/password pairs populated into in-memory DB H2. For details check sql scripts in resources folder if needed)

If the user approves the client they will be redirected from the authorisation server back to the client (specifically to the redirect URI). 
After some delay (due to redirection to some fake non-existing resource localhost:9191) you get string like next in browser address field:
http://localhost:9191/x?code=qjGBjC&state=abc-123.  
Get code `qjGBjC` for future reuse in next actions.  
The returned state `abc-123` used to compare with the state parameter sent in the original request.

#### Generate access token using authorization code from previous step

Use the following generic command to generate an access token:

For this specific application, to generate an access token for the user bob, run next command:

```bash
curl http://localhost:9090/oauth/token \
  -H "Content-type: application/x-www-form-urlencoded" \
  -d 'grant_type=authorization_code&redirect_uri=http://localhost:9191/x&code=qjGBjC' \
  -u entitlements:ott
```
As you see - already generated for bob code `qjGBjC` was used

You'll receive responses similar to below
```json
{
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsibm9uX3BpaV9pZCJdLCJ1c2VyX25hbWUiOiJib2IiLCJzY29wZSI6WyJyZWFkIl0sImV4cCI6MTU4NTIyOTMyOCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjIxODEwZWUxLWI5MGQtNDQ4My1hYTE1LTJlNjZkOTc4MjhlZSIsImNsaWVudF9pZCI6ImVudGl0bGVtZW50cyIsIm5vbl9waWlfaWQiOiIyZjgtNGRhIn0.5Ef4wabYaGi5Ed56kD9bLip92BDx6-WeKNfmwh7P-wI",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsibm9uX3BpaV9pZCJdLCJ1c2VyX25hbWUiOiJib2IiLCJzY29wZSI6WyJyZWFkIl0sImF0aSI6IjIxODEwZWUxLWI5MGQtNDQ4My1hYTE1LTJlNjZkOTc4MjhlZSIsImV4cCI6MTU4NzczNDkyOCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6ImFmOTk4NzNlLTVkMzItNDk1MS04YWFmLWFjN2VjYmViZjE4YiIsImNsaWVudF9pZCI6ImVudGl0bGVtZW50cyIsIm5vbl9waWlfaWQiOiIyZjgtNGRhIn0.pwDQPzq_pMzOMb5f_0GcZWo_mcd2ncT4oI-qbneOI_Y",
    "expires_in": 86399,
    "scope": "read",
    "non_pii_id": "2f8-4da",
    "jti": "21810ee1-b90d-4483-aa15-2e66d97828ee"
}
```

### 2. Resource owner credentials grant
Get access token using login/password.
Used for trusted first party clients both on the web and in native device applications.

#### Generate access token using password
```bash
curl -X POST http://localhost:9090/oauth/token \
  -H 'Authorization: Basic ZW50aXRsZW1lbnRzOm90dA==' \
  -H 'Content-type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' -d 'username=bob' -d 'password=ott'
```
Here we use header for basic authorization client_name:client_secret encoded into Base64:
```
entitlements:ott -> ZW50aXRsZW1lbnRzOm90dA==
```

You'll receive response similar to below
```json
{
  "access_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic3Vic2NyaXB0aW9ucyIsInVzZXJzIl0sInVzZXJfbmFtZSI6ImJvYiIsInNjb3BlIjpbInJlYWQiXSwiZXhwIjoxNjUwNzA1MzY3LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiM2MwNjA2MjctM2FiNy00YTQyLTg4MmMtMjA4OWJkZjBlZTg1IiwiY2xpZW50X2lkIjoiZW50aXRsZW1lbnRzIiwibm9uX3BpaV9pZCI6IjJmOC00ZGEifQ.l_VgIDAXEuJu7Osv6mxmFv0J--MVJ7ZL8PwgnH-l2e0","token_type":"bearer","refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic3Vic2NyaXB0aW9ucyIsInVzZXJzIl0sInVzZXJfbmFtZSI6ImJvYiIsInNjb3BlIjpbInJlYWQiXSwiYXRpIjoiM2MwNjA2MjctM2FiNy00YTQyLTg4MmMtMjA4OWJkZjBlZTg1IiwiZXhwIjoxNjUzMjEwOTY3LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiNTI2YjBiY2YtYzc5OS00NzBlLThkNmYtNzY0ZGMzZDQ4NTI0IiwiY2xpZW50X2lkIjoiZW50aXRsZW1lbnRzIiwibm9uX3BpaV9pZCI6IjJmOC00ZGEifQ.NlH1NftfyuQLQ3sGwrzVL5wg9alvIS_2MOnSMpxrWZg",
  "expires_in":86399,
  "scope":"read",
  "non_pii_id":"2f8-4da",
  "jti":"3c060627-3ab7-4a42-882c-2089bdf0ee85"
}
```

### 3. Client credentials grant
The simplest of OAuth 2.0 grants, it is suitable for machine-to-machine authentication where a specific user’s permission to access data is not required.

#### Generate access token using password
```bash
curl -X POST http://localhost:9090/oauth/token \
  -H 'Authorization: Basic ZW50aXRsZW1lbnRzOm90dA==' \
  -H "Content-type: application/x-www-form-urlencoded" \
  -d 'grant_type=client_credentials'
```
Here we use header for basic authorization client_name:client_secret encoded into Base64:
```
entitlements:ott -> ZW50aXRsZW1lbnRzOm90dA==
```

You'll receive response similar to below
```json
{
  "access_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsic3Vic2NyaXB0aW9ucyIsInVzZXJzIl0sInNjb3BlIjpbInJlYWQiXSwiZXhwIjoxNjUwNzA1MTQ5LCJqdGkiOiJmNGY3MWJmNy04YmRlLTQ0YWYtOGI5Zi00NzY1YzY0OGFlNmYiLCJjbGllbnRfaWQiOiJlbnRpdGxlbWVudHMifQ.kJXmS4e0dNzzYnJayKwH3VMU1bYnAa2ENV1aNMnIzAQ",
  "token_type":"bearer",
  "expires_in":86399,
  "scope":"read",
  "jti":"f4f71bf7-8bde-44af-8b9f-4765c648ae6f"
}
```

## Check existing token

To check existing token use:
```bash
curl -X POST client:secret@localhost:9090/oauth/check_token?token=TOKEN_HERE
```

For our case it should be:
```bash
curl -X POST entitlements:ott@localhost:9090/oauth/check_token?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsibm9uX3BpaV9pZCJdLCJ1c2VyX25hbWUiOiJib2IiLCJzY29wZSI6WyJyZWFkIl0sImV4cCI6MTU4NTIyOTMyOCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjIxODEwZWUxLWI5MGQtNDQ4My1hYTE1LTJlNjZkOTc4MjhlZSIsImNsaWVudF9pZCI6ImVudGl0bGVtZW50cyIsIm5vbl9waWlfaWQiOiIyZjgtNGRhIn0.5Ef4wabYaGi5Ed56kD9bLip92BDx6-WeKNfmwh7P-wI
```

You'll receive a response similar to below
```json
{
    "aud": ["non_pii_id"],
    "user_name": "bob",
    "scope": ["read"],
    "active": true,
    "exp": 1585229328,
    "authorities": ["ROLE_USER"],
    "jti": "21810ee1-b90d-4483-aa15-2e66d97828ee",
    "client_id": "entitlements",
    "non_pii_id": "2f8-4da"
}
```
or next
```json
{
    "error": "invalid_token",
    "error_description": "Encoded token is a refresh token"
}
```

## Get new token pair using refresh token

To get new token pair using existing refresh token use:
```bash
curl -X POST -d refresh_token=REFRESH_TOKEN_HERE -d grant_type=refresh_token client:secret@localhost:9090/oauth/token
```

For our case it should be:
```bash
curl -X POST -d refresh_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsibm9uX3BpaV9pZCJdLCJ1c2VyX25hbWUiOiJib2IiLCJzY29wZSI6WyJyZWFkIl0sImF0aSI6IjIxODEwZWUxLWI5MGQtNDQ4My1hYTE1LTJlNjZkOTc4MjhlZSIsImV4cCI6MTU4NzczNDkyOCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6ImFmOTk4NzNlLTVkMzItNDk1MS04YWFmLWFjN2VjYmViZjE4YiIsImNsaWVudF9pZCI6ImVudGl0bGVtZW50cyIsIm5vbl9waWlfaWQiOiIyZjgtNGRhIn0.pwDQPzq_pMzOMb5f_0GcZWo_mcd2ncT4oI-qbneOI_Y -d grant_type=refresh_token entitlements:ott@localhost:9090/oauth/token
```

You'll receive a response similar to below
```json
{
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsibm9uX3BpaV9pZCJdLCJ1c2VyX25hbWUiOiJib2IiLCJzY29wZSI6WyJyZWFkIl0sImV4cCI6MTU4NTIyOTY5MywiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjRkMmNmNWIwLWNkZTYtNDgyNS05NWExLTc1MTVkNWM5OTM3OCIsImNsaWVudF9pZCI6ImVudGl0bGVtZW50cyIsIm5vbl9waWlfaWQiOiIyZjgtNGRhIn0.XqP3T4hrROecgNqHIAgL1sBUhraN-I6pM3q0rbJjSus",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsibm9uX3BpaV9pZCJdLCJ1c2VyX25hbWUiOiJib2IiLCJzY29wZSI6WyJyZWFkIl0sImF0aSI6IjRkMmNmNWIwLWNkZTYtNDgyNS05NWExLTc1MTVkNWM5OTM3OCIsImV4cCI6MTU4NzczNDkyOCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6ImFmOTk4NzNlLTVkMzItNDk1MS04YWFmLWFjN2VjYmViZjE4YiIsImNsaWVudF9pZCI6ImVudGl0bGVtZW50cyIsIm5vbl9waWlfaWQiOiIyZjgtNGRhIn0.XNjkVBwrldmX16ziIgdIZNGGcdrLPoLxkf4YWXxX1xU",
    "expires_in": 86399,
    "scope": "read",
    "non_pii_id": "2f8-4da",
    "jti": "4d2cf5b0-cde6-4825-95a1-7515d5c99378"
}
```

## Use tokens to access resources through your REST-ful API

Use the generated token as the value of the Bearer in the Authorization header as follows:
```bash
curl http://localhost:9091/YOUR_RESOURCE_PATH -H "Authorization: Bearer ACCESS_TOKEN_HERE"
```
