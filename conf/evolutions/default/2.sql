# ENTRY SCHEAMA
 
# --- !Ups
CREATE TABLE entries (
  id SERIAL,
  headword varchar(128) NOT NULL,
  source varchar(256),
  context text,
  added timestamp NOT NULL,
  updated timestamp DEFAULT CURRENT_TIMESTAMP,
  tags varchar(128),
  rating integer DEFAULT 1,
  sound varchar(128),
  pronunciation varchar(128),
  word_class varchar(128),
  user_id integer NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT entries_users_fk FOREIGN KEY (user_id) REFERENCES users (id)
);

# --- !Downs 
DROP TABLE IF EXISTS entries;
