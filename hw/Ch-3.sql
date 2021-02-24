create database yourusername_university;

use yourusername_university;

create table department
(dept_name varchar (20),
building varchar (15),
budget numeric (12,2),
primary key (dept_name));

insert into department values ('Biology','Watson',90000);

/** Try to insert **/
insert into department values ('Biology','Watson',90000);
insert into department values (null,'Watson',90000);


insert into department values('Comp. Sci.', 'Taylor', 100000);
insert into department values('Elec. Eng.', 'Taylor', 85000);
insert into department values('Finance', 'Painter', 120000);
insert into department values('History', 'Painter', 50000);
insert into department values('Music', 'Packard', 80000);
insert into department values('Physics', 'Watson', 70000);


/***********************/
create table instructor 
( 
  ID varchar (5),  
    name varchar (20) not null,  
    dept_name varchar (20),  
    salary numeric (8,2),  
    primary key (ID),  
    foreign key (dept_name)  
    references department(dept_name) 
); 

insert into instructor values('10101', 'Srinivasan', 'Comp. Sci.', 65000);
insert into instructor values('12121', 'Wu', 'Finance', 90000);
insert into instructor values('15151', 'Mozart', 'Music', 40000);
insert into instructor values('22222', 'Einstein', 'Physics', 95000);
insert into instructor values('32343', 'El Said', 'History', 60000);
insert into instructor values('33456', 'Gold', 'Physics', 87000);
insert into instructor values('45565', 'Katz', 'Comp. Sci.', 75000);
insert into instructor values('58583', 'Califieri', 'History', 62000);
insert into instructor values('76543', 'Singh', 'Finance', 80000);
insert into instructor values('76766', 'Crick', 'Biology', 72000);
insert into instructor values('83821', 'Brandt', 'Comp. Sci.', 92000);
insert into instructor values('98345', 'Kim', 'Elec. Eng.', 80000);

select * from instructor;

alter table instructor add room_no varchar(4);
alter table instructor drop room_no;

select name from instructor;
select dept_name from instructor;
select distinct dept_name from instructor;
select all dept_name from instructor;

select ID, name, salary*1.1 from instructor;

select name from instructor where dept_name = 'Comp. Sci.' and salary > 80000;

select name, instructor.dept_name,building 
from instructor, department 
where instructor.dept_name=department.dept_name;