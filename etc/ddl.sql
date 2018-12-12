CREATE TABLE users(
    id uuid PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE posts(
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    text TEXT NOT NULL,
    created_on TIMESTAMP NOT NULL,

    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE comments(
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    post_id uuid NOT NULL,
    text VARCHAR(256) NOT NULL,
    created_on TIMESTAMP NOT NULL,

    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_post_id FOREIGN KEY (post_id) REFERENCES posts(id)
);

