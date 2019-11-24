# -- Table definitions

# --- !Ups
CREATE TABLE enquete (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL,
    gender varchar(6) NOT NULL,
    message varchar(255) NOT NULL,
    created_at timestamp default CURRENT_TIMESTAMP() NOT NULL
);

CREATE TABLE task (
    id int PRIMARY KEY AUTO_INCREMENT,
    title varchar(32) NOT NULL,
    description varchar(255),
    is_done boolean default FALSE,
    created_at timestamp default CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE user (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL,
    password varchar(255)NOT NULL,
    created_at timestamp default CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE id (
    userID int NOT NULL,
    taskID int NOT NULL
);

# --- !Downs
DROP TABLE enquete;

DROP TABLE task;

DROP TABLE user;

DROP TABLE id;