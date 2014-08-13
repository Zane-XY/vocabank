# ENTRY USER
 
# --- !Ups

CREATE TABLE users (
  id SERIAL,
  name varchar(128) NOT NULL,
  email varchar(128) NOT NULL,
  password varchar(64) NOT NULL,
  registerdate timestamp DEFAULT current_timestamp,
  PRIMARY KEY (ID)
);

# --- !Downs 

DROP TABLE IF EXISTS users;

