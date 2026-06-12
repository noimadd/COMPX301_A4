# COMPX301_A4 Simulated Annealing
- Damion Sklenars-Clare
- 1638052

## Instructions to Run
Compile and run with three arguments
1. 'text file' - contains one box per line with dimensions e.g. "3 1 10"
2. 'temperature' - positive integer, controls how likely a worse solution is to be picked
3. 'coolingRate' - decimal between 0 and temperature

example
javac *.java
java SimAnnStack Boxes.txt 1000 0.01

Output is written to 'output.txt'

## Initial Candidate Solution
1. Expand all boxes into their orientations
2. Sort each orientation by base area deescending
3. If the orientation's base fits on top and original box isn't used add it to the top
4. Result is a valid but likely unoptimal stack

## Simulated Annealing
1. Starts from the initial candidate solution(greedy solution)
2. Each iteration randomly flips boxes in/out of the current stack
3. Number of changes in an iteration is ```ceil(temp)``` - this leads to more changes earlier on with less as it begins to cool down
4. Always accepts better neighbours, worse ones are selected with probability ```e^(delta/temp)```
5. Tracks best solution across all iterations, returning it once temperature <= 0

## External Help

### AI Usage
AI was used in order to help me define and break down the assignment into separate steps/sections. It also helped when debugging for example I had a bug where a box that should only have two orientations had three and wasn't sure why - AI then pointed out that I had my min and max calculations for the createOrientation method in the wrong order which was causing the mismatch.

### Articles
- [Simulated Annealing - Wikipedia](https://en.wikipedia.org/wiki/Simulated_annealing)
I didn't look up much external material however, I did use this wiki article on simulated annealing to clarify the cooling and acceptance probability equation. The general equation being stated within the Acceptance Probability section