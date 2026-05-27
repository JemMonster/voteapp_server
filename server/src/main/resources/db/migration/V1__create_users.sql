-- V1__create_users.sql

create table if not exists users (
    id uuid primary key,
    email varchar(255) not null unique,
    name varchar(100)
);

