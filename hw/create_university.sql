create database yourusername_university;

use yourusername_university;

create table department
(dept_name varchar (20),
building varchar (15),
budget numeric (12,2),
primary key (dept_name));

create table course
(course_id varchar (7),
title varchar (50),
dept_name varchar (20),
credits numeric (2,0),
primary key (course_id),
foreign key (dept_name) references department(dept_name));

create table instructor
(ID varchar (5),
name varchar (20) not null,
dept_name varchar (20),
salary numeric (8,2),
primary key (ID),
foreign key (dept_name) references department(dept_name));

create table student
(ID varchar (5),
name varchar (20) not null,
dept_name varchar (20),
tot_cred numeric (3,0) ,
primary key (ID),
foreign key (dept_name) references department(dept_name));

create table section
(course_id varchar (8),
sec_id varchar(8),
semester varchar (6),
year numeric (4,0),
building varchar (15),
room_number varchar(7),
time_slot_id varchar(4),
primary key (course_id,sec_id,semester,year),
foreign key (course_id) references course(course_id));

create table teaches
(ID varchar(5),
course_id varchar(8),
sec_id varchar(8),
semester varchar (6),
year numeric(4,0),
primary key (ID, course_id, sec_id, semester, year),
foreign key (course_id, sec_id, semester, year) references section(course_id, sec_id, semester, year),
foreign key (ID) references instructor(ID));

create table takes
(ID varchar (5),
course_id varchar (8),
sec_id varchar (8),
semester varchar (6),
year numeric (4,0),
grade varchar (2),
primary key (ID, course_id, sec_id, semester, year),
foreign key (course_id, sec_id, semester, year) references section(course_id, sec_id, semester, year),
foreign key (ID) references student (ID));