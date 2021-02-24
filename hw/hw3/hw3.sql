#a
select Name
from Track
where Name like 'z%'
order by Name;

#b find names of employees who are older than their supervisor

select *
from Employee;

select FirstName
from Employee A
where A.BirthDate < (select BirthDate from Employee where A.ReportsTo = EmployeeId)
order by FirstName;

#c
select Name
from Track
where UnitPrice = (select max(UnitPrice) from Track)
order by Name;

#d
select CustomerID, LastName,
case
	when sum(Total) is null then sum(Total) = 0.00
    else sum(Total)
end as spent
from Customer natural left outer join Invoice
group by CustomerID;

#e highest price album
select Title
from Album natural join Track
group by AlbumID
having sum(UnitPrice) >= all (select sum(UnitPrice) from Album natural join Track group by AlbumID);

#f

-- select Title
-- from Album natural join Track
-- where exists
-- 		(select AlbumID, Name
-- 		from Track left join InvoiceLine
-- 		on Track.TrackId = InvoiceLine.TrackId)
-- 			except
-- 		(select AlbumID, Name
-- 		from Track right join InvoiceLine
-- 		on Track.TrackId = InvoiceLine.TrackId));
        
select distinct(Title)
from Album natural join Track
where TrackId not in (select TrackId from InvoiceLine)
order by Title;

#g
create view CustomerInvoices as 
	select FirstName, LastName, sum(Total) as spent
	from Customer natural left outer join Invoice
	group by CustomerID;
select * 
from CustomerInvoices;

