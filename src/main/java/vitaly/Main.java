
package vitaly;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private static final String FILE_NAME = "src/main/resources/airports.csv";
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java -jar -Xmx7m target/renue-test-autocomplete-input-text-1.0-SNAPSHOT.jar <column_number>");
            System.exit(1);
        }
        int columnNumber = Integer.parseInt(args[0]);
        if (columnNumber < 1) {
            System.out.println("Column number should be >=1");
            System.exit(1);
        }

        System.out.print("Enter text : ");
        try (RandomAccessFile file = new RandomAccessFile(new File(FILE_NAME), "r")) {
            HashMap<Character, ArrayList<Long>> charToPositions = new HashMap<>();
            String currentLine;
            long position = file.getFilePointer();
            while ((currentLine = file.readLine()) != null) {
                String[] fields = currentLine.replace("\"", "").split("\",");
                if(fields[columnNumber - 1].charAt(0) == 'a'){
                    System.out.println("???");
                }
                if(columnNumber>fields.length){
                    System.out.println("Column number > then in file");
                    file.close();
                    System.exit(1);
                }
                if (charToPositions.containsKey(fields[columnNumber - 1].charAt(0))) {
                    ArrayList<Long> positions = charToPositions.get(fields[columnNumber - 1].charAt(0));
                    positions.add(position);
                    charToPositions.replace(fields[columnNumber - 1].charAt(0), positions);
                } else {

                    ArrayList<Long> positions = new ArrayList<>();
                    positions.add(position);
                    charToPositions.put(fields[columnNumber - 1].charAt(0), positions);
                }
                position = file.getFilePointer();
            }

            Scanner lookScanner = new Scanner(System.in);
            String searchText;
            while (!(searchText = lookScanner.nextLine()).equals("!quit")) {
                if (searchText.isEmpty()) {
                    break;
                }
                long startTime = System.currentTimeMillis();

                ArrayList<Long> positions = charToPositions.get(searchText.charAt(0));
                ArrayList<LineData> matchingLines = new ArrayList<>();
                if (positions == null) {
                    System.out.print("Symbols not found. Try again or quit using <!quit>. Enter text: ");
                    continue;
                }
                for (Long pos : positions) {
                    file.seek(pos);
                    currentLine = file.readLine();
                    String[] fields = currentLine.replace("\"", "").split(",");
                    if (fields[columnNumber - 1].startsWith(searchText)) {
                        matchingLines.add(new LineData(fields[columnNumber - 1], currentLine));
                    }
                }
                Collections.sort(matchingLines);
                long endTime = System.currentTimeMillis();
                for (LineData lineData : matchingLines) {
                    System.out.println(lineData.toString());
                }
                System.out.println("Count of found lines = " + matchingLines.size() + " time = " + (endTime - startTime));
                System.out.print("Enter search text: ");
            }
            file.close();
        } catch (IOException e) {
            System.out.println("Error open file");
            System.exit(1);
        }
    }

    public static ArrayList<String> parseCsv(String line){
        ArrayList<String> parsed = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char currentSymbol = line.charAt(i);
            if(currentSymbol == '"'){
                inQuotes=!inQuotes;
            } else if (currentSymbol == ',' && !inQuotes) {
                parsed.add(stringBuilder.toString());
            }else {
                stringBuilder.append(currentSymbol);
            }
        }
        return parsed;


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

