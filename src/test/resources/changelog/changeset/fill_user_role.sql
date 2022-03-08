insert into roles (name, code)
values ('a', 'ROLE_A'),
       ('b', 'ROLE_B'),
       ('c', 'ROLE_C'),
       ('d', 'ROLE_D'),
       ('e', 'ROLE_E'),
       ('f', 'ROLE_F'),
       ('g', 'ROLE_G');

insert into users (role_id, login, location_id)
values (1,'admin', 1),
       (2, 'user_HR', 1),
       (3, 'user', 1),
       (4, 'user1', 1),
       (5, 'user2', 1),
       (6,'user3', 1),
       (7,'user4', 1),
       (8,'user5', 1),
       (9, 'user6', 1);
