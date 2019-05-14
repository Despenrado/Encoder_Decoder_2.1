import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UI{


    ExecutorService executorService = Executors.newFixedThreadPool(1);
    ExecutorService executorService2 = Executors.newFixedThreadPool(1);

    LinkedList<ArrayList<Boolean>> signal;
    private int capacity;
    private static int times = 0;
    private static int time = 0;
    private static boolean aBoolean = false;
    private static int sign_lenght = 1;
    protected static int pollute = 1;
    private String cin;

    UI(){
    }

    public void mainLoop(){
        signal = new LinkedList<ArrayList<Boolean>>();
        System.out.print("$ ");
        Scanner scanner = new Scanner(System.in);
        cin = scanner.nextLine();
        String []retval = cin.split(" ");

        switch (retval[0]){
            case"h":
            case"help": {
                help();
                break;
            }
            case"exit":{
                System.exit(0);
                break;
            }
            case "s":{
                ScanResults.print(ScanResults.getStatistic(retval[1]));
                break;
            }
            case "t":
            case "test":{
                testMenu(retval);
                break;
            }
            default:{
                System.out.println("    h or help for help");
                break;
            }
        }





        mainLoop();
    }

    public void help(){
        System.out.println("HELP:\n" +
                "[command] [quantity] [capacity] [option] [option2] [pollute]\n" +
                "\n" +
                "command:\n" +
                "t of test -- test\n" +
                "s -- get statistic and after space enter name of file\n" +
                "h or help -- help\n" +
                "exit -- exit from application\n" +
                "\n" +
                "quantity: - how many times test 1 signal (integer)\n" +
                "\n" +
                "capacity - split signal by bits\n" +
                "\n" +
                "option:\n" +
                "-r - generate random signal of option2 bits\n" +
                "-f - read from file. Name of file = option 2\n" +
                "-s - use bites from option2 to generating signal\n" +
                "\n" +
                "option2n:" +
                "if you use -r, enter how many bites will be in signal\n" +
                "if you use -f, enter name of file which have signal\n" +
                "if you use -s, enter your signal(example: 1010)\n" +
                "\n" +
                "pollute:\n" +
                "if you write int here, it's value will be use for probability of contamination of each byte of the signal. Default = 1(if you nothing to write)");
    }

    public void testMenu(String[] retval){
        try {
            times = Integer.parseInt(retval[1]);
            this.capacity = Integer.parseInt(retval[2]);
        }
        catch (Exception e){ return;}

        try {
            if (retval[5] != null) {
                pollute = Integer.parseInt(retval[5]);
            }
        }catch (Exception e){}

        switch (retval[3]){
            case "-r":{
                System.out.print("generating random signal... ");
                if(retval[4] == null){return;}
                genRandomSignal(Integer.parseInt(retval[4]));
                UI.printToFile("Random_Signal.txt", this.signal);
                sign_lenght = signal.size();
                System.out.println("OK");
                startTest();
                break;
            }
            case "-f":{
                System.out.print("read from file... ");
                if(retval[4] == null){return;}
                signal = readFromFile(retval[4], capacity);
                sign_lenght = signal.size();
                startTest();
                break;
            }
            case "-s":{
                System.out.print("read from console... ");
                ToBoolean(retval[4], capacity);
                sign_lenght = signal.size();
                startTest();
                break;
            }
            case "-str":{               // in progress
                System.out.print("read from console... ");
                signal = UI.wordToBoolean(retval[4], capacity);
                sign_lenght = signal.size();
                Iterator<ArrayList<Boolean>> iter = signal.iterator();
                while (iter.hasNext()) {
                    TripleEncoder.ptintToConsole(iter.next());
                }
                break;
            }
            default:{ return;}
        }
        ScanResults.print(ScanResults.getStatistic("Results_Humming.txt"));
        ScanResults.print(ScanResults.getStatistic("Results_Triple.txt"));

    }


    public void startTest(){
        Test.fileNewTest(cin);
        TestTriple.fileNewTest(cin);
        System.out.println("|                                               Testing:                                           |");
        for (int i = 0; i < times; i++) {
            Iterator<ArrayList<Boolean>> iter = signal.iterator();
            while (iter.hasNext()){
                ArrayList<Boolean> tmp = iter.next();
                executorService.submit(new TestTriple(tmp, capacity));
                executorService2.submit(new Test(tmp, capacity));
            }
        }
        aBoolean = false;
        while (!aBoolean){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void genRandomSignal(int n){
        SecureRandom secureRandom = new SecureRandom();
        ArrayList<Boolean> tmp = new ArrayList<Boolean>();
        for (int i = 0; i < n; i++) {
            tmp.add(secureRandom.nextBoolean());
            if(tmp.size() == capacity){
                signal.add(new ArrayList<Boolean>(tmp));
                tmp = new ArrayList<Boolean>();
            }
        }
        if(tmp.size() != 0){
            signal.add(new ArrayList<Boolean>(tmp));
            tmp = new ArrayList<Boolean>();
        }
    }

    public static LinkedList<ArrayList<Boolean>> readFromFile(String fileName, int cap){
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))){
            String string = bufferedReader.readLine();
            String []retval = string.split(" ");
            LinkedList<ArrayList<Boolean>> sign = new LinkedList<ArrayList<Boolean>>();
            ArrayList<Boolean> bool = new ArrayList<Boolean>();
            for (int i = 0; i < retval.length; i++) {
                bool.add(Boolean.parseBoolean(retval[i]));
                if(bool.size() == cap){
                    sign.add(new ArrayList<Boolean>(bool));
                    bool = new ArrayList<Boolean>();
                }
            }
            if(bool.size() != 0){
                sign.add(new ArrayList<Boolean>(bool));
                bool = new ArrayList<Boolean>();
            }
            System.out.println("OK");
            return sign;

        }catch (IOException e){}
        System.out.println("error");
        return  null;
    }

    public static void printToFile(String fileName, LinkedList<ArrayList<Boolean>> tmp){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName, false), "UTF-8")))){
            Iterator<ArrayList<Boolean>> iter1 = tmp.iterator();
            while (iter1.hasNext()){
                Iterator<Boolean> iter2 = iter1.next().iterator();
                while (iter2.hasNext()){
                    bufferedWriter.write(Boolean.toString(iter2.next()) + " ");
                }
            }
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e){
            System.out.println("fatal error: print to file is failed");
        }
    }

    public void ToBoolean(String s, int cap){
        try{
            capacity = cap;
            ArrayList<Boolean> arrayList = new ArrayList<Boolean>();
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '1') {
                    arrayList.add(true);
                }
                if (s.charAt(i) == '0') {
                    arrayList.add(false);
                }
                if (arrayList.size() == cap) {
                    signal.add(new ArrayList<Boolean>(arrayList));
                    arrayList = new ArrayList<Boolean>();
                }
            }
            System.out.println("OK");
        }catch (Exception e){return;}

    }


    public static synchronized void loading(){
        time++;
        if(times >= 100){
            if(time % (times*sign_lenght /50) == 0){
                System.out.print("#");
            }
        }else{
            System.out.print("#");
        }
        if((time/2) == times*sign_lenght){
            System.out.println();
            System.out.println("completed");
            time = 0;
            aBoolean = true;
        }
    }

    public static LinkedList<ArrayList<Boolean>> wordToBoolean(String s, int capacity){
        try {
            LinkedList<ArrayList<Boolean>> tmp = new LinkedList<ArrayList<Boolean>>();
            ArrayList<Boolean> booleans = new ArrayList<Boolean>();
            char[] c = s.toCharArray();
            for (int i = 0; i < c.length; i++) {
                int a = (int) c[i];
                if (a - 128 > 0) {
                    a -= 128;
                    booleans.add(true);
                } else {
                    booleans.add(false);
                }
                if (a - 64 > 0) {
                    a -= 64;
                    booleans.add(true);
                } else {
                    booleans.add(false);
                }
                if (a - 32 > 0) {
                    a -= 32;
                    booleans.add(true);
                } else {
                    booleans.add(false);
                }
                if (a - 16 > 0) {
                    a -= 16;
                    booleans.add(true);
                } else {
                    booleans.add(false);
                }
                if (a - 8 > 0) {
                    a -= 8;
                    booleans.add(true);
                } else {
                    booleans.add(false);
                }
                if (a - 4 > 0) {
                    a -= 4;
                    booleans.add(true);
                } else {
                    booleans.add(false);
                }
                if (a - 2 > 0) {
                    a -= 2;
                    booleans.add(true);
                } else {
                    booleans.add(false);
                }
                if (a - 1 > 0) {
                    a -= 1;
                    booleans.add(true);
                } else {
                    booleans.add(false);
                }
                if (capacity == booleans.size()) {
                    tmp.add(booleans);
                    booleans = new ArrayList<Boolean>();
                }
            }
            System.out.println("OK");
            return tmp;
        }catch (Exception e){return null;}
    }

    public static void main(String[] args) {

        UI ui = new UI();
        System.out.println("    h or help for help");
        ui.mainLoop();
        System.exit(0);
    }
}

