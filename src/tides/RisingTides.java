package tides;

import java.util.*;

/**
 * This class contains methods that provide information about select terrains 
 * using 2D arrays. Uses floodfill to flood given maps and uses that 
 * information to understand the potential impacts. 
 * Instance Variables:
 *  - a double array for all the heights for each cell
 *  - a GridLocation array for the sources of water on empty terrain 
 * 
 * @author Original Creator Keith Scharz (NIFTY STANFORD) 
 * @author Vian Miranda (Rutgers University)
 */
public class RisingTides {

    // Instance variables
    private double[][] terrain;     // an array for all the heights for each cell
    private GridLocation[] sources; // an array for the sources of water on empty terrain 

    /**
     * DO NOT EDIT!
     * Constructor for RisingTides.
     * @param terrain passes in the selected terrain 
     */
    public RisingTides(Terrain terrain) {
        this.terrain = terrain.heights;
        this.sources = terrain.sources;
    }

    /**
     * Find the lowest and highest point of the terrain and output it.
     * 
     * @return double[][], with index 0 and index 1 being the lowest and 
     * highest points of the terrain, respectively
     */
    public double[] elevationExtrema() { 
        double lowest = Double.MAX_VALUE;
        double highest = Double.MIN_VALUE;

        for (int row = 0; row < terrain.length; row++) {
            for (int col = 0; col < terrain[row].length; col++) {
                double currentHeight = terrain[row][col];
                lowest = Math.min(lowest, currentHeight);
                highest = Math.max(highest, currentHeight);
            }
        }

        return new double[]{lowest, highest};
    }

   
    /**
     * Implement the floodfill algorithm using the provided terrain and sources.
     * 
     * All water originates from the source GridLocation. If the height of the 
     * water is greater than that of the neighboring terrain, flood the cells. 
     * Repeat iteratively till the neighboring terrain is higher than the water 
     * height.
     * 
     * 
     * @param height of the water
     * @return boolean[][], where flooded cells are true, otherwise false
     */
    public boolean[][] floodedRegionsIn(double height) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        boolean[][] flooded = new boolean[rows][cols];
        ArrayList<GridLocation> queue = new ArrayList<>();

        // Step 2: Initialize an ArrayList of GridLocations for initial sources (coastline).
        // You need to add your initial water source locations to this ArrayList.
        for (GridLocation source : sources) {
            queue.add(source);
        }

        // Step 3: Flood the initial source GridLocations.
        for (GridLocation source : queue) {
            flooded[source.row][source.col] = true;
        }

        int[] dr = {-1, 1, 0, 0}; // Offsets for four cardinal directions.
        int[] dc = {0, 0, -1, 1};

        // Step 4: Repeat until the ArrayList is empty.
        while (!queue.isEmpty()) {
            // Step 5: Remove the first element from the ArrayList.
            GridLocation current = queue.remove(0);
            int row = current.row;
            int col = current.col;

            // Step 6: Check all of its neighbors in each of the four cardinal directions.
            for (int i = 0; i < 4; i++) {
                int newRow = row + dr[i];
                int newCol = col + dc[i];

                // Check if the neighbor is within the grid bounds.
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    // Step 7: If the neighbor's height is <= provided height, flood the neighbor.
                    if (terrain[newRow][newCol] <= height && !flooded[newRow][newCol]) {
                        flooded[newRow][newCol] = true;
                        queue.add(new GridLocation(newRow, newCol));
                    }
                }
            }
        }

        // Step 8: Return the resulting array.
        return flooded;
    }

    


      
    /**
     * Checks if a given cell is flooded at a certain water height.
     * 
     * @param height of the water
     * @param cell location 
     * @return boolean, true if cell is flooded, otherwise false
     */
    public boolean isFlooded(double height, GridLocation cell) {
        return floodedRegionsIn(height)[cell.row][cell.col];
    }
        
    

    /**
     * Given the water height and a GridLocation find the difference between 
     * the chosen cells height and the water height.
     * 
     * If the return value is negative, the Driver will display "meters below"
     * If the return value is positive, the Driver will display "meters above"
     * The value displayed will be positive.
     * 
     * @param height of the water
     * @param cell location
     * @return double, representing how high/deep a cell is above/below water
     */
    public double heightAboveWater(double height, GridLocation cell) {
        double cellHeight = terrain[cell.row][cell.col];
        return cellHeight - height;
    }
        

    /**
     * Total land available (not underwater) given a certain water height.
     * 
     * @param height of the water
     * @return int, representing every cell above water
     */
    public int totalVisibleLand(double height) {
        boolean[][] resultingArray = floodedRegionsIn(height); // Remove the extra opening bracket here
        int count = 0;
        for (int row = 0; row < resultingArray.length; row++) {
            for (int col = 0; col < resultingArray[0].length; col++) { // Use resultingArray[0].length for columns
                if (resultingArray[row][col] == false) { // Use == for comparison, not =
                    count++;
                }
            }
        }
        return count;
    }
        
        
    
    
    
        

    /**
     * Given 2 heights, find the difference in land available at each height. 
     * 
     * If the return value is negative, the Driver will display "Will gain"
     * If the return value is positive, the Driver will display "Will lose"
     * The value displayed will be positive.
     * 
     * @param height of the water
     * @param newHeight the future height of the water
     * @return int, representing the amount of land lost or gained
     */
    public int landLost(double height, double newHeight) {
        int landAtHeight = totalVisibleLand(height);
        int landAtNewHeight = totalVisibleLand(newHeight);
        return landAtHeight - landAtNewHeight;
    }


        

    /**
     * Count the total number of islands on the flooded terrain.
     * 
     * Parts of the terrain are considered "islands" if they are completely 
     * surround by water in all 8-directions. Should there be a direction (ie. 
     * left corner) where a certain piece of land is connected to another 
     * landmass, this should be considered as one island. A better example 
     * would be if there were two landmasses connected by one cell. Although 
     * seemingly two islands, after further inspection it should be realized 
     * this is one single island. Only if this connection were to be removed 
     * (height of water increased) should these two landmasses be considered 
     * two separate islands.
     * 
     * @param height of the water
     * @return int, representing the total number of islands
     */
 public int numOfIslands(double height) {
    boolean[][] flooded = floodedRegionsIn(height);
    int rows = flooded.length;
    int cols = flooded[0].length;
 
    WeightedQuickUnionUF uf = new WeightedQuickUnionUF(rows, cols);

    int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            if (!flooded[i][j]) {
                for (int[] dir : directions) {
                    int newRow = i + dir[0];
                    int newCol = j + dir[1];
                    if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && !flooded[newRow][newCol]) {
                        uf.union(new GridLocation(i, j), new GridLocation(newRow, newCol));
                    }
                }
            }
        }
    }

    Set<GridLocation> uniqueIslands = new HashSet<>();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            if (!flooded[i][j]) { 
                uniqueIslands.add(uf.find(new GridLocation(i, j)));
            }
        }
    }

    return uniqueIslands.size();
}
}
