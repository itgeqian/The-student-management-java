-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: student_management
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程描述',
  `credit` decimal(3,1) NOT NULL COMMENT '学分',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `department_id` bigint NOT NULL COMMENT '院系ID',
  `course_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程代码',
  `max_students` int DEFAULT '50' COMMENT '最大学生数',
  `semester` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期',
  `class_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上课时间',
  `classroom` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '教室',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_course_code` (`course_code`) USING BTREE,
  KEY `idx_teacher_id` (`teacher_id`) USING BTREE,
  KEY `idx_department_id` (`department_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='课程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,'Java程序设计','Java编程基础与实践',4.0,1,111,'JAVA-101',50,'2023-2024-1','周一 1-2节','A101',1,'2024-12-07 11:26:22','2024-12-07 12:03:29'),(2,'数据库原理','数据库设计与SQL编程',3.0,2,111,'DB-101',45,'2023-2024-1','周二 3-4节','B202',1,'2024-12-07 11:26:22','2024-12-07 12:03:31'),(3,'计算机网络','网络协议与架构',3.5,3,111,'NET-101',60,'2023-2024-1','周三 5-6节','C303',1,'2024-12-07 11:26:22','2024-12-07 12:03:34'),(5,'软件工程','111',2.0,156816341100000,112,'g001',50,'2023-2024-2',NULL,NULL,1,'2024-12-09 14:00:32','2024-12-09 14:00:32');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_class`
--

DROP TABLE IF EXISTS `course_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_class` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '班级名称',
  `max_student` int NOT NULL DEFAULT '50' COMMENT '最大学生数',
  `semester` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学期',
  `class_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上课时间',
  `classroom` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '教室',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否激活',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '班级描述',
  `homework_weight` int NOT NULL DEFAULT '30' COMMENT '作业成绩权重',
  `experiment_weight` int NOT NULL DEFAULT '30' COMMENT '实验成绩权重',
  `final_exam_weight` int NOT NULL DEFAULT '40' COMMENT '期末考试权重',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=604346731100001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='课程班级表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_class`
--

LOCK TABLES `course_class` WRITE;
/*!40000 ALTER TABLE `course_class` DISABLE KEYS */;
INSERT INTO `course_class` VALUES (1,4,4,'2323232',50,'2023-2024-1',NULL,'2323232',1,NULL,10,10,80,'2024-12-07 14:18:49','2024-12-08 02:38:50'),(2,1,4,'计本2205',50,'2023-2024-2','MON_3_4','地点',1,NULL,10,10,80,'2024-12-07 22:43:18','2024-12-08 02:33:16'),(40285681100000,3,606625201100000,'计本2204',50,'2023-2024-2','MON_5_6','9A220',1,NULL,30,30,40,'2024-12-09 10:43:52','2024-12-09 10:43:52'),(158975921100000,5,156816341100000,'班级',50,'2023-2024-2','TUE_3_4','9A220',1,NULL,30,30,40,'2024-12-09 14:01:41','2024-12-09 14:01:41'),(604346731100000,2,599014441100000,'计本2204',50,'2023-2024-2','MON_7_8','9A220',1,NULL,30,30,40,'2024-12-08 22:37:18','2024-12-08 22:37:18');
/*!40000 ALTER TABLE `course_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_class_student`
--

DROP TABLE IF EXISTS `course_class_student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_class_student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_class_id` bigint NOT NULL COMMENT '课程班级ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-在读，QUIT-已退出',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='课程班级学生关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_class_student`
--

LOCK TABLES `course_class_student` WRITE;
/*!40000 ALTER TABLE `course_class_student` DISABLE KEYS */;
/*!40000 ALTER TABLE `course_class_student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_selection`
--

DROP TABLE IF EXISTS `course_selection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_selection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_class_id` bigint NOT NULL COMMENT '课程班级ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `homework_score` int DEFAULT '0' COMMENT '作业成绩',
  `experiment_score` int DEFAULT '0' COMMENT '实验成绩',
  `final_exam_score` int DEFAULT '0' COMMENT '期末考试成绩',
  `total_score` int DEFAULT '0' COMMENT '总成绩',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SELECTED' COMMENT '选课状态：SELECTED-已选, DROPPED-已退, COMPLETED-已完成',
  `select_time` datetime NOT NULL COMMENT '选课时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course_class_id` (`course_class_id`) USING BTREE,
  KEY `idx_student_id` (`student_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='选课记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_selection`
--

LOCK TABLES `course_selection` WRITE;
/*!40000 ALTER TABLE `course_selection` DISABLE KEYS */;
INSERT INTO `course_selection` VALUES (1,1,6,0,0,0,0,'SELECTED','2024-12-07 16:09:52','2024-12-08 02:41:31'),(2,2,6,0,0,0,0,'SELECTED','2024-12-08 02:25:51','2024-12-08 02:33:45'),(3,2,6,0,0,0,0,'SELECTED','2024-12-08 02:48:21','2024-12-08 02:48:26'),(4,2,607739101100000,0,0,0,0,'SELECTED','2024-12-08 22:48:24','2024-12-08 22:48:24'),(5,40285681100000,614983461100000,0,0,0,0,'SELECTED','2024-12-09 11:36:13','2024-12-09 11:36:13'),(6,40285681100000,607739101100000,0,0,0,0,'SELECTED','2024-12-09 11:38:58','2024-12-09 11:38:58'),(7,604346731100000,614983461100000,0,0,0,0,'SELECTED','2024-12-09 11:50:24','2024-12-09 11:50:24'),(8,40285681100000,157630061100000,0,0,0,0,'SELECTED','2024-12-09 14:20:06','2024-12-09 14:20:06'),(9,158975921100000,157630061100000,0,0,0,0,'SELECTED','2024-12-09 14:21:42','2024-12-09 14:21:42');
/*!40000 ALTER TABLE `course_selection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (111,'部门','11','2024-12-07 10:31:45'),(112,'数计学院','求求你们别卷了','2024-12-08 22:27:59'),(113,'电气学院','','2024-12-08 22:41:17');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `final_grade`
--

DROP TABLE IF EXISTS `final_grade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `final_grade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `exam_score` decimal(5,2) DEFAULT NULL,
  `homework_score` decimal(5,2) DEFAULT NULL,
  `experiment_score` decimal(5,2) DEFAULT NULL,
  `total_score` decimal(5,2) DEFAULT NULL,
  `is_published` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_submitted` tinyint(1) DEFAULT NULL,
  `publish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `final_grade`
--

LOCK TABLES `final_grade` WRITE;
/*!40000 ALTER TABLE `final_grade` DISABLE KEYS */;
INSERT INTO `final_grade` VALUES (1,1,6,100.00,20.00,0.00,64.00,1,'2024-12-07 22:57:31','2024-12-07 23:13:09',1,NULL),(3,40285681100000,614983461100000,80.00,80.00,85.00,81.00,1,'2024-12-09 18:11:48','2024-12-09 18:17:28',1,NULL);
/*!40000 ALTER TABLE `final_grade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grade_composition`
--

DROP TABLE IF EXISTS `grade_composition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grade_composition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_class_id` bigint NOT NULL,
  `homework_percentage` int NOT NULL,
  `experiment_percentage` int NOT NULL,
  `exam_percentage` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grade_composition`
--

LOCK TABLES `grade_composition` WRITE;
/*!40000 ALTER TABLE `grade_composition` DISABLE KEYS */;
/*!40000 ALTER TABLE `grade_composition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `homework`
--

DROP TABLE IF EXISTS `homework`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `homework` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_class_id` bigint NOT NULL DEFAULT '0',
  `title` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
  `attachment_url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `deadline` datetime NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `type` varchar(25) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `homework`
--

LOCK TABLES `homework` WRITE;
/*!40000 ALTER TABLE `homework` DISABLE KEYS */;
INSERT INTO `homework` VALUES (1,1,'1111','111',NULL,'2024-12-09 16:00:00','2024-12-07 13:54:03','HOMEWORK'),(2,1,'242424','2434',NULL,'2024-12-07 14:03:32','2024-12-07 22:03:42','HOMEWORK'),(3,0,'232','232',NULL,'2024-12-07 14:10:16','2024-12-07 22:10:21','HOMEWORK'),(4,1,'45353','3453453',NULL,'2024-12-25 16:00:00','2024-12-07 22:11:42','HOMEWORK'),(5,1,'13232','2323',NULL,'2024-12-07 14:21:30','2024-12-07 22:21:39','HOMEWORK'),(6,1,'242','2342',NULL,'2024-12-24 06:23:42','2024-12-07 22:24:01','HOMEWORK'),(7,1,'沃尔沃','森扽','homework\\2024\\12\\07\\717185f3\\6d36\\微信图片_20241205155756.jpg','2024-12-26 06:26:15','2024-12-07 22:26:21','EXAM'),(8,2,'实验报告','提交实验报告','homework\\2024\\12\\09\\4a653981\\0799\\软件工程实验指导书（2024）.doc','2024-12-09 16:00:00','2024-12-09 11:26:22','HOMEWORK'),(9,40285681100000,'11','111','','2024-12-15 16:00:00','2024-12-09 11:32:35','HOMEWORK'),(10,40285681100000,'123','123','','2024-12-09 16:00:00','2024-12-09 11:45:28','HOMEWORK'),(11,158975921100000,'随便写个作业','随便','homework\\2024\\12\\09\\9e7580d5\\b903\\992f7e6af7239dfaf48a0f4239c0fe3.jpg','2024-12-18 06:03:14','2024-12-09 14:03:23','HOMEWORK'),(12,40285681100000,'第2次作业','111','','2024-12-10 16:00:00','2024-12-09 17:28:02','EXPERIMENT'),(13,40285681100000,'期末作业','期末','','2024-12-16 16:00:00','2024-12-09 17:30:00','EXAM');
/*!40000 ALTER TABLE `homework` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `homework_submission`
--

DROP TABLE IF EXISTS `homework_submission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `homework_submission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `homework_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `answer` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
  `attachment_url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `score` int DEFAULT NULL,
  `comment` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
  `status` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT 'SUBMITTED',
  `submit_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `homework_submission`
--

LOCK TABLES `homework_submission` WRITE;
/*!40000 ALTER TABLE `homework_submission` DISABLE KEYS */;
INSERT INTO `homework_submission` VALUES (2,1,6,'1111','homework\\\\2024\\\\12\\\\07\\\\55abc294\\\\06f4\\\\微信图片_20241205155756.jpg',20,'','GRADED','2024-12-07 18:00:09','2024-12-07 18:58:07'),(3,7,6,'234242342424',NULL,100,'33333333333333333333333333333','GRADED','2024-12-07 22:51:08','2024-12-07 22:57:31'),(4,4,6,'23232323',NULL,NULL,NULL,'SUBMITTED','2024-12-08 03:23:37','2024-12-08 03:23:37'),(5,7,607739101100000,'123',NULL,NULL,NULL,'SUBMITTED','2024-12-08 22:48:56','2024-12-08 22:48:56'),(6,10,614983461100000,'我完成了',NULL,80,NULL,'GRADED','2024-12-09 11:46:24','2024-12-09 17:10:48'),(10,9,614983461100000,'我完成了','',NULL,NULL,'SUBMITTED','2024-12-09 17:09:11','2024-12-09 17:09:11'),(11,12,614983461100000,'我的答案','homework\\2024\\12\\09\\321b9aa6\\2753\\992f7e6af7239dfaf48a0f4239c0fe3.jpg',85,'good！','GRADED','2024-12-09 17:28:32','2024-12-09 17:31:43'),(12,13,614983461100000,'我完成了期末作业','',80,NULL,'GRADED','2024-12-09 17:30:17','2024-12-09 18:20:15');
/*!40000 ALTER TABLE `homework_submission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `student_no` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `department_id` bigint DEFAULT NULL,
  `grade` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `class_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `gender` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `student_no` (`student_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=614983461100001 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (6,6,'1234567890',111,'2024','232','15122222222','120@qq.com','test11',1),(157630061100000,157630061100000,'S01',112,'2023','计本2206','13800138002','2933909453@qq.com','测试同学',1),(605768751100000,605768751100000,'202214024113',112,'2024','计本2204','13345233550','2933909350@qq.com','彭于晏',1),(607739101100000,607739101100000,'202214024114',113,'2024','电气2204','15045673589','2933909352@qq.com','胡歌',1),(614983461100000,614983461100000,'student',111,'2024','计本2204','13800138001','2933909353@qq.com','student',1);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=124114351882899459 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$ZgpTGBDBlOPZtXD5hSmXD.oN./iGFq/6ETUs0zsNNT1ATkw4eBP9S','管理员','ADMIN','2024-12-06 23:31:49','2024-12-08 16:09:46','2933909350@qq.com','13345233550'),(156816341100000,'T003','$2a$10$GDzoG9/5e5BjJ22CU8QLyuHoE45VTLehuaUjBWxwtVQZZ/IScK6ri','李老师','TEACHER','2024-12-09 13:58:05','2024-12-09 13:58:05',NULL,NULL),(157630061100000,'S01','$2a$10$1vVfCuLuvxcCZ8XNdRNDXeGInT2u6Jix8fU0X7O4fesS.FnM9000m','测试同学','STUDENT','2024-12-09 13:59:27','2024-12-09 13:59:27',NULL,NULL),(599014441100000,'T001','$2a$10$Po9kkhlp7W9FAc/LZL4bQOXesbpNW96nYUUEz6Wm2eg/GA2Xzi/2a','张光伟','TEACHER','2024-12-08 22:28:25','2024-12-09 12:23:48','2933909350@qq.com','15029054462'),(605768751100000,'202214024113','$2a$10$kcDZTEOeEil50LdxQLeWje/SCsg5/8u5IL/Qn6.qHZa3NMFCGkUva','彭于晏','STUDENT','2024-12-08 22:39:41','2024-12-08 22:39:41','2933909350@qq.com','13345233550'),(606625201100000,'T002','$2a$10$YZNRXqDG3frFYU1zD4bj1eCKSxJGQIdMgzaxDBH1geyovnQdGDMy.','吴彦祖','TEACHER','2024-12-08 22:41:06','2024-12-09 12:23:38',NULL,NULL),(607739101100000,'202214024114','$2a$10$50.a7K8iNH.em5tfW9skneuf0D99.IjMWiOlWMu6W9tTY.kb2dPDS','胡歌','STUDENT','2024-12-08 22:42:58','2024-12-08 22:42:58',NULL,NULL),(613853571100000,'teacher','$2a$10$8IklSJx5f6e1nyQ8PMJ6me/2AaYSXH85vodIsiFgsuPqdAeoEYju.','teacher','TEACHER','2024-12-08 22:53:09','2024-12-08 22:53:09',NULL,NULL),(614983461100000,'student','$2a$10$bdiAFJdaLrEg4lICT4ef6uDdIDy7JSnvzCciZu7qIQdBavdXLZW5q','student','STUDENT','2024-12-08 22:55:02','2024-12-08 22:55:02',NULL,NULL),(124114351882899457,'2933909350','$2a$10$FDa/t8RYBkorNjDWQKzF.OtlP/cMDsa0dPEs6CounoeCAJPpYlC5i','王磊','ADMIN','2024-12-08 11:52:55','2024-12-08 16:09:50','2933909350@qq.com','13345233550'),(124114351882899458,'admin111','$2a$10$4Ya4trWMoGDrkFqhmzJWW.jzeU.Fy.ihIdu5fZxhhvEJkCN9yH8wC','admin','ADMIN','2024-12-09 13:57:13','2024-12-09 13:57:13','','');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `department_id` bigint DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `teacher_no` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=124114351882899457 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

LOCK TABLES `teacher` WRITE;
/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (156816341100000,156816341100000,112,'李老师','','','T003'),(599014441100000,599014441100000,112,'张光伟','13345233550','2933909350@qq.com','T001'),(606625201100000,606625201100000,113,'吴彦祖','13345233550','2933909350@qq.com','T002'),(613853571100000,613853571100000,111,'teacher','','','teacher');
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-12-09 20:46:41
