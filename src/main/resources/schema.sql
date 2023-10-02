--drop TABLE IF EXISTS users;
--drop TABLE IF EXISTS items;
--drop TABLE IF EXISTS bookings;
--drop TABLE IF EXISTS comments;
--drop TABLE IF EXISTS item_requests;

create table if not exists users (
  id int GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT uq_user_email UNIQUE (email)
);

create table if not exists items (
  id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  available BOOLEAN NOT NULL,
  user_id BIGINT NOT NULL,
  request_id BIGINT NULL,
  CONSTRAINT pk_item PRIMARY KEY (id)
);
CREATE INDEX if not exists idx_request_id ON items (request_id);

create table if not exists bookings (
  id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  status VARCHAR(50) NOT NULL,
  start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  item_id BIGINT NOT NULL,
  booker_id BIGINT NOT NULL,
  CONSTRAINT pk_booking PRIMARY KEY (id)
);

create table if not exists comments (
  id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  text VARCHAR(5000) NOT NULL,
  item_id BIGINT NOT NULL,
  user_id INT NOT NULL,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT pk_comment PRIMARY KEY (id)
);

create table if not exists item_requests (
  id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  description VARCHAR(5000) NOT NULL,
  user_id INT NOT NULL,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT pk_item_request PRIMARY KEY (id)
);