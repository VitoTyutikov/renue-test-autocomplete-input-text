/*
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
}*/
package vitaly;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class AirportSearch {
    private static final String FILE_NAME = "src/main/resources/airports.csv";
    private static final int LINE_SIZE = 100;
    private static final int MAX_MEMORY_USAGE = 7 * 1024 * 1024;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java AirportSearch <column_number>");
            System.exit(1);
        }

        int columnNumber = Integer.parseInt(args[0]);
        if (columnNumber <= 0) {
            System.err.println("Column number should be positive");
            System.exit(1);
        }

        try (RandomAccessFile file = new RandomAccessFile(new File(FILE_NAME), "r")) {
            long fileSize = file.length();
            long memoryUsagePerLine = LINE_SIZE + 8; // overhead for storing line start position
            long maxLinesToRead = Math.min(fileSize / memoryUsagePerLine, MAX_MEMORY_USAGE / memoryUsagePerLine);

            // read file line by line and store starting position of each line
            List<Long> lineStartPositions = new ArrayList<>();
            long lineStartPosition = 0;
            while (lineStartPositions.size() < maxLinesToRead && lineStartPosition < fileSize) {
                lineStartPositions.add(lineStartPosition);
                file.seek(lineStartPosition);
                file.readLine();
                lineStartPosition = file.getFilePointer();
            }

            // search for matching lines
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter search text: ");
            String searchText = scanner.nextLine().trim();
            while (!searchText.equals("!quit")) {
                long startTime = System.currentTimeMillis();
                if (searchText.isEmpty()) {
                    break;
                }
                List<LineData> matchingLines = new ArrayList<>();
                for (long startPosition : lineStartPositions) {
                    String line = readLineFromFile(file, startPosition);
                    String[] fields = line.split(",") ;
                    if (fields[columnNumber - 1].startsWith(searchText)) {
                        matchingLines.add(new LineData(fields[columnNumber - 1], line));
                    }
                }
                long endTime = System.currentTimeMillis();
                Collections.sort(matchingLines);

                for (LineData lineData : matchingLines) {
                    System.out.println(lineData.toString());
                }
                System.out.println("Count of found lines = "+matchingLines.size()+" time = "+(endTime-startTime));
                System.out.print("Enter search text: ");
                searchText = scanner.nextLine().trim();
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String readLineFromFile(RandomAccessFile file, long startPosition) throws IOException {
        file.seek(startPosition);
        StringBuilder builder = new StringBuilder();
        int c;
        while ((c = file.read()) != -1 && c != '\n') {
            builder.append((char) c);
        }
        return builder.toString();
    }

    private static class LineData implements Comparable<LineData> {
        private final String value;
        private final String line;

        public LineData(String value, String line) {
            this.value = value;
            this.line = line;
        }

        @Override
        public int compareTo(LineData o) {
            return value.compareTo(o.value);
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", value, line);
        }
    }
}
