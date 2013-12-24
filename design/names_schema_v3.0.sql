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
-- DROP DATABASE IF EXISTS `discs_names`;
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
-- DELETE FROM `configuration`;
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
  `approval_needed` bit(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=cp1250;

-- Dumping data for table discs_names.name_category: ~10 rows (approximately)
-- DELETE FROM `name_category`;
/*!40000 ALTER TABLE `name_category` DISABLE KEYS */;
INSERT INTO `name_category` (`id`, `name`, `description`, `version`, `approval_needed`) VALUES
	(0000000001, 'SUP', 'Super section', 0000000001, 1),
	(0000000002, 'SECT', 'Section', 0000000001, 1),
	(0000000003, 'SUB', 'Subsection', 0000000001, 1),
	(0000000004, 'DSCP', 'Discipline', 0000000001, 1),
	(0000000005, 'CAT', 'Category', 0000000001, 1),
	(0000000006, 'GDEV', 'Generic device', 0000000001, 1),
	(0000000007, 'SDEV', 'Specific device', 0000000001, 0);
/*!40000 ALTER TABLE `name_category` ENABLE KEYS */;


-- Dumping structure for table discs_names.name_event
DROP TABLE IF EXISTS `name_event`;
CREATE TABLE IF NOT EXISTS `name_event` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `name_id` varchar(64) NOT NULL,
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
-- DELETE FROM `name_event`;
/*!40000 ALTER TABLE `name_event` DISABLE KEYS */;
INSERT INTO `name_event` (`id`, `name_id`, `name`, `full_name`, `name_category_id`, `parent_name_id`, `event_type`, `requested_by`, `request_date`, `status`, `processed_by`, `process_date`, `requestor_comment`, `processor_comment`, `version`) VALUES
	(0000000001, 'a', 'Sup1', 'Super Section 1', 1, NULL, 'INSERT', 2, '2013-12-06 14:59:48', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000002, 'b', 'Sup2', 'Super Section 2', 1, NULL, 'INSERT', 2, '2013-12-06 15:15:27', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000003, 'c', 'Sec1', 'Section 1', 2, 1, 'INSERT', 2, '2013-12-09 09:06:15', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000004, 'd', 'Sec2', 'Section 2', 2, 2, 'INSERT', 2, '2013-12-09 09:06:24', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000005, 'e', 'Sec3', 'Section 3', 2, 2, 'INSERT', 2, '2013-12-06 15:15:27', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000006, 'f', '01', 'Subsection 1', 3, 3, 'INSERT', 2, '2013-12-06 15:15:27', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000007, 'g', '02', 'Subsection 2', 3, 3, 'INSERT', 2, '2013-12-06 15:15:27', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000008, 'h', '03', 'Subsection 3', 3, 4, 'INSERT', 2, '2013-12-06 15:15:27', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000009, 'i', '04', 'Subsection 4', 3, 5, 'INSERT', 2, '2013-12-06 15:15:27', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000010, 'j', 'Dsc1', 'Discipline 1', 4, NULL, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000011, 'k', 'Dsc2', 'Discipline 2', 4, NULL, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000012, 'l', 'Dsc3', 'Discipline 3', 4, NULL, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000013, 'm', 'Cat1', 'Category 1', 5, 10, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000014, 'n', 'Cat2', 'Category 2', 5, 10, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000015, 'o', 'Cat3', 'Category 3', 5, 11, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000016, 'p', 'GDv1', 'Generic Device 1', 6, 14, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000017, 'q', 'GDv2', 'Generic Device 2', 6, 13, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000018, 'r', 'GDv3', 'Generic Device 3', 6, 13, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000019, 's', 'SDv1', 'Specific Device 1', 7, 16, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000020, 't', 'SDv2', 'Specific Device 2', 7, 17, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001),
	(0000000021, 'u', 'SDv3', 'Specific Device 3', 7, 18, 'INSERT', 2, '2013-12-09 10:20:42', 'APPROVED', 1, '2013-12-11 10:09:08', 'A', 'B', 0000000001);
/*!40000 ALTER TABLE `name_event` ENABLE KEYS */;


-- Dumping structure for table discs_names.name_release
DROP TABLE IF EXISTS `name_release`;
CREATE TABLE IF NOT EXISTS `name_release` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `rel_id` varchar(16) NOT NULL UNIQUE,
  `description` varchar(255) NOT NULL,
  `doc_url` varchar(255) DEFAULT NULL,
  `release_date` datetime NOT NULL,
  `released_by` int(10) unsigned NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_released_by_privilege_id` (`released_by`),
  CONSTRAINT `FK_released_by_privilege_id` FOREIGN KEY (`released_by`) REFERENCES `privilege` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COMMENT='Each row is a naming system release';

-- Dumping data for table discs_names.name_release: ~0 rows (approximately)
-- DELETE FROM `name_release`;
/*!40000 ALTER TABLE `name_release` DISABLE KEYS */;
/*!40000 ALTER TABLE `name_release` ENABLE KEYS */;


-- Dumping structure for table discs_names.device_name
DROP TABLE IF EXISTS `device_name`;
CREATE TABLE IF NOT EXISTS `device_name` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `name_id` varchar(64) NOT NULL,
  `section_id` int(10) unsigned NOT NULL,
  `device_type_id` int(10) unsigned NOT NULL,
  `instance_index` varchar(10) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'invalid',
  `requested_by` int(10) unsigned NOT NULL,
  `request_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_by` int(10) unsigned DEFAULT NULL,
  `process_date` timestamp NULL DEFAULT NULL,
  `version` int(10) unsigned zerofill NOT NULL DEFAULT '0000000001',
  PRIMARY KEY (`id`),
  UNIQUE KEY `section_id` (`section_id`,`device_type_id`,`instance_index`),
  KEY `FK_device_name_device_type_id` (`device_type_id`),
  CONSTRAINT `FK_device_name_discipline_device_type_id` FOREIGN KEY (`device_type_id`) REFERENCES `name_event` (`id`),
  CONSTRAINT `FK_device_name_section_id` FOREIGN KEY (`section_id`) REFERENCES `name_event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250;

-- Dumping data for table discs_names.device_name: ~0 rows (approximately)
-- DELETE FROM `device_name`;
/*!40000 ALTER TABLE `device_name` DISABLE KEYS */;
/*!40000 ALTER TABLE `device_name` ENABLE KEYS */;


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
-- DELETE FROM `privilege`;
/*!40000 ALTER TABLE `privilege` DISABLE KEYS */;
INSERT INTO `privilege` (`id`, `username`, `operation`, `version`) VALUES
	(0000000001, 'root', 'S', 0000000000),
	(0000000002, 'jaba', 'E', 0000000000),
	(0000000003, 'admin', 'S', 0000000000),
	(0000000004, 'miha', 'E', 0000000000);
/*!40000 ALTER TABLE `privilege` ENABLE KEYS */;
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
