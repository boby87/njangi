DO $$ 
BEGIN 
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'njangi') THEN 
    CREATE ROLE njangi WITH LOGIN PASSWORD 'njangi_secret' SUPERUSER; 
  END IF; 
END $$;

CREATE DATABASE njangi OWNER njangi;
