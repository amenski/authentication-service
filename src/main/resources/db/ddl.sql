
DROP TABLE IF EXISTS auth_role;
CREATE TABLE `auth_role` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `authority` varchar(4000) NOT null,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS auth_account;
CREATE TABLE `auth_account` (
  `id` int(11) NOT null AUTO_INCREMENT, 
  `email` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL DEFAULT '',
  `avatar` varchar(255) DEFAULT NULL,
  `account_type` enum('individual','organization') NOT NULL,
  `enabled` bool NOT NULL DEFAULT false,
  `created_at` datetime, -- if timestamp, it will take LocalDate instead of UTC
  `modified_by` varchar(50),
  `modified_at` datetime,
  `last_access` datetime,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 default CHARSET=utf8;

DROP TABLE IF EXISTS auth_user;
CREATE TABLE `auth_user` (
  `account_id` int(11) NOT NULL, -- primary key and foreign key at the same time
  `fname` varchar(45) NOT NULL DEFAULT '',
  `lname` varchar(45) NOT NULL DEFAULT '',
  `dob` date DEFAULT NULL,
  `gender` enum('M','F') DEFAULT NULL,
  `phone` varchar(45) DEFAULT NULL,
  `created_at` datetime,
  `modified_by` varchar(50),
  `modified_at` datetime,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS auth_account_roles;
CREATE TABLE `auth_account_roles` (
  `account_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS auth_endpoint;
CREATE TABLE `auth_endpoint` (
  `service_name` varchar(50) NOT NULL,
  `endpoint`     varchar(50) NOT NULL,
  `http_method`  varchar(50) NOT NULL,
  `permission`   varchar(250) NOT NULL,
  PRIMARY KEY (`service_name`, `endpoint`, `http_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS auth_token_storage;
CREATE TABLE `auth_token_storage` (
  `id` int(11) NOT null AUTO_INCREMENT, 
  `owner` varchar(50) NOT NULL,
  `token_string` varchar(4000) NOT NULL,
  `expiration`   BIGINT NOT null,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

ALTER TABLE auth_account ADD CONSTRAINT `unique_email` UNIQUE (`email`);
ALTER TABLE auth_user ADD CONSTRAINT `FK_auth_user_auth_account` FOREIGN KEY `auth_user`(`account_id`) REFERENCES `auth_account` (`id`);
ALTER TABLE auth_account_roles ADD CONSTRAINT `fk_auth_acct_roles_auth_roles` FOREIGN KEY (`role_id`) REFERENCES `auth_role` (`id`);
ALTER TABLE auth_account_roles ADD CONSTRAINT `fk_auth_acct_roles_auth_acc` FOREIGN KEY (`ACCOUNT_id`) REFERENCES `auth_account` (`id`);