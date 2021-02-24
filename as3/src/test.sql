use staubwb

select *
from Performance
where Industry = 'Materials'
order by StartDate,
Ticker limit 50;

select *
from Performance
where Industry = 'Materials'
and StartDate = '2009.08.21'
order by StartDate,
Ticker limit 50; 


select *
from Performance
order by TickerReturn - IndustryReturn desc
limit 200;





use johnson330


select *
from Company natural join PriceVolume
where Industry = 'Materials'
and Ticker = 'TIE'
and (case
		when 
	end)
order by TransDate desc;


