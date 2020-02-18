-- phpMyAdmin SQL Dump
-- version 4.6.6deb5
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Feb 17, 2020 at 07:30 PM
-- Server version: 5.7.29-0ubuntu0.16.04.1
-- PHP Version: 7.0.33-0ubuntu0.16.04.9

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `jjag3/SurgeryAssistant`
--

-- --------------------------------------------------------

--
-- Table structure for table `Appointments`
--

CREATE TABLE `Appointments` (
  `app_id` int(10) UNSIGNED NOT NULL,
  `patient_id` int(11) NOT NULL,
  `doctor` int(11) NOT NULL,
  `timeslot_id` int(11) NOT NULL,
  `conversation_state_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Appointments`
--

INSERT INTO `Appointments` (`app_id`, `patient_id`, `doctor`, `timeslot_id`, `conversation_state_id`) VALUES
(1, 1, 1, 1, 1),
(2, 2, 2, 2, 2),
(3, 3, 3, 3, 3),
(4, 4, 3, 7, 4),
(5, 5, 4, 8, 5);

-- --------------------------------------------------------

--
-- Table structure for table `Conversation State`
--

CREATE TABLE `Conversation State` (
  `conversation_state_id` int(11) NOT NULL,
  `7_day_reminder_sent` tinyint(4) NOT NULL,
  `1_day_reminder_sent` tinyint(4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Conversation State`
--

INSERT INTO `Conversation State` (`conversation_state_id`, `7_day_reminder_sent`, `1_day_reminder_sent`) VALUES
(1, 0, 0),
(2, 1, 0),
(3, 0, 0),
(4, 0, 0),
(5, 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `Doctors`
--

CREATE TABLE `Doctors` (
  `doctor_id` int(11) NOT NULL,
  `doctor_name` varchar(45) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Doctors`
--

INSERT INTO `Doctors` (`doctor_id`, `doctor_name`) VALUES
(1, 'Dr. Aloha'),
(2, 'Dr Barbara'),
(3, 'Dr Cindy'),
(4, 'Dr Dora'),
(5, 'Dr Emma'),
(6, 'Dr Fen'),
(7, 'Dr Greg'),
(8, 'Dr House'),
(9, 'Dr Ian'),
(10, 'Dr JJ');

-- --------------------------------------------------------

--
-- Table structure for table `Logs`
--

CREATE TABLE `Logs` (
  `app_id` int(10) NOT NULL,
  `email_body` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Logs`
--

INSERT INTO `Logs` (`app_id`, `email_body`) VALUES
(1, 'whats up bro'),
(2, 'toit nups'),
(2, 'toit nups'),
(3, 'What what');

-- --------------------------------------------------------

--
-- Table structure for table `Patients`
--

CREATE TABLE `Patients` (
  `patient_id` int(11) NOT NULL,
  `patient_email` varchar(255) NOT NULL,
  `patient_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Patients`
--

INSERT INTO `Patients` (`patient_id`, `patient_email`, `patient_name`) VALUES
(1, 'va308@cam.ac.uk', 'Vasundhara'),
(2, 'jjag3@cam.ac.uk', 'JJ'),
(3, 'spv28@cam.ac.uk', 'Shaun'),
(4, 'dd525@cam.ac.uk', 'Delia'),
(5, 'mmm67@cam.ac.uk', 'Mila'),
(6, 'sm2354@cam.ac.uk', 'Simonas');

-- --------------------------------------------------------

--
-- Table structure for table `Timeslots`
--

CREATE TABLE `Timeslots` (
  `timeslot_id` int(11) NOT NULL,
  `timeslot` datetime NOT NULL,
  `available` tinyint(4) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `location` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Timeslots`
--

INSERT INTO `Timeslots` (`timeslot_id`, `timeslot`, `available`, `doctor_id`, `location`) VALUES
(1, '2020-02-19 10:00:00', 0, 1, 'Addenbrookes'),
(2, '2020-02-20 11:00:00', 0, 2, 'Addenbrookes'),
(3, '2020-02-21 12:00:00', 0, 3, 'Addenbrookes'),
(4, '2020-02-21 13:00:00', 1, 4, 'Addenbrookes'),
(5, '2020-02-22 13:00:00', 1, 1, 'Addenbrookes'),
(6, '2020-02-23 10:00:00', 1, 2, 'Addenbrookes'),
(7, '2020-02-24 14:00:00', 0, 3, 'Addenbrookes'),
(8, '2020-02-25 15:00:00', 0, 4, 'Newnham Walk Surgery'),
(9, '2020-02-22 14:00:00', 1, 5, 'Newnham Walk Surgery'),
(10, '2020-02-23 13:00:00', 1, 6, 'Newnham Walk Surgery'),
(11, '2020-02-23 11:00:00', 1, 7, 'Newnham Walk Surgery');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Appointments`
--
ALTER TABLE `Appointments`
  ADD PRIMARY KEY (`app_id`),
  ADD UNIQUE KEY `app_id` (`app_id`),
  ADD KEY `doctor` (`doctor`),
  ADD KEY `patient_id` (`patient_id`),
  ADD KEY `conversation_state_id` (`conversation_state_id`),
  ADD KEY `timeslot_id` (`timeslot_id`);

--
-- Indexes for table `Conversation State`
--
ALTER TABLE `Conversation State`
  ADD PRIMARY KEY (`conversation_state_id`);

--
-- Indexes for table `Doctors`
--
ALTER TABLE `Doctors`
  ADD PRIMARY KEY (`doctor_id`);

--
-- Indexes for table `Patients`
--
ALTER TABLE `Patients`
  ADD PRIMARY KEY (`patient_id`);

--
-- Indexes for table `Timeslots`
--
ALTER TABLE `Timeslots`
  ADD PRIMARY KEY (`timeslot_id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `Appointments`
--
ALTER TABLE `Appointments`
  ADD CONSTRAINT `Appointments_ibfk_1` FOREIGN KEY (`doctor`) REFERENCES `Doctors` (`doctor_id`),
  ADD CONSTRAINT `Appointments_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `Patients` (`patient_id`),
  ADD CONSTRAINT `Appointments_ibfk_3` FOREIGN KEY (`conversation_state_id`) REFERENCES `Conversation State` (`conversation_state_id`),
  ADD CONSTRAINT `Appointments_ibfk_4` FOREIGN KEY (`timeslot_id`) REFERENCES `Timeslots` (`timeslot_id`);

--
-- Constraints for table `Timeslots`
--
ALTER TABLE `Timeslots`
  ADD CONSTRAINT `Timeslots_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `Doctors` (`doctor_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
