-- Run once after the first start (Hibernate creates the tables, these tweak them)
USE inboxiq_db;

-- Allow long unsubscribe links
ALTER TABLE senders MODIFY unsubscribe_link TEXT;

-- One row per user + sender (prevents duplicates and "Query did not return a unique result")
ALTER TABLE senders ADD UNIQUE KEY uq_user_sender (user_id, sender_email);
