ALTER TABLE TASK DROP COLUMN TYPE;
ALTER TABLE TASK ADD (TYPE NVARCHAR2(255) NOT NULL);