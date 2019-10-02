import java.io.*;
import java.util.*;

public class StaubWaldenbergAssignment1 {

    //lists to store data about stock market days
    static String currentCompany;
    static ArrayList<String[]> crazyDays;
    static ArrayList<String[]> splits;
    static int totalCrazyDays;
    static String craziestDay;
    static double craziestDayPercent;
    static int totalSplits;

    //constructs lists, etc. to store stats
    static void initStats(){
        currentCompany = null;
        crazyDays = new ArrayList<String[]>();
        splits = new ArrayList<String[]>();
        totalCrazyDays = 0;
        craziestDay = null;
        craziestDayPercent = 0;
        totalSplits = 0;
    }

    //clears stats to prepare for company switch
    static void clearStats(){
        currentCompany = null;
        crazyDays.clear();
        splits.clear();
        totalCrazyDays = 0;
        craziestDay = null;
        craziestDayPercent = 0;
        totalSplits = 0;
    }

    //reads line of file
    static String readline(String string1, String filepath)throws Exception {
        File file1 = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file1));
        if(br.ready())
            return br.readLine();
        return null;
    }

    //calculates percent difference between high and low price
    static double calcPercentChange(String[] day){
        double high = Double.parseDouble(day[3]);
        double low = Double.parseDouble(day[4]);
        return (high-low)/high*100;
    }



    //checks if a day is crazy and updates stats accordingly
    static void crazyDayCheck(String[] day){
        if(calcPercentChange(day) >= 15.00){
            crazyDays.add(day);
            totalCrazyDays ++;
            //update craziest day
            if(calcPercentChange(day) > craziestDayPercent) {
                craziestDayPercent = calcPercentChange(day);
                craziestDay = day[1];
            }
        }
    }

    //adds current split to stats with splitype specified
    static void splitAdd(String[] day1, String[] day2, String splitType){
        String[] split = {splitType, day2[1], day2[5], day1[2]};
        splits.add(split);
        totalSplits++;
    }

    //checks for 3 types of splits
    static void splitCheck(String[] day1, String[] day2){
        double splitFraction = Math.abs(Double.parseDouble(day2[5]) / Double.parseDouble(day1[2]));
        //2:1 split
        if(Math.abs(splitFraction - 2.0) < .05)
            splitAdd(day1, day2, "2:1");
        //3:1 split
        if(Math.abs(splitFraction - 3.0) < .05)
            splitAdd(day1, day2, "3:1");
        //3:2 split
        if(Math.abs(splitFraction - 1.5) < .05)
            splitAdd(day1, day2, "3:2");
    }

    //rounds double to two decimal places
    static double roundTwoD(double num){
        return Math.floor(num * 100) / 100;
    }

    //prints all stats for current company
    static void printStats(){

        if(!crazyDays.isEmpty()) {
            for (String[] day : crazyDays)
                System.out.println("Crazy day: " + day[1] + "\t" + roundTwoD(calcPercentChange(day)));
        }
        System.out.println("Total crazy days = " + totalCrazyDays);
        if(!crazyDays.isEmpty())
            System.out.println("The craziest day:\t" + craziestDay + "\t" + roundTwoD(craziestDayPercent));
        System.out.println();
        if(!splits.isEmpty()) {
            for (String[] day : splits)
                System.out.println(day[0] + " split on: " + day[1] + "\t " + day[2] + " --> " + day[3]);
        }
        System.out.println("Total number of splits: " + totalSplits);
        System.out.println("\n");
    }




    public static void main(String[] args)throws Exception
    {

        //set standard out to write to file
        PrintStream tofile = new PrintStream(new File("output.txt"));
        PrintStream console = System.out;
        System.setOut(tofile);



        //setup to read file
        //pathname may change for grading? change path before turning in
        File file1 = new File("Stockmarket-1990-2015.txt");
        BufferedReader br = new BufferedReader(new FileReader(file1));


        //read day 1
        String[] day1 = br.readLine().split("\t");
        //init stat storage for first company
        initStats();
        currentCompany = day1[0];
        //read day 2
        String[] day2 = br.readLine().split("\t");
        System.out.println("Processing " + currentCompany + "\n==================");


        while(day1 != null)
        {
            //if end of file has been reached
            if(day2 == null){
                crazyDayCheck(day1);
                printStats();
                clearStats();
            //if new company has been reached
            }else if(!day2[0].equals(currentCompany)) {
                crazyDayCheck(day1);
                printStats();
                clearStats();
                currentCompany = day2[0];
                System.out.println("Processing " + currentCompany + "\n==================");
            //general case
            }else{
                crazyDayCheck(day1);
                splitCheck(day1, day2);
            }

            //increment day 1
            day1 = day2;
            //increment day 2
            //error handling for end of file
            if(br.ready())
                day2 = br.readLine().split("\t");
            else
                day2 = null;
        }
    }
}
