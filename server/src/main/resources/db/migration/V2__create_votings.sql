-- V2__create_votings.sql

-- Enums (stored as text)
create type if not exists voting_type as enum (
    'SINGLE',
    'MULTIPLE',
    'PETITION',
    'RAFFLE'
);

create type if not exists voting_status as enum (
    'ACTIVE',
    'CLOSED'
);

create table if not exists votings (
    id uuid primary key,
    title varchar(255) not null,
    description text not null,
    type voting_type not null,
    status voting_status not null,
    image_url varchar(500),
    creator_id uuid not null references users(id),
    ends_at timestamp not null
);

create table if not exists voting_options (
    id uuid primary key,
    voting_id uuid not null references votings(id),
    text text not null,
    votes integer not null default 0
);

create table if not exists votes (
    id uuid primary key,
    user_id uuid not null references users(id),
    voting_id uuid not null references votings(id),
    option_ids text not null
);

