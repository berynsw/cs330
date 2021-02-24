create database staubwb_wwu;

use staubwb_wwu;


#4.a
select distinct course_id
	from course natural join section
	where section.course_id=course.course_id 
		and (section.semester='Fall' and section.year=2009) 
        or (section.semester='Spring' and section.year=2010);
#4.b
select name, salary
	from instructor
    where salary=(select max(salary) from instructor);
#4.c
select dept_name, avg(salary)
	from instructor
		group by dept_name
		having avg(salary)>42000;
#4.d
select dept_name, max(salary)
	from instructor
    group by dept_name;
#4.e
select distinct student.name
	from student natural join takes
    where (course_id like 'CS%');
#4.f Find the enrollment of each section that was offered in Spring 2009.
select course_id, sec_id, semester, year, count(ID) as enrollment
	from takes natural join student
	where year=2009 and semester='Spring'
	group by course_id, sec_id, semester, year;
#4.g unsure of the wording on this question??? Find the maximum enrollment, across all sections, in Spring 2009.    
select max(T.enrollment)
	from
		(select course_id, sec_id, semester, year, count(ID) enrollment
			from takes natural join student
			where year=2009 and semester='Spring'
				group by course_id, sec_id, semester, year) T;
#4.h  - delete all courses that have never been offered (don't occur in the section relation)
delete
from course
where course_id not in
					(select course_id
					from section);


delete 
	from course 
	where course_id =(select * 
						from (select course.course_id
								from section right join course
								on section.course_id=course.course_id
								where section.course_id is null) as T);


                
select *
from course;
















