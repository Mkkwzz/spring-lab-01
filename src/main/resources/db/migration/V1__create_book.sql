-- baseline for lab 10: the catalogue moves from memory into a table owned by Flyway
create table book (
    id             bigserial primary key,
    title          varchar(200) not null,
    author         varchar(120) not null,
    published_year integer      not null
);
