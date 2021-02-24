import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.io.*;
import java.sql.PreparedStatement;

class as3 {
    static final String defaultReaderParams = "readerparams.txt";
    static final String defaultWriterParams = "writerparams.txt";
    static Connection readerConn = null;
    static Connection writerConn = null;
    static PreparedStatement getDates;
    static PreparedStatement getTickerDates;
    static PreparedStatement getIndustryPriceData;

    static final String getDatesQuery =
            "select max(startDate), min(endDate)" +
                    "  from (select Ticker, min(TransDate) as StartDate, max(TransDate) as endDate," +
                    "            count(distinct TransDate) as tradingDays" +
                    "          from Company natural join PriceVolume" +
                    "          where Industry = ?" +
                    "          group by Ticker" +
                    "          having tradingDays >= ?) as TickerDates";

    static final String getTickerDatesQuery =
            "select Ticker, min(TransDate) as StartDate, max(TransDate) as endDate," +
                    "      count(distinct TransDate) as tradingDays" +
                    "  from Company natural join PriceVolume" +
                    "  where Industry = ?" +
                    "    and TransDate >= ? and TransDate <= ?" +
                    "  group by Ticker" +
                    "  having tradingDays >= ?" +
                    "  order by Ticker";

    static final String getIndustryPriceDataQuery =
            "select Ticker, TransDate, OpenPrice, ClosePrice" +
                    "  from PriceVolume natural join Company" +
                    "  where Industry = ?" +
                    "    and TransDate >= ? and TransDate <= ?" +
                    "  order by TransDate, Ticker";

    static final String getAllIndustries =
            "select distinct Industry" +
                    "  from Company" +
                    "  order by Industry";

    static final String dropPerformanceTable =
            "drop table if exists Performance;";

    static final String createPerformanceTable =
            "create table Performance (" +
                    "  Industry char(30)," +
                    "  Ticker char(6)," +
                    "  StartDate char(10)," +
                    "  EndDate char(10)," +
                    "  TickerReturn char(12)," +
                    "  IndustryReturn char(12)" +
                    "  );";

    static final String insertPerformance =
            "insert into Performance(Industry, Ticker, StartDate, EndDate, TickerReturn, IndustryReturn)" +
                    "  values(?, ?, ?, ?, ?, ?);";


    static class IndustryData {
        List<ticker> tickers;
        int numDays;
        String startDate;
        String endDate;
        IndustryData(List<ticker> tickers, int numDays, String startDate, String endDate){
            this.tickers = tickers;
            this.numDays = numDays;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
    static class ticker {
        String ticker;
        List<interval> intervals;
        ticker(String ticker){
            this.ticker = ticker;
            this.intervals = new ArrayList<>();
        }
    }
    static class interval {
        day start;
        day end;
        interval(day start, day end){
            this.start = start;
            this.end = end;
        }
    }


    public static void main(String[] args) throws Exception {
        // Get connection properties
        Properties readerProps = new Properties();
        readerProps.load(new FileInputStream(defaultReaderParams));
        Properties writerProps = new Properties();
        writerProps.load(new FileInputStream(defaultWriterParams));

        try {
            setupReader(readerProps);
            setupWriter(writerProps);

            List<String> industries = getIndustries();
            System.out.printf("%d industries found%n", industries.size());
            for (String industry : industries) {
                System.out.printf("%s%n", industry);
            }
            System.out.println();

            for (String industry : industries) {
                System.out.printf("Processing %s%n", industry);
                IndustryData iData = processIndustry(industry);

                //all tickers in current industry
                if (iData != null && iData.tickers.size() > 2) {
                    System.out.printf("%d accepted tickers for %s(%s - %s), %d common dates%n",
                            iData.tickers.size(), industry, iData.startDate, iData.endDate, iData.numDays);
                    processIndustryGains(industry, iData);
                } else
                    System.out.printf("Insufficient data for %s => no analysis%n", industry);
                System.out.println();
            }
            //Close everything you don't need any more
            readerConn.close();
            writerConn.close();


            System.out.println("Database connections closed");
        } catch (SQLException ex) {
            System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                    ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
        }
    }

    static void setupReader(Properties connectProps) throws SQLException {
        String dburl = connectProps.getProperty("dburl");
        String username = connectProps.getProperty("user");
        readerConn = DriverManager.getConnection(dburl, connectProps);
        System.out.printf("Reader connection %s %s established.%n", dburl, username);
        getDates = readerConn.prepareStatement(getDatesQuery);
        getTickerDates = readerConn.prepareStatement(getTickerDatesQuery);
        getIndustryPriceData = readerConn.prepareStatement(getIndustryPriceDataQuery);
    }


    static void setupWriter(Properties connectProps) throws SQLException {
        String dburl = connectProps.getProperty("dburl");
        String username = connectProps.getProperty("user");
        writerConn = DriverManager.getConnection(dburl, connectProps);
        System.out.printf("Writer connection %s %s established.%n", dburl, username);
        Statement tstmt = writerConn.createStatement();
        tstmt.execute(dropPerformanceTable);
        tstmt.execute(createPerformanceTable);
        tstmt.close();
    }

    // get names of all industries
    static List<String> getIndustries() throws SQLException {
        List<String> result = new ArrayList<>();
        Statement stmt = readerConn.prepareStatement(getAllIndustries);
        ResultSet rs = stmt.executeQuery(getAllIndustries);
        while (rs.next()) {
            result.add(rs.getString("Industry"));
        }
        return result;
    }

    static IndustryData processIndustry(String industry) throws SQLException {
        getDates.setString(1, industry);
        getDates.setString(2, "150");
        ResultSet dateRange = getDates.executeQuery();
        dateRange.first();
        String start = dateRange.getString("max(startDate)");
        String end = dateRange.getString("min(endDate)");
        getTickerDates.setString(1, industry);
        getTickerDates.setString(2, start);
        getTickerDates.setString(3, end);
        getTickerDates.setString(4, "150");
        ResultSet ticks = getTickerDates.executeQuery();
        List<ticker> tickers = new ArrayList<>();
        int count = 0;
        int numDays = 0;
        while(ticks.next()){
            if(count == 0) {
                numDays = ticks.getInt("tradingDays");
            }
            count++;
            tickers.add(new ticker(ticks.getString("Ticker")));
            if(ticks.getInt("tradingDays") < numDays){
                numDays = ticks.getInt("tradingDays");
            }
        }
        return new IndustryData(tickers, numDays, start, end);
    }

    // calculates tickerreturn and industryreturn for every interval in every ticker in the current industry
    static void processIndustryGains(String industry, IndustryData data) throws SQLException {
        int num_of_intervals = data.numDays / 60;
        PreparedStatement firstCompany = readerConn.prepareStatement("select * from PriceVolume natural join Company " +
                "where Industry = ? " +
                "and TransDate between ?" +
                " and ? and " +
                "Ticker = ? order " +
                "by TransDate");
        firstCompany.setString(1, industry);
        firstCompany.setString(2, data.startDate);
        firstCompany.setString(3, data.endDate);
        firstCompany.setString(4, data.tickers.get(0).ticker);
        ResultSet r = firstCompany.executeQuery();

        List<interval> intervals = new ArrayList<>();
        int interval_count = 0;
        int day_count = 1;
        day start = null;
        while(r.next() && (interval_count < num_of_intervals)){
            if(day_count % 60 == 1){
                start = new day(r.getString("TransDate"), r.getDouble("OpenPrice"), r.getDouble("OpenPrice"));
            }
            if(day_count % 60 == 0){
                day end = new day(r.getString("TransDate"), r.getDouble("OpenPrice"), r.getDouble("OpenPrice"));
                intervals.add(new interval(start, end));
                interval_count++;
            }
            day_count++;
        }
        populate_intervals(industry, data, intervals);
        for(int i = 0; i < data.tickers.size(); i++){
            for(int j = 0; j < num_of_intervals; j++){
                String ticker = data.tickers.get(i).ticker;
                day startday = data.tickers.get(i).intervals.get(j).start;
                String startdate = startday.date;
                day endday = data.tickers.get(i).intervals.get(j).end;
                String enddate = endday.date;
                double tickerreturn = (endday.close/startday.open)-1;

                double industryreturn = 0;
                String company = null;
                for(int k = 0; k < data.tickers.size(); k++){
                    if(k != i){
                        day sday = data.tickers.get(k).intervals.get(j).start;
                        day eday = data.tickers.get(k).intervals.get(j).end;

                        double add = (eday.close/sday.open);
                        industryreturn += add;
                    }
                }
                industryreturn /= (data.tickers.size()-1);
                industryreturn--;

                PreparedStatement insertPerformanceData = writerConn.prepareStatement(insertPerformance);
                insertPerformanceData.setString(1, industry);
                insertPerformanceData.setString(2, ticker);
                insertPerformanceData.setString(3, startdate);
                insertPerformanceData.setString(4, enddate);
                insertPerformanceData.setString(5, String.format("%10.7f", tickerreturn));
                insertPerformanceData.setString(6, String.format("%10.7f", industryreturn));
                int result = insertPerformanceData.executeUpdate();
            }
        }
    }

    // populates close and open pricevolume data for every interval in every ticker in current industry
    // has a separate case to calculate the end date of the last interval
    static void populate_intervals(String industry, IndustryData data, List<interval> intervals) throws SQLException{
        for(int i = 0; i < data.tickers.size(); i++){

            Deque<day> adjustedDays = adjust_ticker(industry, data, data.tickers.get(i).ticker);

            for(int j = 0; j < intervals.size(); j++) {
                day startd = null;
                Iterator iter = adjustedDays.iterator();
                while (iter.hasNext()) {
                    day d = (day) iter.next();
                    String interval_start = intervals.get(j).start.date;
                    int date_compare = d.date.compareTo(interval_start);
                    if (date_compare == 0 || date_compare > 0) {
                        startd = d;
                        break;
                    }
                }
                iter = adjustedDays.descendingIterator();
                while(iter.hasNext()){
                    day end = (day) iter.next();
                    if(j < intervals.size()-1){
                        String interval_end = intervals.get(j+1).start.date;
                        int date_compare = end.date.compareTo(interval_end);
                        if(date_compare < 0) {
                            data.tickers.get(i).intervals.add(new interval(startd, end));
                            break;
                        }
                    }
                    else{
                        String interval_end = intervals.get(j).end.date;
                        int date_compare = end.date.compareTo(interval_end);
                        if(date_compare == 0 || date_compare < 0) {
                            data.tickers.get(i).intervals.add(new interval(startd, end));
                            break;
                        }
                    }
                }
            }
        }
    }

    // adjusts pricevolume data for a given ticker based on stock splits
    static Deque<day> adjust_ticker(String industry, IndustryData data, String ticker) throws SQLException{
        PreparedStatement company = readerConn.prepareStatement("select * from PriceVolume natural join Company " +
                "where Industry = ? " +
                "and TransDate between ?" +
                " and ? and " +
                "Ticker = ? order " +
                "by TransDate desc");
        company.setString(1, industry);
        company.setString(2, data.startDate);
        company.setString(3, data.endDate);
        company.setString(4, ticker);
        ResultSet r = company.executeQuery();

        Deque<day> adjustedDays = new LinkedList<>();
        double splitDenom = 1;
        double newerDayOpen = 0;
        while(r.next()) {
            if (newerDayOpen != 0) {
                double ratio = r.getDouble("ClosePrice") / newerDayOpen;
                if (Math.abs(ratio - 2.0) < .20)
                    splitDenom *= 2;
                if (Math.abs(ratio - 3.0) < .30)
                    splitDenom *= 3;
                if (Math.abs(ratio - 1.5) < .15)
                    splitDenom *= 1.5;
            }
            adjustedDays.addFirst(new day(r.getString("TransDate"),
                    r.getDouble("OpenPrice") / splitDenom,
                    r.getDouble("ClosePrice") / splitDenom));
            newerDayOpen = r.getDouble("OpenPrice");
        }
        return adjustedDays;
    }
}