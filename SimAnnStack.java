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
 * Last Updated: 12/06/2026
 * Damion Sklenars-Clare | 1638052
 */
public class SimAnnStack {
    private static int[][][] orientations; // [index][orientationIndex][dimensions]
    private static int[] orientationCount; // number of valid orientations for each box
    private static int totalIterations; // number of iterations to get to the solution

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

        // generates the best stack it can over a range of attempts based on temperature and rate
        boolean[] include = new boolean[boxes.length];
        Arrays.fill(include, true);
        int[][] stack = buildInitialSolution(include);
        int height = stackHeight(stack);

        int[][] best = simulatedAnnealing(stack, boxes, temp, rate);
        int finalHeight = stackHeight(best);


        // write all of the statistics about the best stack and the stack itself to output.txt
        PrintWriter pw = new PrintWriter(new FileWriter("output.txt"));

        int cumulativeHeight = finalHeight;
        for (int[] box : best) {
            pw.println(box[0] + " " + box[1] + " " + box[2] + " " + cumulativeHeight);
            cumulativeHeight -= box[2];
        }

        pw.println("\n------------- Stats -------------");
        pw.println("Initial height (greedy): " + height);
        pw.println("Final height (SA): " + finalHeight);
        pw.println("Improvement: " + (finalHeight - height));
        pw.println("Iterations run: " + totalIterations);
        pw.println("Boxes considered: " + boxes.length);
        pw.println("Boxes used in stack: " + best.length);

        pw.close();

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

    /**
     * generates a 2D array of all valid orientations for each box
     * each box can have up to 3 unique orientations
     * @param boxes array of box dimensions
     */
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

            // adds unique orientations to final array
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

    /**
     * creates an orientation for a box - also ensures that the width is always smaller than the length
     * @param h height
     * @param w width
     * @param d depth
     * @return an array representing the orientation
     */
    private static int[] createOrientation(int h, int w, int d) {
        int width = Math.min(w, d);
        int length = Math.max(w, d);
        return new int[]{width, length, h};
    }

    /**
     * greedy algorithm that builds initial stack solution
     * orientations are sorted in descending order of base area
     * boxes are added if they can fit and have not been used yet
     * @param include boolean array where true if box i should be considered - boolean used for ease of randomness in gen neighbour
     * @return 2D array of the stack solution
     */
    static int[][] buildInitialSolution(boolean[] include) {
        // creates a 1D list of all orientations
        // for each box and each orientation of that box add the orientation dimensions and the box index
        ArrayList<int[]> allOrientations = new ArrayList<>();
        for (int i = 0; i < include.length; i++) {
            if (!include[i]) continue;
            for (int j = 0; j < orientationCount[i]; j++) {
                int[] ori = orientations[i][j];
                allOrientations.add(new int[]{ori[0], ori[1], ori[2], i});
            }
        }

        // Sort by base area descending
        allOrientations.sort((a, b) -> (b[0] * b[1]) - (a[0] * a[1]));

        ArrayList<int[]> stack = new ArrayList<>(); // stack solution
        boolean[] used = new boolean[include.length]; // which boxes have been used already

        // iterate through each orientation and add if it fits and hasn't been used - greedy solution
        for (int[] ori : allOrientations) {
            int boxIndex = ori[3];
            if (used[boxIndex]) continue;

            if (stack.isEmpty() || fitsOn(ori, stack.get(stack.size() - 1))) {
                stack.add(ori);
                used[boxIndex] = true;
            }
        }

        return stack.toArray(new int[0][]);
    }

    /**
     * returns true if top box can fit on bottom box
     * @param top orientation of the top box
     * @param bottom orientation of the bottom box
     * @return true if top box fits on bottom box
     */
    private static boolean fitsOn(int[] top, int[] bottom) {
        return top[0] < bottom[0] && top[1] < bottom[1];
    }

    /**
     * calculates the total height of a passed stack
     * @param stack a stack of boxes
     * @return the height in units
     */
    private static int stackHeight(int[][] stack) {
        int total = 0;
        for (int[] box : stack) total += box[2];
        return total;
    }

    /**
     * generates neighbours at random, accepting better solutions and randomly accepting worse ones
     * @param initialStack the stack created by greedy solution
     * @param boxes all of the boxes, including dimensions
     * @param temp chance of switching to worse solution
     * @param rate rate at which temp decreases
     * @return best found solution of stacked boxes
     */
    private static int[][] simulatedAnnealing(int[][] initialStack, int[][] boxes, double temp, double rate) {
        int[][] current = initialStack; // current stack
        int[][] best = initialStack; // best stack - initially equal to greedy
        int currentHeight = stackHeight(current); // current height
        int bestHeight = currentHeight; // best height - initially equal to greedy
        Random rand = new Random(); // for chance to accept worse option
        totalIterations = 0; // resets for stats stuff

        // while temp is larger than 0 continue to generate neighbours randomly swapping to them based on the temperature
        while(temp > 0) {
            int changes = (int) Math.ceil(temp); // number of changes to make - decreases alongside temperature
            int[][] neighbour = genNeighbour(current, boxes.length, changes); // newly generated neighbour
            int neighbourHeight = stackHeight(neighbour); // neighbour height
            int delta = neighbourHeight - currentHeight; // difference between neighbour and current height

            // if delta > 0 switch else switch to the worse solution at random
            if (delta > 0) {
                current = neighbour;
                currentHeight = neighbourHeight;
            } else {
                double probAccept = Math.exp((double) delta / temp);
                if (rand.nextDouble() < probAccept) {
                    current = neighbour;
                    currentHeight = neighbourHeight;
                }
            }

            // updates current best solution
            if (currentHeight > bestHeight) {
                best = current;
                bestHeight = currentHeight;
            }

            totalIterations++;
            temp -= rate;
        }

        return best;
    }

    /**
     * randomly 'bit flips' boxes
     * randomly changes boxes from included to not included and vice versa to see if the new solution is better or worse
     * @param current current stack
     * @param numBoxes number of available boxes
     * @param changes number of flips to make
     * @return a new stack
     */
    private static int[][] genNeighbour(int[][] current, int numBoxes, int changes) {
        boolean[] include = new boolean[numBoxes];
        for (int[] box : current) {
            include[box[3]] = true;
        }

        Random rand = new Random();
        for (int i = 0; i < changes; i++) {
            int randId = rand.nextInt(numBoxes);
            include[randId] = !include[randId];
        }

        return buildInitialSolution(include);
    }
}