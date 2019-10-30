import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class ass2 {

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

            showCompanies();

            // Enter Ticker and TransDate, Fetch data for that ticker and date
            Scanner in = new Scanner(System.in);
            while (true) {
                System.out.print("Enter ticker and date (YYYY.MM.DD): ");
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

    static void showCompanies() throws SQLException {
        // Create and execute a query
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("select Ticker, Name from Company");

        // Show results
        while (results.next()) {
            System.out.printf("%5s %s%n", results.getString("Ticker"), results.getString("Name"));
        }
        stmt.close();
    }

    static void showCompany(String[] args) throws SQLException {
        // Create and execute a query
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("select Ticker, Name from Company");

        // Show results
        if (results.next()) {
            System.out.printf("%5s %s%n", results.getString("Ticker"), results.getString("Name"));
            showTickerDays(args);
        }
        else
            System.out.printf("not in database, ticker not found: %s%n", args[0]);
        stmt.close();
    }

    static void showTickerDays(String[] args) throws SQLException {
        // Prepare query
        //if no date constraints are given
        PreparedStatement pstmt;

        if(args.length == 1){
            pstmt = conn.prepareStatement(
                    "* " +
                            "  from PriceVolume " +
                            "  where Ticker = ? order by TransDate DESC");
            pstmt.setString(1, args[0]);

            ResultSet rs = pstmt.executeQuery();
            getDays(rs);
            pstmt.close();
        }
        //if just a start date is given
        else if(args.length == 2) {
            pstmt = conn.prepareStatement(
                    "select TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice " +
                            "  from PriceVolume " +
                            "  where Ticker = ? and TransDate >= ? order by TransDate DESC");
            pstmt.setString(1, args[0]);
            pstmt.setString(2, args[1]);

            ResultSet rs = pstmt.executeQuery();
            getDays(rs);
            pstmt.close();
        }
        else if(args.length == 3)  {
            pstmt = conn.prepareStatement(
                    "select TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice " +
                            "  from PriceVolume " +
                            "  where Ticker = ? and TransDate => ? and TransDate <= ? order by TransDate DESC");
            pstmt.setString(1, args[0]);
            pstmt.setString(2, args[1]);
            pstmt.setString(3, args[2]);

            ResultSet rs = pstmt.executeQuery();
            getDays(rs);

            pstmt.close();
        }
        else
            System.out.println("invalid number of args, must insert a ticker followed by optional 1 or 2 date constraints");

    }
    static void getDays(ResultSet rs) throws SQLException {

        while (rs.next()) {
            System.out.printf("%s Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f%n", rs.getString("TransDate"),
                    rs.getDouble("OpenPrice"), rs.getDouble("HighPrice"), rs.getDouble("LowPrice"), rs.getDouble("ClosePrice"));
        }
    }
}

