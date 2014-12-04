// Assignment 10
// Martin John
// jmartin4
// Ben Campbell
// nampb

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.funworld.*;
import javalib.worldimages.*;
import javalib.colors.*;
import javalib.worldcanvas.*;


// Represents a single square of the game area
class Node {
    // In logical coordinates, with the origin at the top-left corner of the
    // screen
    int x;
    int y;
    // the four adjacent cells to this one
    Node left;
    Node top;
    Node right;
    Node bottom;
    
    Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public String toString() {
        return "(" + Integer.toString(this.x) + ", " + Integer.toString(this.y) + ")";
    }
}

class Edge {
    int weight;
    Node a;
    Node b;
    
    Edge(Node a, Node b, int weight) {
        this.weight = weight;
        this.a = a;
        this.b = b;
    }
}

class MazeGame {
    // Constants
    static final int BOARD_WIDTH = 100; // in Nodes
    static final int BOARD_HEIGHT = 60; // in Nodes
    static final int NODE_SIZE = 20; // in px
    
    ArrayList<ArrayList<Node>> map;
    ArrayList<Edge> edges;
    
    MazeGame() {
        this.initMaze();
    }
    
    void initMaze() {
        Utils utils = new Utils();
        
        ArrayList<ArrayList<Node>> tempMatrix = this.buildMatrix();
        tempMatrix = this.massageMatrix(tempMatrix);
        ArrayList<Edge> edges = this.extractEdges(tempMatrix);
        utils.sort(edges);
        
        this.edges = edges;
        this.map = tempMatrix;
    }
    
    ArrayList<ArrayList<Node>> buildMatrix() {
        ArrayList<ArrayList<Node>> result =
                new ArrayList<ArrayList<Node>>();
        
        for (int indexY = 0; indexY < BOARD_HEIGHT; indexY = indexY + 1) {
            ArrayList<Node> tempX = new ArrayList<Node>();

            for (int indexX = 0; indexX < BOARD_WIDTH; indexX = indexX + 1) {
                tempX.add(new Node(indexX, indexY));
            }
            
            result.add(tempX);
        }
        
        return result;
    }
    
    // To link together all the Nodes in the given matrix of Nodes with their neighbors.
    ArrayList<ArrayList<Node>> massageMatrix(ArrayList<ArrayList<Node>>
        matrix) {
        
        for (ArrayList<Node> arr : matrix) {
            for (Node n : arr) {
                // Y-Axis
                if (n.y == 0) {
                    n.top = n;
                    n.bottom = matrix.get(n.y + 1).get(n.x);
                }
                else if (n.y == BOARD_HEIGHT - 1) {
                    n.top = matrix.get(n.y - 1).get(n.x);
                    n.bottom = n;
                }
                else {
                    n.top = matrix.get(n.y - 1).get(n.x);
                    n.bottom = matrix.get(n.y + 1).get(n.x);
                }

                // X-Axis
                if (n.x == 0) {
                    n.left = n;
                    n.right = matrix.get(n.y).get(n.x + 1);
                }
                else if (n.x == BOARD_WIDTH - 1) {
                    n.left = matrix.get(n.y).get(n.x - 1);
                    n.right = n;
                }
                else {
                    n.left = matrix.get(n.y).get(n.x - 1);
                    n.right = matrix.get(n.y).get(n.x + 1);
                }
            }
        }
        
        return matrix;
    }
    
    // Returns a list of the Edges as implied by the connections in the given matrix of Nodes.
    ArrayList<Edge> extractEdges(ArrayList<ArrayList<Node>>
        matrix) {

        Random rand = new Random();
        ArrayList<Edge> result = new ArrayList<Edge>();
        
        for (ArrayList<Node> arr : matrix) {
            for (Node n : arr) {
                if(n.x != BOARD_WIDTH - 1) {
                    // Right
                    result.add(new Edge(n, n.right, rand.nextInt(100)));
                }
                if(n.y != BOARD_HEIGHT - 1) {
                    // Bottom
                    result.add(new Edge(n, n.bottom, rand.nextInt(100)));
                }
            }
        }
        
        return result;
    }
    
    ArrayList<Edge> kruskal(ArrayList<Edge> edges, ArrayList<ArrayList<Node>> nodes) {
        HashMap<String, String> reps = new HashMap<String, String>();
        ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
        Utils utils = new Utils();
        
        // Init all representations to themselves.
        for(ArrayList<Node> arr : nodes) {
            for(Node n : arr) {
                reps.put(n.toString(), n.toString());
            }
        }
        
        // 
        
        for (int index = 1 ; index < edges.size(); index = index + 1) {
            
            if(utils.find(reps, edges.get(index).a.toString()).equals(utils.find(reps, (edges.get(index).b.toString())))) {
                
            }
            else {
                edgesInTree.add(edges.get(index));
                utils.union(reps, utils.find(reps, edges.get(index).a.toString()), 
                        utils.find(reps, edges.get(index).b.toString()));
            }
        }
        
        return edgesInTree;
    }
}

class Utils {
    //Effect swap the values at 2 indices in given array
    <T> void swap(ArrayList<T> arr, int idx1, int idx2) {
        T temp = arr.get(idx1);
        arr.set(idx1, arr.get(idx2));
        arr.set(idx2, temp);
        
    }
        
    // Finds the index of the minimum item after the given start
    // Assumes that start is in the list
    int findMin(ArrayList<Edge> arr, int start) {
        Edge minSoFar = arr.get(start);
        int indexOfMin = start;
        for (int index = start; index < arr.size(); index = index + 1) {
            if(minSoFar.weight >= arr.get(index).weight) {
                minSoFar = arr.get(index);
                indexOfMin = index;
            }
        }
        return indexOfMin;
    }
    
    void sort(ArrayList<Edge> arr) {
        for(int index = 0; index < arr.size(); index = index + 1) {
            int indexOfMin = this.findMin(arr, index);
            this.swap(arr, indexOfMin, index);
        }
    }
    
    String find(HashMap<String, String> hashMap, String key) {
        String k = hashMap.get(key);
        
        if(k.equals(key)) {
            return k;
        }
        else {
            return 
                    this.find(hashMap, k);
        }
    }
    
    void union(HashMap<String, String> hashMap, String rep1, String rep2) {
        hashMap.put(rep1, rep2);
        
    }
}

class ExamplesMaze {
    MazeGame game;
    
    void setup() {
        this.game = new MazeGame();
    }
    
    void testConstructor(Tester t) {
        this.setup();
        t.checkExpect(this.game.edges.size(), 11840);
    }
    
    void testKuskal(Tester t) {
        this.setup();
        t.checkExpect(this.game.kruskal(this.game.edges, this.game.map).size(), 5999);
    }
    
    void testUtils(Tester t) {
       ArrayList<Edge> test = new ArrayList<Edge>(Arrays.asList(new Edge(new Node(1, 1), new Node(1, 1), 10), 
               new Edge(new Node(1, 1), new Node(1, 1), 9), new Edge(new Node(1, 1), new Node(1, 1), 5),
               new Edge(new Node(1, 1), new Node(1, 1), 15)));
       Utils utils = new Utils();
       utils.sort(test);
       
       HashMap<String, String> hash = new HashMap();
       hash.put("A", "A");
       hash.put("B", "B");
       hash.put("C", "C");
       hash.put("D", "E");
       hash.put("E", "A");
       
       t.checkExpect(utils.find(hash, "D"), "A");
       t.checkExpect(utils.find(hash, "E"), "A");
       t.checkExpect(utils.find(hash, "A"), "A");
    }
    
    
    
    
    
}