ALTER DATABASE sampledb DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

GRANT ALL ON sampledb.* TO 'dbuser'@'%' IDENTIFIED BY 'dbpassword' WITH GRANT OPTION;