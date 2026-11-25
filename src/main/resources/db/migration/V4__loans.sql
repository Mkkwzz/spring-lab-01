-- lab 12 (variant 2): loans of catalogue books and the queue of readers waiting for a book
create table loan (
    id         bigserial primary key,
    book_id    bigint      not null references book (id),
    reader     varchar(60) not null,
    issued_on  date        not null,
    due_on     date        not null,
    extensions integer     not null default 0,
    returned   boolean     not null default false
);

create table hold (
    id        bigserial primary key,
    book_id   bigint      not null references book (id),
    reader    varchar(60) not null,
    placed_on date        not null
);
