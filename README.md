
# Spring Boot app for securing a REST API with JSON Web Token (JWT)

## Main building blocks
 * Spring Boot
 * JSON Web Token (go to https://jwt.io/ to decode your generated token and learn more)
 * H2 Database Engine - used for rapid prototyping and development, but not suitable for production at least in most cases. Go to http://h2database.com to learn more

## How to build:
    ./gradlew build

## How to build Docker image with application inside:
    docker build ./ -t oauth-service-app

## To run the application

```bash
java -jar ./build/libs/auth-server-0.1-SNAPSHOT.jar --auth.profile=mvpd
java -jar ./build/libs/auth-server-0.1-SNAPSHOT.jar --auth.profile=ott
```

Or import the project into your IDE and run `AuthServerApplication` from there.
Application starting with `auth.profile=ott` by default

Or use docker compose:  
`docker-compose up`

## To test the application

### Firstly, you will need the following basic pieces of information:

 * Client: `entitlements`
 * Secret: `ott` (or `mvpd` - value of `auth.profile` param used here)
 * Username and password: `alice/bob/clara` and `password`

### Authorize using OTT login form

Use link: http://localhost:9090/oauth/authorize?client_id=entitlements&response_type=code&redirect_uri=http://localhost:9191/x&node-name=hulu

Put `bob/ott` login/password pair into form and click `Approve`

(The login/password pairs populated into in-memory DB H2. For details check sql scripts in resources folder if needed)

After some delay (due to redirection to some fake unexisting resource localhost:9191) you get string like next in browser address field:
http://localhost:9191/x?code=qjGBjC
Get code `qjGBjC` for future reuse in next actions

### Generate an access token

Use the following generic command to generate an access token:

For this specific application, to generate an access token for the user bob, run next command:

```bash
curl http://localhost:9090/oauth/token -H "Content-type: application/x-www-form-urlencoded" -d 'grant_type=authorization_code&redirect_uri=http://localhost:9191/x&code=qjGBjC' -u entitlements:ott
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

### Check existing token

To check existing token use:
```bash
curl -X POST client:secret@localhost:9090/oauth/check_token?token=TOKEN_HERE
```

For our case this is next command:
```bash
curl -X POST entitlements:ott@localhost:9090/oauth/check_token?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsibm9uX3BpaV9pZCJdLCJ1c2VyX25hbWUiOiJib2IiLC
JzY29wZSI6WyJyZWFkIl0sImV4cCI6MTU4NTIyOTMyOCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjIxODEwZWUxLWI5MGQtNDQ4My1hYTE1LTJlNjZkOTc4MjhlZSIsImNsaWVudF9pZCI6ImVu
dGl0bGVtZW50cyIsIm5vbl9waWlfaWQiOiIyZjgtNGRhIn0.5Ef4wabYaGi5Ed56kD9bLip92BDx6-WeKNfmwh7P-wI
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

### Get new token pair using refresh token

To get new token pair using existing refresh token use:
```bash
curl -X POST -d refresh_token=REFRESH_TOKEN_HERE -d grant_type=refresh_token client:secret@localhost:9090/oauth/token
```

For our case it should be :
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

### Use tokens to access resources through your RESTful API

Use the generated tokens as the value of the Bearer in the Authorization header as follows:

```bash
curl http://localhost:9091/YOUR_RESOURCE_PATH -H "Authorization: Bearer ACCESS_TOKEN_HERE"
```
