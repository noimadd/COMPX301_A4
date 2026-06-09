import java.io.*;
import java.util.*;

/**
 * Simulated Annealing Box Stacking Problem
 * This program reads box dimensions from a parsed text file
 * the program then uses simulated annealing to find an optimal stacking configuration
 * The program takes three command line arguments:
 * 1. file.txt - text file containing box dimensions at one box per line
 * 2. temperature - initial temperature as a positive integer - temperature controls the probability of accepting a worse solution during the search process
 * 3. coolingRate - cooling rate as a double between 0 and 1 - cooling rate determines how quickly the temperature decreases throughout the search process
 * 
 * Last Updated: 09/06/2026
 * Damion Sklenars-Clare | 1638052
 */
public class SimAnnStack {
    private static int[][][] orientations; // [index][orientationIndex][dimensions]
    private static int[] orientationCount; // [index][orientationCount] - number of valid orientations for each box

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Program requires 3 arguments: file.txt temperature coolingRate");
            return;
        }

        // input file with box dimensions
        String file = args[0];
        if (!new File(file).exists() || !file.endsWith(".txt")) {
            System.out.println("File not found or invalid format: " + file);
            return;
        }

        // initial temperature and cooling rate 
        int temp;
        double rate;
        try {
            temp = Integer.parseInt(args[1]);

            if (temp <= 0) {
                System.out.println("Temperature must be a positive integer.");
                return;
            }

            rate = Double.parseDouble(args[2]);

            if (rate <= 0 || rate >= temp) {
                System.out.println("Cooling rate must be a double between 0 and 1.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid temperature or cooling rate value.");
            return;
        }


        int[][] boxes = parseBoxes(file);
        if (boxes.length == 0) {
            System.out.println("No valid boxes found.");
            return;
        }

        genOrientations(boxes);

        System.out.println("Parsed " + boxes.length + " boxes");
        System.out.println("Orientations:");
        for (int i = 0; i < boxes.length; i++) {
            System.out.print("  Box " + i + ": ");
            for (int j = 0; j < orientationCount[i]; j++) {
                int[] ori = orientations[i][j];
                System.out.print("[" + ori[0] + " " + ori[1] + " " + ori[2] + "] ");
            }
            System.out.println();
        }

    }

    /**
     * parses the box dimensions from the input file and returns an array of boxes
     * @param file the input file containing box dimensions
     * @return 2D array of box dimensions - [index][dimensions]
     * @throws IOException 
     */
    private static int[][] parseBoxes(String file) throws IOException {
        ArrayList<int[]> boxes = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+"); // splits by spaces

            if (parts.length != 3) continue;

            try {
                int a = Integer.parseInt(parts[0]);
                int b = Integer.parseInt(parts[1]);
                int c = Integer.parseInt(parts[2]);

                if (a > 0 && b > 0 && c > 0) 
                    boxes.add(new int[]{a, b, c});
            } catch (NumberFormatException e) {}
        }

        br.close();
        return boxes.toArray(new int[0][]);
    }

    private static void genOrientations(int[][] boxes) {
        orientations = new int[boxes.length][3][3]; // max 3 orientations per box
        orientationCount = new int[boxes.length]; // num valid orientations per box

        for (int i = 0; i < boxes.length; i++) {
            int a = boxes[i][0], b = boxes[i][1], c = boxes[i][2];

            // height, width, depth
            int[][] perms = {
                createOrientation(a, b, c),
                createOrientation(c, a, b), 
                createOrientation(b, c, a)
            };

            // check for duplicates
            Set<String> seen = new HashSet<>();
            int count = 0;

            for (int[] perm : perms) {
                String key = Arrays.toString(perm);
                if (!seen.contains(key)) {
                    orientations[i][count++] = perm;
                    seen.add(key);
                }
            }

            orientationCount[i] = count;
        }
    }

    private static int[] createOrientation(int h, int w, int d) {
        int width = Math.min(w, d);
        int length = Math.max(w, d);
        return new int[]{width, length, h};
    }
}