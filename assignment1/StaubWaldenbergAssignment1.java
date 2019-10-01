import java.io.*;
import java.util.*;

public class StaubWaldenbergAssignment1 {


    static class Company{
        public String name;
        public ArrayList<String[]> crazyDays;
        public ArrayList<String[]> splits;
        public int totalCrazyDays;
        public int totalSplits;

        //constructor for company object
        Company(String s){
            this.name = s;
            this.crazyDays = new ArrayList<String[]>();
            this.splits = new ArrayList<String[]>();
            this.totalCrazyDays = 0;
            this.totalSplits = 0;
        }

        String processing(){
            return "Processing " + this.name + "\n==================";
        }
    }

    public static String readline(String string1, String filepath)throws Exception {
        File file1 = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file1));
        if(br.ready())
            return br.readLine();
        return null;
    }

    public static double calcPercentChange(String[] day){
        double high = Double.parseDouble(day[3]);
        double low = Double.parseDouble(day[4]);
        return (high-low)/high*100;
    }

    public static void printCrazyDay(String[] day){
        System.out.println("Crazy day:\t" + day[1] + "\t" + calcPercentChange(day));
    }





    public static void main(String[] args)throws Exception
    {

        //set standard out to write to file

        PrintStream tofile = new PrintStream(new File("output.txt"));
        PrintStream console = System.out;
        System.setOut(tofile);


        //setup to read file
        //pathname may change for grading? change path before turning in
        File file1 = new File("/home/staubwb/IdeaProjects/main/school/cs330/assignment1/Stockmarket-1990-2015.txt");
        BufferedReader br = new BufferedReader(new FileReader(file1));


        //read day 1
        String[] day1 = br.readLine().split("\t");


        //create struct for first company
        Company company1 = new Company(day1[0]);


        System.out.println("Processing " + company1.name + "\n==================");







        while(br.ready())
        {
            //read day 2
            String[] day2 = br.readLine().split("\t");

            //day1 company equals day 2 company
            if(day2[0].equals(day1[0])) {


                //crazy day check
                if(calcPercentChange(day1) >= 15.00){
                    company1.crazyDays.add(day1);

                }
            }
            else{

                //print crazy days for current company
                for(String[] day : company1.crazyDays)
                    printCrazyDay(day);

                System.out.print("\n");

                //switch company
                company1 = new Company(day2[0]);

                System.out.println("Processing " + company1.name + "\n==================");

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
