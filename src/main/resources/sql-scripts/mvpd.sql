
insert into app_role (id, role_name, description) values (1, 'ROLE_USER', 'Standard User');

-- non-encrypted password: mvpd
insert into app_user (id, name, non_pii_id, login, password) values (1, 'Alice', '8eb-e8b', 'alice', '$2a$04$e3mqQOFtvs.E6ZSOU4NnMu11hPwRNI/HJESM1IhUT0V1wA0LwK.4S');
insert into app_user (id, name, non_pii_id, login, password) values (2, 'Bob', '4da-2f8', 'bob', '$2a$04$e3mqQOFtvs.E6ZSOU4NnMu11hPwRNI/HJESM1IhUT0V1wA0LwK.4S');
insert into app_user (id, name, non_pii_id, login, password) values (3, 'Clara', 'e8b-4da', 'clara', '$2a$04$e3mqQOFtvs.E6ZSOU4NnMu11hPwRNI/HJESM1IhUT0V1wA0LwK.4S');

insert into user_role(user_id, role_id) values(1,1);
insert into user_role(user_id, role_id) values(2,1);
insert into user_role(user_id, role_id) values(3,1);
