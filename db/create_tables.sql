create database if not exists `addonis`;
use `addonis`;

create table binary_contents
(
    binary_content_id int auto_increment
        primary key,
    data              longblob not null,
    name              text     not null,
    constraint binary_contents_data_uk
        unique (data) using hash,
    constraint binary_contents_id_uk
        unique (binary_content_id)
);

create table categories
(
    category_id int          not null
        primary key,
    name        varchar(100) not null,
    constraint categories_name_uk
        unique (name)
);

create table invited_users
(
    id              int auto_increment
        primary key,
    email           varchar(70)                          not null,
    invitation_date datetime default current_timestamp() not null on update current_timestamp(),
    constraint invited_users_email_uk
        unique (email)
);

create table ratings
(
    rating_id int not null
        primary key
);

create table roles
(
    role_id int auto_increment
        primary key,
    name    varchar(50) not null,
    constraint roles_name_uk
        unique (name)
);

create table states
(
    state_id int         not null
        primary key,
    name     varchar(50) not null,
    constraint states_name_uk
        unique (name)
);

create table tags
(
    tag_id int auto_increment
        primary key,
    name   varchar(50) not null,
    constraint tags_name_uk
        unique (name),
    constraint tags_tag_id_uk
        unique (tag_id)
);

create table target_ides
(
    target_ide_id int         not null
        primary key,
    name          varchar(50) null,
    constraint target_ide_name_uk
        unique (name)
);

create table users
(
    user_id      int auto_increment
        primary key,
    username     varchar(50)                                            not null,
    password     varchar(255)                                           not null,
    email        varchar(80)                                            not null,
    phone_number varchar(25)                                            not null,
    photo_url    varchar(255) default '/assets/avatars/default/OIP.png' not null,
    is_blocked   tinyint(1)   default 0                                 not null,
    is_verified  tinyint(1)   default 0                                 not null,
    is_deleted   tinyint(1)   default 0                                 not null,
    constraint users_email_uk
        unique (email),
    constraint users_phone_number_uk
        unique (phone_number),
    constraint users_username_uk
        unique (username)
);

create table addons
(
    addon_id            int auto_increment
        primary key,
    name                varchar(50)                             null,
    target_ide_id       int                                     null,
    creator_id          int                                     not null,
    description         text                                    null,
    binary_content_id   int                                     null,
    origin_url          varchar(255)                            null,
    upload_date         timestamp default current_timestamp()   not null on update current_timestamp(),
    last_commit_date    datetime  default '2022-11-13 22:03:38' null,
    last_commit_message text                                    null,
    downloads           int       default 0                     not null,
    state_id            int                                     not null,
    open_issues_count   int                                     null,
    pull_requests_count int                                     null,
    is_featured         tinyint(1)                              not null,
    constraint addons_binary_contents_null_fk
        foreign key (binary_content_id) references binary_contents (binary_content_id),
    constraint addons_states_state_id_fk
        foreign key (state_id) references states (state_id),
    constraint addons_target_ides_target_ide_id_fk
        foreign key (target_ide_id) references target_ides (target_ide_id),
    constraint addons_users_creator_id_fk
        foreign key (creator_id) references users (user_id)
);

create table addons_categories
(
    addon_id    int not null,
    category_id int null,
    constraint addons_categories_addon_id_fk
        foreign key (addon_id) references addons (addon_id),
    constraint addons_categories_categeory_id_fk
        foreign key (category_id) references categories (category_id)
);

create table addons_ratings
(
    user_id   int not null,
    addon_id  int not null,
    rating_id int not null,
    constraint addons_ratings_addon_id_fk
        foreign key (addon_id) references addons (addon_id),
    constraint addons_ratings_ratings_null_fk
        foreign key (rating_id) references ratings (rating_id),
    constraint addons_ratings_user_id_fk
        foreign key (user_id) references users (user_id)
);

create table addons_tags
(
    addon_id int not null,
    tag_id   int not null,
    constraint addons_tags_addon_id_fk
        foreign key (addon_id) references addons (addon_id)
            on delete cascade,
    constraint addons_tags_tag_id_fk
        foreign key (tag_id) references tags (tag_id)
);

create table pasword_reset_tokens
(
    id              int auto_increment
        primary key,
    token           varchar(100) not null,
    expiration_date datetime     not null,
    user_id         int          not null,
    constraint verify_addon_tokens_addons_null_fk
        foreign key (user_id) references users (user_id)
);

create table users_roles
(
    user_id int not null,
    role_id int not null,
    constraint users_roles_role_id_fk
        foreign key (role_id) references roles (role_id),
    constraint users_roles_user_id_fk
        foreign key (user_id) references users (user_id)
);

create table verify_account_tokens
(
    id      int auto_increment
        primary key,
    token   text not null,
    user_id int  not null,
    constraint verify_account_token_token_uk
        unique (token) using hash,
    constraint verify_account_tokens_user_uk
        unique (user_id),
    constraint verify_account_tokens_user_fk
        foreign key (user_id) references users (user_id)
);

