# COMPX301_A4 Simulated Annealing
- Damion Sklenars-Clare
- 1638052

## Insutructions to Run

## Initial Candidate Solution
1. Expand all boxes into their orientations
2. Sort each orientation by base area deescending
3. If the orientation's base fits on top and original box isn't used add it to the top
4. Result is a valid but likely unoptimal stack

## AI Usage
AI was used in order to help me define and break down the assignment and my solutions into separate steps/sections. I also made use of it when debugging for example I had a bug where a box that should only have two solutions had three and wasn't sure why - AI then pointed out that I had my min and max calculations for the createOrientation method in the wrong order which was causing the mismatch.