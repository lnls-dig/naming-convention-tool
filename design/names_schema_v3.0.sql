-- --------------------------------------------------------
-- Host:                         localhost
-- Server version:               5.5.20 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2013-12-09 11:16:43
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;

-- Dumping database structure for discs_names
DROP DATABASE IF EXISTS `discs_names`;
CREATE DATABASE IF NOT EXISTS `discs_names` /*!40100 DEFAULT CHARACTER SET cp1250 */;
USE `discs_names`;


-- Dumping structure for table discs_names.configuration
DROP TABLE IF EXISTS `configuration`;
CREATE TABLE IF NOT EXISTS `configuration` (
  `name` varchar(64) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COMMENT='Each row is a module property';

-- Dumping data for table discs_names.configuration: ~1 rows (approximately)
DELETE FROM `configuration`;
/*!40000 ALTER TABLE `configuration` DISABLE KEYS */;
INSERT INTO `configuration` (`name`, `value`) VALUES
	('version', '3.0');
/*!40000 ALTER TABLE `configuration` ENABLE KEYS */;


-- Dumping structure for table discs_names.name_category
DROP TABLE IF EXISTS `name_category`;
CREATE TABLE IF NOT EXISTS `name_category` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(50) DEFAULT NULL,
  `version` int(10) unsigned zerofill NOT NULL DEFAULT '0000000001',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=cp1250;

-- Dumping data for table discs_names.name_category: ~10 rows (approximately)
DELETE FROM `name_category`;
/*!40000 ALTER TABLE `name_category` DISABLE KEYS */;
INSERT INTO `name_category` (`id`, `name`, `description`, `version`) VALUES
	(0000000001, 'SUP', 'Super section', 0000000001),
	(0000000002, 'SECT', 'Section', 0000000001),
	(0000000003, 'SUB', 'Subsection', 0000000001),
	(0000000004, 'DSCP', 'Discipline', 0000000001),
	(0000000005, 'CAT', 'Category', 0000000001),
	(0000000006, 'GDEV', 'Generic device', 0000000001),
	(0000000007, 'SDEV', 'Specific device', 0000000001),
	(0000000008, 'STYP', 'Signal type', 0000000001),
	(0000000009, 'SINS', 'Signal instance', 0000000001),
	(0000000010, 'ADS', 'Additional signal description', 0000000001);
/*!40000 ALTER TABLE `name_category` ENABLE KEYS */;


-- Dumping structure for table discs_names.name_event
DROP TABLE IF EXISTS `name_event`;
CREATE TABLE IF NOT EXISTS `name_event` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '',
  `full_name` varchar(50) NOT NULL DEFAULT '',
  `name_category_id` int(10) unsigned NOT NULL,
  `parent_name_id` int(10) unsigned DEFAULT NULL,
  `event_type` varchar(50) NOT NULL,
  `requested_by` int(10) unsigned NOT NULL,
  `request_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(50) NOT NULL DEFAULT 'unconfirmed',
  `processed_by` int(10) unsigned DEFAULT NULL,
  `process_date` timestamp NULL DEFAULT NULL,
  `requestor_comment` varchar(255) DEFAULT NULL,
  `processor_comment` varchar(255) DEFAULT NULL,
  `version` int(10) unsigned zerofill NOT NULL DEFAULT '0000000001',
  PRIMARY KEY (`id`),
  KEY `FK_parent_name_name` (`parent_name_id`),
  KEY `FK_requested_by_privilege_id` (`requested_by`),
  KEY `FK_processed_by_privilege_id` (`processed_by`),
  KEY `FK_name_event_name_category_id` (`name_category_id`),
  CONSTRAINT `FK_name_event_name_category_id` FOREIGN KEY (`name_category_id`) REFERENCES `name_category` (`id`),
  CONSTRAINT `FK_parent_name_name` FOREIGN KEY (`parent_name_id`) REFERENCES `name_event` (`id`),
  CONSTRAINT `FK_processed_by_privilege_id` FOREIGN KEY (`processed_by`) REFERENCES `privilege` (`id`),
  CONSTRAINT `FK_requested_by_privilege_id` FOREIGN KEY (`requested_by`) REFERENCES `privilege` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=cp1250;

-- Dumping data for table discs_names.name_event: ~12 rows (approximately)
DELETE FROM `name_event`;
/*!40000 ALTER TABLE `name_event` DISABLE KEYS */;
INSERT INTO `name_event` (`id`, `name`, `full_name`, `name_category_id`, `parent_name_id`, `event_type`, `requested_by`, `request_date`, `status`, `processed_by`, `process_date`, `requestor_comment`, `processor_comment`, `version`) VALUES
	(0000000001, 'Sup1', 'Super Section 1', 1, NULL, 'i', 2, '2013-12-06 14:59:48', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000002, 'Sup2', 'Super Section 2', 1, NULL, 'i', 2, '2013-12-06 15:15:27', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000003, 'Sec1', 'Section 1', 2, 1, 'i', 2, '2013-12-09 09:06:15', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000004, 'Sec2', 'Section 2', 2, 2, 'i', 2, '2013-12-09 09:06:24', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000005, 'Sec3', 'Section 3', 2, 2, 'i', 2, '2013-12-06 15:15:27', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000006, '01', 'Subsection 1', 3, 3, 'i', 2, '2013-12-06 15:15:27', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000007, '02', 'Subsection 2', 3, 3, 'i', 2, '2013-12-06 15:15:27', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000008, '03', 'Subsection 3', 3, 4, 'i', 2, '2013-12-06 15:15:27', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000009, '04', 'Subsection 4', 3, 5, 'i', 2, '2013-12-06 15:15:27', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000010, 'Dsc1', 'Discipline 1', 4, NULL, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000011, 'Dsc2', 'Discipline 2', 4, NULL, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000012, 'Dsc3', 'Discipline 3', 4, NULL, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000013, 'Cat1', 'Category 1', 5, 10, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000014, 'Cat2', 'Category 2', 5, 10, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000015, 'Cat3', 'Category 3', 5, 11, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000016, 'GDv1', 'Generic Device 1', 6, 14, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000017, 'GDv2', 'Generic Device 2', 6, 13, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000018, 'GDv3', 'Generic Device 3', 6, 13, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000019, 'SDv1', 'Specific Device 1', 7, 16, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000020, 'SDv2', 'Specific Device 2', 7, 17, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000021, 'SDv3', 'Specific Device 3', 7, 18, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000022, 'S001', 'Signal (Type) 1', 8, NULL, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000023, 'S002', 'Signal (Type) 2', 8, NULL, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001),
	(0000000024, 'S003', 'Signal (Type) 3', 8, NULL, 'i', 2, '2013-12-09 10:20:42', 'a', NULL, NULL, NULL, NULL, 0000000001);
/*!40000 ALTER TABLE `name_event` ENABLE KEYS */;


-- Dumping structure for table discs_names.name_release
DROP TABLE IF EXISTS `name_release`;
CREATE TABLE IF NOT EXISTS `name_release` (
  `id` varchar(16) NOT NULL,
  `description` varchar(255) NOT NULL,
  `doc_url` varchar(255) DEFAULT NULL,
  `release_date` datetime NOT NULL,
  `released_by` varchar(64) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COMMENT='Each row is a naming system release';

-- Dumping data for table discs_names.name_release: ~0 rows (approximately)
DELETE FROM `name_release`;
/*!40000 ALTER TABLE `name_release` DISABLE KEYS */;
/*!40000 ALTER TABLE `name_release` ENABLE KEYS */;


-- Dumping structure for table discs_names.NC_name
DROP TABLE IF EXISTS `NC_name`;
CREATE TABLE IF NOT EXISTS `NC_name` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `section_id` int(10) unsigned NOT NULL,
  `discipline_id` int(10) unsigned NOT NULL,
  `signal_id` int(10) unsigned DEFAULT NULL,
  `instance_index` char(1) DEFAULT NULL,
  `name` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'invalid',
  `version` int(10) unsigned zerofill NOT NULL DEFAULT '0000000001',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `section_id` (`section_id`,`discipline_id`,`signal_id`,`instance_index`),
  KEY `FK_NC_name_discipline_id` (`discipline_id`),
  KEY `FK_NC_name_signal_id` (`signal_id`),
  CONSTRAINT `FK_NC_name_discipline_id` FOREIGN KEY (`discipline_id`) REFERENCES `name_event` (`id`),
  CONSTRAINT `FK_NC_name_section_id` FOREIGN KEY (`section_id`) REFERENCES `name_event` (`id`),
  CONSTRAINT `FK_NC_name_signal_id` FOREIGN KEY (`signal_id`) REFERENCES `name_event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250;

-- Dumping data for table discs_names.NC_name: ~0 rows (approximately)
DELETE FROM `NC_name`;
/*!40000 ALTER TABLE `NC_name` DISABLE KEYS */;
/*!40000 ALTER TABLE `NC_name` ENABLE KEYS */;


-- Dumping structure for table discs_names.privilege
DROP TABLE IF EXISTS `privilege`;
CREATE TABLE IF NOT EXISTS `privilege` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `operation` varchar(1) NOT NULL DEFAULT 'g',
  `version` int(10) unsigned zerofill NOT NULL DEFAULT '0000000001',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=cp1250;

-- Dumping data for table discs_names.privilege: ~2 rows (approximately)
DELETE FROM `privilege`;
/*!40000 ALTER TABLE `privilege` DISABLE KEYS */;
INSERT INTO `privilege` (`id`, `username`, `operation`, `version`) VALUES
	(0000000001, 'root', 'S', 0000000000),
	(0000000002, 'jaba', 'E', 0000000000);
/*!40000 ALTER TABLE `privilege` ENABLE KEYS */;
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
