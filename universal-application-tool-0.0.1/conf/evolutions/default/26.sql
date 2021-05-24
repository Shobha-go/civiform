# --- Add a column to applications that records the email address of the submitter
# --- Also ensure that no global admins can be program admins.

# --- !Ups
alter table applications add column submitter_email varchar(255);

# --- !Downs
alter table applications drop column submitter_email;
