package vitaly;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.print("Enter text : ");
        FileReader fileReader = new FileReader("src/main/resources/airports.csv");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        Scanner LookScanner = new Scanner(System.in);
        String lineFromFile;
        String lookingText = LookScanner.nextLine();
        while(!lookingText.equals("!quit")){
            lineFromFile = bufferedReader.readLine();

            while(lineFromFile!=null){
                System.out.println(lineFromFile);

                lineFromFile = bufferedReader.readLine();
            }
            bufferedReader.reset();
            lookingText = LookScanner.nextLine();
        }
        bufferedReader.close();
        fileReader.close();

    }
}