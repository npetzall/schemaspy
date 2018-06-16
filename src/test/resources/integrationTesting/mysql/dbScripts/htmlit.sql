CREATE DATABASE `htmlit`;

CREATE TABLE `htmlit`.`group` (
  groupId INTEGER AUTO_INCREMENT,
  name VARCHAR(16) NOT NULL,
  description VARCHAR(80) NOT NULL,
  PRIMARY KEY (groupId),
  UNIQUE name_unique (name)
) engine=InnoDB COMMENT 'Groups';
CREATE TABLE `htmlit`.`user` (
  userId INTEGER AUTO_INCREMENT,
  groupId INTEGER NOT NULL COMMENT 'FK to groups omitted, implied relationship',
  name VARCHAR(16) NOT NULL COMMENT 'UserName',
  email VARCHAR(40) NOT NULL,
  PRIMARY KEY (userId),
  UNIQUE email_unique (email)
) engine=InnoDB COMMENT 'Users of the system';

CREATE TABLE `htmlit`.resources (
  resourceId INTEGER NOT NULL,
  name VARCHAR(40) NOT NULL,
  description VARCHAR(80) NOT NULL,
  PRIMARY KEY (resourceId),
  UNIQUE name_unique(name)
) engine=InnoDB;

CREATE TABLE `htmlit`.`resources/for/groups` (
  `r/f/gid` INTEGER AUTO_INCREMENT,
  groupId INTEGER NOT NULL,
  resourceId INTEGER NOT NULL,
  access ENUM('n','r','rw') DEFAULT 'n',
  PRIMARY KEY(`r/f/gid`),
  FOREIGN KEY groups_gid_fk (groupId) REFERENCES `htmlit`.`group`(groupId),
  FOREIGN KEY resource_rid_fk(resourceId) REFERENCES `htmlit`.resources(resourceId)
) engine=InnoDB;

CREATE TABLE `htmlit`.`or/phan` (
  `or/phanid` INTEGER AUTO_INCREMENT,
  PRIMARY KEY (`or/phanid`)
) engine=InnoDB;

CREATE VIEW `htmlit`.userAndGroup AS SELECT u.name AS UserName, g.name AS GroupName FROM `htmlit`.`user` u LEFT JOIN `htmlit`.`group` g ON u.groupId = g.groupId;
CREATE VIEW `htmlit`.`r/f/g/names` AS SELECT g.name AS GroupName, r.name AS ResourceName FROM `htmlit`.`resources/for/groups` rfg LEFT JOIN `htmlit`.resources r on rfg.resourceId = r.resourceId LEFT JOIN `htmlit`.`group` g ON rfg.groupId = g.groupId;

CREATE FUNCTION `htmlit`.no_det (s CHAR(20))
RETURNS CHAR(50) NOT DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

CREATE FUNCTION `htmlit`.yes_det (s CHAR(20))
RETURNS CHAR(50) DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

CREATE DEFINER = 'test'@'%' FUNCTION `htmlit`.`say/hello/to` (`name` CHAR(50))
RETURNS CHAR(60) DETERMINISTIC
RETURN CONCAT('Hello, ', `name`, '!');

GRANT SELECT, EXECUTE, SHOW VIEW on `htmlit`.* to test@`%`;
FLUSH PRIVILEGES;