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
            System.out.println("Use: java -jar -Xmx7m target/renue-test-autocomplete-*.jar <column_number>");
            System.exit(1);
        }
        int columnNumber = Integer.parseInt(args[0]);
        if (columnNumber < 1) {
            System.out.println("Column number should be >=1");
            System.exit(1);
        }
        try (RandomAccessFile file = new RandomAccessFile(new File(FILE_NAME), "r")) {
            HashMap<Character, ArrayList<Long>> charToPositions = characterToArrayList(file, columnNumber);
            System.out.print("Enter text : ");
            Scanner lookScanner = new Scanner(System.in);
            String searchText;
            while (!(searchText = lookScanner.nextLine()).equals("!quit")) {
                if (searchText.isEmpty()) {
                    System.out.print("Input is Empty. Try again or quit using <!quit>. Enter text: ");
                    continue;
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
                    String currentLine = file.readLine();
                    ArrayList<String> fields = parseCsv(currentLine);
                    if (fields.get(columnNumber - 1).startsWith(searchText)) {
                        matchingLines.add(new LineData(fields.get(columnNumber - 1), currentLine));
                    }
                }
                if (matchingLines.isEmpty()) {
                    System.out.print("Symbols not found. Try again or quit using <!quit>. Enter text: ");
                    continue;
                }
                long endTime = System.currentTimeMillis();
                Collections.sort(matchingLines);

                for (LineData lineData : matchingLines) {
                    System.out.println(lineData.toString());
                }
                System.out.println("Count of found lines = " + matchingLines.size() + " time = " + (endTime - startTime));
                System.out.print("Enter search text: ");
            }
        } catch (
                IOException e) {
            System.out.println("Error open file");
            System.exit(1);
        }
    }


    private static ArrayList<String> parseCsv(String line) {
        ArrayList<String> parsed = new ArrayList<>();
        boolean inQuotes = false;

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char currentSymbol = line.charAt(i);

            if (currentSymbol == '"') {
                inQuotes = !inQuotes;
            } else if (currentSymbol == ',' && !inQuotes) {
                parsed.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(currentSymbol);
            }
        }
        parsed.add(stringBuilder.toString());
        return parsed;


    }

    private static HashMap<Character, ArrayList<Long>> characterToArrayList(RandomAccessFile file, int columnNumber) throws
            IOException {
        HashMap<Character, ArrayList<Long>> charToPositions = new HashMap<>();
        String currentLine;
        long position = file.getFilePointer();
        while ((currentLine = file.readLine()) != null) {
            ArrayList<String> fields = parseCsv(currentLine);
            if (columnNumber > fields.size()) {
                System.out.println("Column number > then in file");
                file.close();
                System.exit(1);
            }
            if (charToPositions.containsKey(fields.get(columnNumber - 1).charAt(0))) {
                ArrayList<Long> positions = charToPositions.get(fields.get(columnNumber - 1).charAt(0));
                positions.add(position);
                charToPositions.replace(fields.get(columnNumber - 1).charAt(0), positions);
            } else {
                ArrayList<Long> positions = new ArrayList<>();
                positions.add(position);
                charToPositions.put(fields.get(columnNumber - 1).charAt(0), positions);
            }
            position = file.getFilePointer();
        }
        return charToPositions;
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
            return String.format("\"%s\"[%s]", value, line);
        }
    }
}

