import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;
import java.sql.*;

class Staub-Waldenberg-Assignment2{
static connection conn = null;

    public static void main(String[] args) throws Exception{

        String paramsFile = "ConnectionParameters.txt";
        if(args.length >= 1){
            paramsFile = args[0];
        }
        Properties connectprops = new Properties();
        connectprops.load(new FileInputStream(paramsFile));

    }

}