
insert into app_role (id, role_name, description) values (1, 'ROLE_USER', 'Standard User');

-- non-encrypted password: ott
insert into app_user (id, name, non_pii_id, login, password) values (1, 'Alice', 'e8b-8eb', 'alice', '$2a$04$w.J4P19z9zs2SUfGch6ePuhDxr/g2iHJFzbFUCsF9VhPRljNawGUW');
insert into app_user (id, name, non_pii_id, login, password) values (2, 'Bob', '2f8-4da', 'bob', '$2a$04$w.J4P19z9zs2SUfGch6ePuhDxr/g2iHJFzbFUCsF9VhPRljNawGUW');
insert into app_user (id, name, non_pii_id, login, password) values (3, 'Clara', '4da-e8b', 'clara', '$2a$04$w.J4P19z9zs2SUfGch6ePuhDxr/g2iHJFzbFUCsF9VhPRljNawGUW');

insert into user_role(user_id, role_id) values(1,1);
insert into user_role(user_id, role_id) values(2,1);
insert into user_role(user_id, role_id) values(3,1);
