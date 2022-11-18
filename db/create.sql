create table categories
(
    category_id int          not null
        primary key,
    name        varchar(100) not null,
    constraint categories_name_uk
        unique (name)
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
    tag_id int         not null
        primary key,
    name   varchar(50) not null,
    constraint tags_tag_name_uk
        unique (name)
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
    username     varchar(50)          not null,
    password     varchar(50)          not null,
    email        varchar(80)          not null,
    phone_number varchar(25)          not null,
    photo_url    varchar(255)         not null,
    is_blocked   tinyint(1)           not null,
    is_verified  tinyint(1) default 0 not null,
    is_deleted   tinyint(1) default 0 not null,
    constraint users_email_uk
        unique (email),
    constraint users_phone_number_uk
        unique (phone_number),
    constraint users_username_uk
        unique (username)
);

create table addons
(
    addon_id       int auto_increment
        primary key,
    name           varchar(50)                           not null,
    target_ide_id  int                                   not null,
    creator_id     int                                   not null,
    description    text                                  not null,
    binary_content longblob                              not null,
    origin_url     varchar(255)                          not null,
    upload_date    timestamp default current_timestamp() not null on update current_timestamp(),
    downloads      int       default 0                   not null,
    state_id       int                                   not null,
    constraint addon_name_uk
        unique (name),
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
    user_id  int        not null,
    addon_id int        not null,
    rating   tinyint(6) not null,
    constraint addons_ratings_addon_id_fk
        foreign key (addon_id) references addons (addon_id),
    constraint addons_ratings_user_id_fk
        foreign key (user_id) references users (user_id)
);

create table addons_tags
(
    addon_id int not null,
    tag_id   int not null,
    constraint addons_tags_addon_id_fk
        foreign key (addon_id) references addons (addon_id),
    constraint addons_tags_tag_id_fk
        foreign key (tag_id) references tags (tag_id)
);

create table drafts
(
    draft_id       int auto_increment
        primary key,
    name           varchar(50)  null,
    target_ide_id  int          null,
    creator_id     int          null,
    description    text         null,
    binary_context longblob     null,
    origin_url     varchar(255) null,
    constraint drafts_target_ides_target_ide_id_fk
        foreign key (target_ide_id) references target_ides (target_ide_id),
    constraint drafts_users_creator_id_fk
        foreign key (creator_id) references users (user_id)
);

create table drafts_categories
(
    draft_id    int not null,
    category_id int not null,
    constraint drafts_categories_category_id_fk
        foreign key (category_id) references categories (category_id),
    constraint drafts_categories_draft_id_fk
        foreign key (draft_id) references drafts (draft_id)
);

create table drafts_tags
(
    draft_id int null,
    tag_id   int not null,
    constraint drafts_tags_draft_id_fk
        foreign key (draft_id) references drafts (draft_id),
    constraint drafts_tags_tag_id_fk
        foreign key (tag_id) references tags (tag_id)
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

