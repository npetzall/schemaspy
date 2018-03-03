CREATE DATABASE test_enum_schema;
USE test_enum_schema;
CREATE TABLE `entity` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `entityID` VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL,
  `published` enum('N','Y') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'N',
  PRIMARY KEY (`id`),
  UNIQUE KEY `entity_entityid_unique` (`entityID`)
);