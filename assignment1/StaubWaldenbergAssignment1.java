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


    static void initStats(){
        currentCompany = null;
        crazyDays = new ArrayList<String[]>();
        splits = new ArrayList<String[]>();
        totalCrazyDays = 0;
        craziestDay = null;
        craziestDayPercent = 0;
        totalSplits = 0;
    }

    static void clearStats(){
        currentCompany = null;
        crazyDays.clear();
        splits.clear();
        totalCrazyDays = 0;
        craziestDay = null;
        craziestDayPercent = 0;
        totalSplits = 0;
    }


    static String readline(String string1, String filepath)throws Exception {
        File file1 = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file1));
        if(br.ready())
            return br.readLine();
        return null;
    }

    static double calcPercentChange(String[] day){
        double high = Double.parseDouble(day[3]);
        double low = Double.parseDouble(day[4]);
        return (high-low)/high*100;
    }

    static void printStats(){

        if(!crazyDays.isEmpty()) {
            for (String[] day : crazyDays)
                System.out.println("Crazy day:\t" + day[1] + "\t" + calcPercentChange(day));
        }
        System.out.println("Total crazy days = " + totalCrazyDays);
        if(!crazyDays.isEmpty())
            System.out.println("The craziest day:\t" + craziestDay + "\t" + craziestDayPercent);


    }







    public static void main(String[] args)throws Exception
    {

        //set standard out to write to file
        /*
        PrintStream tofile = new PrintStream(new File("output.txt"));
        PrintStream console = System.out;
        System.setOut(tofile);
        */


        //setup to read file
        //pathname may change for grading? change path before turning in
        File file1 = new File("Stockmarket-1990-2015.txt");
        BufferedReader br = new BufferedReader(new FileReader(file1));


        //read day 1
        String[] day1 = br.readLine().split("\t");

        //init storage for first company
        initStats();
        currentCompany = day1[0];

        //read day 2
        String[] day2 = br.readLine().split("\t");



        System.out.println("Processing " + currentCompany + "\n==================");


        while(br.ready())
        {

            //day1 company equals day 2 company
            if(day2[0].equals(currentCompany)) {

                //crazy day check
                if(calcPercentChange(day1) >= 15.00){
                    crazyDays.add(day1);
                    totalCrazyDays ++;
                    //update craziest day
                    if(calcPercentChange(day1) > craziestDayPercent) {
                        craziestDayPercent = calcPercentChange(day1);
                        craziestDay = day1[1];
                    }
                }
            }
            else{
                //print stats for current company
                printStats();

                System.out.print("\n");

                //switch company
                clearStats();
                currentCompany = day2[0];

                System.out.println("Processing " + currentCompany + "\n==================");

            }


            //increment day 1
            day1 = day2;
            //increment day 2
            try {
                day2 = br.readLine().split("\t");
            }catch(NullPointerException e){}

        }






    }





}
