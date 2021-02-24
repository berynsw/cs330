import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

class as2 {

    //this code for connecting to the database was modified from the demo.java file
    static Connection conn = null;
    public static void main(String[] args) throws Exception {
        // Get connection properties
        String paramsFile = "ConnectionParameters.txt";
        if (args.length >= 1) {
            paramsFile = args[0];
        }
        Properties connectprops = new Properties();
        connectprops.load(new FileInputStream(paramsFile));

        try {
            // Get connection
            Class.forName("com.mysql.jdbc.Driver");
            String dburl = connectprops.getProperty("dburl");
            String username = connectprops.getProperty("user");
            conn = DriverManager.getConnection(dburl, connectprops);
            System.out.printf("Database connection %s %s established.%n", dburl, username);

            // Enter Ticker and TransDate, Fetch data for that ticker and date
            Scanner in = new Scanner(System.in);
            while (true) {
                System.out.print("Enter ticker and [start , end dates]: ");
                String[] data = in.nextLine().trim().split("\\s+");
                if (data.length < 1)
                    break;
                else
                    showCompany(data);
            }
            conn.close();

        } catch (SQLException ex) {
            System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                    ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
        }
    }

    //executes a query to display requested company name and ticker
    //calls showTickerDays to execute query for stock days requested
    static void showCompany(String[] data) throws SQLException {
        //show requested company name
        PreparedStatement stmt = conn.prepareStatement(
                "select Ticker, Name " +
                        "  from Company " +
                        "  where Ticker = ?");
        stmt.setString(1, data[0]);
        ResultSet results = stmt.executeQuery();

        //afterwards, call showTickerDays to get pricevolume data for that company
        if (results.next()) {
            System.out.printf("%s %n", results.getString("Name"));
            showTickerDays(data);
        }
        else
            System.out.printf("not in database, ticker not found: %s%n", data[0]);
        stmt.close();
    }

    //executes query to get stock days in descending order
    //calls splitAdjust to adjust for splits
    static void showTickerDays(String[] data) throws SQLException {
        //if no date constraints are given
        PreparedStatement pstmt;

        if(data.length == 1){
            pstmt = conn.prepareStatement(
                    "select * " +
                            "  from PriceVolume " +
                            "  where Ticker = ? order by TransDate DESC");
            pstmt.setString(1, data[0]);

            ResultSet rs = pstmt.executeQuery();
            splitAdjust(rs);
            pstmt.close();
        }
        //date constraints are given
        else if(data.length == 3)  {
            pstmt = conn.prepareStatement(
                    "select * " +
                            "  from PriceVolume " +
                            "  where Ticker = ? and TransDate between ? and ? " +
                            "order by TransDate DESC");
            pstmt.setString(1, data[0]);
            pstmt.setString(2, data[1]);
            pstmt.setString(3, data[2]);

            ResultSet rs = pstmt.executeQuery();
            splitAdjust(rs);
            pstmt.close();
        }
        else
            System.out.println("invalid number of args, must insert a ticker followed by optional 1 or 2 date constraints");
    }



    //checks each day in the resultSet for splits, adjusts price data accordingly
    //calls executeInvestment to buy and sell stocks
    static void splitAdjust(ResultSet rs) throws SQLException {
        Deque<day> adjustedDays = new LinkedList<day>();
        double splitDenom = 1;
        int splits = 0;
        int totalDays = 0;
        day newDay = null;
        //go through stock days in descending order to check for splits and adjust
        while (rs.next()) {
            //if we are not on the first day and should check for a stock split
            if (newDay != null) {
                double ratio = rs.getDouble("ClosePrice") / newDay.open;
                if (Math.abs(ratio - 2.0) < .20) {
                    splitDenom *= 2;
                    splits++;
                    System.out.printf("2:1 split on %s %.2f ---> %.2f%n", rs.getString("TransDate"), rs.getDouble("ClosePrice"), newDay.open);
                }
                if (Math.abs(ratio - 3.0) < .30) {
                    splitDenom *= 3;
                    splits++;
                    System.out.printf("3:1 split on %s %.2f ---> %.2f%n", rs.getString("TransDate"), rs.getDouble("ClosePrice"), newDay.open);
                }
                if (Math.abs(ratio - 1.5) < .15) {
                    splitDenom *= 1.5;
                    splits++;
                    System.out.printf("3:2 split on %s %.2f ---> %.2f%n", rs.getString("TransDate"), rs.getDouble("ClosePrice"), newDay.open);
                }
            }
            adjustedDays.addLast(new day(rs.getString("TransDate"),
                    rs.getDouble("OpenPrice") / splitDenom,
                    rs.getDouble("HighPrice") / splitDenom,
                    rs.getDouble("LowPrice") / splitDenom,
                    rs.getDouble("ClosePrice") / splitDenom));
            day dayCopy = new day(rs.getString("TransDate"),
                    rs.getDouble("OpenPrice"),
                    rs.getDouble("HighPrice"),
                    rs.getDouble("LowPrice"),
                    rs.getDouble("ClosePrice"));
            newDay = dayCopy;
            totalDays++;
        }
        System.out.println(splits + " splits in " + totalDays + " trading days");
        executeInvestment(adjustedDays);
    }


    //calculates average of closing price values in the sum deque
    static double movingAvg(Deque q){
        Iterator dI = q.iterator();
        double sum = 0;
        int count = 0;
        while(count < q.size()){
            day add = (day)dI.next();
            sum += add.close;
            count++;
        }
        return sum / 50;
    }

    //buys and sells stock shares based on adjusted day data
    static void executeInvestment(Deque<day> deque){
        double netGain = 0;
        double shares = 0;
        int transactions = 0;
        int count = 0;
        double mAvg = 0;

        //fiftyDayWindow is used to calculate the moving average
        Deque<day> fiftyDayWindow = new LinkedList<day>();
        //examines adjusted stock days
        while(deque.size() > 1){
            day curDay = deque.removeLast();
            //System.out.printf("date: %s prices open: %.2f high: %.2f low: %.2f close: %.2f average: %.2f%n " ,curDay.date, curDay.open, curDay.high, curDay.low, curDay.close, mAvg);

            //buy 100 shares at price open(d+1)
            if((curDay.close < mAvg) && (curDay.close / curDay.open <= .970000001)){
                netGain -= ((100 * deque.peekLast().open) + 8);
                shares += 100;
                transactions++;
            }
            //sell 100 shares at price open(d) + close(d) / 2
            else if((shares >= 100) && (curDay.open > mAvg) && (curDay.open / fiftyDayWindow.peekLast().close >= 1.00999999)){
                netGain += ((100 * (curDay.open + curDay.close) / 2) - 8);
                shares -= 100;
                transactions++;
            }

            fiftyDayWindow.addLast(curDay);
            count++;
            //calculates average, then moves it
            if(count >= 50) {
                mAvg = movingAvg(fiftyDayWindow);
                fiftyDayWindow.removeFirst();
            }
        }
        //sells remaining shares
        if(shares > 0){
            netGain += (deque.peekLast().open * shares);
            transactions++;
        }
        System.out.printf("%nExecuting investment strategy%n");
        System.out.printf("Transactions executed: %d%n",transactions);
        System.out.printf("Net cash: %.2f%n%n",netGain);
    }
}


//INTC 1980.01.01 1999.12.31
