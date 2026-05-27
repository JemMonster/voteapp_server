-- V3__add_indexes.sql

create index if not exists idx_votings_creator_id on votings (creator_id);
create index if not exists idx_votings_status on votings (status);
create index if not exists idx_votings_type on votings (type);
create index if not exists idx_votings_ends_at on votings (ends_at);

create index if not exists idx_voting_options_voting_id on voting_options (voting_id);

create index if not exists idx_votes_user_id on votes (user_id);
create index if not exists idx_votes_voting_id on votes (voting_id);

