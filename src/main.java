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
        this.left = this;
        this.right = this;
        this.top = this;
        this.bottom = this;
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

class MazeGame extends World {
    // Constants
    static final int BOARD_WIDTH = 30; // in Nodes
    static final int BOARD_HEIGHT = 30; // in Nodes
    static final int HALF_BOARD_WIDTH = BOARD_WIDTH / 2; // in Nodes
    static final int HALF_BOARD_HEIGHT = BOARD_HEIGHT / 2; // in Nodes
    static final int NODE_SIZE = 15; // in px
    static final int HALF_NODE_SIZE = NODE_SIZE / 2; // in px  
    
    ArrayList<ArrayList<Node>> map;
    ArrayList<Edge> edges;
    
    MazeGame() {
        this.initMaze();
        this.linkNodes(this.kruskal(this.edges, this.map));
        
        for(ArrayList<Node> arr : this.map) {
            for(Node n : arr) {
                System.out.println("THIS: " + n.toString() + "    Top: " + n.top.toString() + " bottom: " + n.bottom.toString()
                        + " left: " + n.left.toString() + " right: " + n.right.toString());
            }
        }
    }
    
    void initMaze() {
        Utils utils = new Utils();
        
        ArrayList<ArrayList<Node>> tempMatrix = this.buildMatrix();
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
    
    // Returns a list of the Edges as implied by the connections in the given matrix of Nodes.
    ArrayList<Edge> extractEdges(ArrayList<ArrayList<Node>>
        matrix) {
        Random rand = new Random();
        ArrayList<Edge> result = new ArrayList<Edge>();
        
        for (int indexY = 0; indexY < matrix.size(); indexY = indexY + 1) {
            ArrayList<Node> row = matrix.get(indexY);
            
            for (int indexX = 0; indexX < row.size(); indexX = indexX + 1) {
                Node n = row.get(indexX);
                
                if(n.x != BOARD_WIDTH - 1) {
                    // Right
                    result.add(new Edge(n, row.get(indexX + 1), rand.nextInt(100)));
                    System.out.println(n.toString() + ",  " + row.get(indexX + 1).toString());
                }
                if(n.y != BOARD_HEIGHT - 1) {
                    // Bottom
                    result.add(new Edge(n, matrix.get(indexY + 1).get(indexX), rand.nextInt(100)));
                    System.out.println(n.toString() + ",  " + matrix.get(indexY + 1).get(indexX).toString());
                }
            }
        }
        
        return result;
    }
    
    // Takes a list of Nodes and list of Edges with weights for those nodes and 
    // returns a list of Edges that are representative of what is actually to be
    // used for this game's spanning tree.
    ArrayList<Edge> kruskal(ArrayList<Edge> edges, ArrayList<ArrayList<Node>> nodes) {
        HashMap<String, String> reps = new HashMap<String, String>();
        ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
        Utils utils = new Utils();
        
        // Init all representations to themselves.
        for(ArrayList<Node> arr : nodes) {
            for(Node n : arr) {
                reps.put(n.toString(), n.toString());
                System.out.println(n.toString());
            }
        }
        
        System.out.println("LENGTHMOFO " + reps.size());
        
        for (int index = 1 ; index < edges.size(); index = index + 1) { 
            if(!utils.find(reps, edges.get(index).a.toString()).equals(utils.find(reps, (edges.get(index).b.toString())))) {
                edgesInTree.add(edges.get(index));
                utils.union(reps, utils.find(reps, edges.get(index).a.toString()), 
                        utils.find(reps, edges.get(index).b.toString()));
            }
        }
        
        return edgesInTree;
    }
    
    void linkNodes(ArrayList<Edge> edges) {
        for(Edge edge : edges) {
            // Above
            if(edge.a.x == edge.b.x && edge.a.y < edge.b.y) {
                edge.a.bottom = edge.b;
                edge.b.top = edge.a;
            }
            // Below
            else if(edge.a.x == edge.b.x && edge.a.y > edge.b.y) {
                edge.b.bottom = edge.a;
                edge.a.top = edge.b;
            }
            // Left
            else if(edge.a.x < edge.b.x && edge.a.y == edge.b.y) {
                edge.a.right = edge.b;
                edge.b.left = edge.a;
            }
            // Right
            else if (edge.a.x > edge.b.x && edge.a.y == edge.b.y) {
                edge.b.right = edge.a;
                edge.a.left = edge.b;
            }
        }
    }
    
    // The background for the world.
    WorldImage background;
    
    // Creates the image of the game.
    public WorldImage makeImage() {
        // Initialize the background.
        this.background = new RectangleImage(new Posn(
                (HALF_BOARD_WIDTH * NODE_SIZE),
                (HALF_BOARD_HEIGHT * NODE_SIZE)), (BOARD_WIDTH * NODE_SIZE),
                (BOARD_HEIGHT * NODE_SIZE), new Black());

//        System.out.println(this.map.get(0).get(0).top.toString());
//        System.out.println(this.map.get(0).get(0).bottom.toString());
//        System.out.println(this.map.get(0).get(0).left.toString());
//        System.out.println(this.map.get(0).get(0).right.toString());
        
        // Draw the Cells onto the background.
        for (ArrayList<Node> arr : this.map) {
            
            for(Node n : arr) {
//                this.background = new OverlayImages(this.background,
//                        new RectangleImage(new Posn(n.x * NODE_SIZE + HALF_NODE_SIZE, n.y * NODE_SIZE + HALF_NODE_SIZE), NODE_SIZE, NODE_SIZE, new Blue()));
//                
//                // Top edge case
//                if(n.y == 0) {
//                    this.background = new OverlayImages(this.background,
//                            new RectangleImage(new Posn((n.x * NODE_SIZE) + HALF_NODE_SIZE, n.y * NODE_SIZE),
//                                    NODE_SIZE, 2, new Black()));
//                }
//                
//                // Left edge case
//                if(n.x == 0) {
//                    this.background = new OverlayImages(this.background,
//                            new RectangleImage(new Posn(n.x * NODE_SIZE, (n.y * NODE_SIZE) + HALF_NODE_SIZE),
//                                    2, NODE_SIZE, new Black()));
//                }
//                
//                if(n.bottom.toString().equals(n.toString())) {
//                    this.background = new OverlayImages(this.background,
//                            new RectangleImage(new Posn((n.x * NODE_SIZE) + HALF_NODE_SIZE, (n.y * NODE_SIZE) + NODE_SIZE),
//                                    NODE_SIZE, 2, new Black()));
//                }
//                
//                if(n.right.toString().equals(n.toString())) {
//                    this.background = new OverlayImages(this.background,
//                            new RectangleImage(new Posn(n.x * NODE_SIZE + NODE_SIZE, (n.y * NODE_SIZE) + HALF_NODE_SIZE),
//                                    2, NODE_SIZE, new Black()));
//                }
//                
                
                int top = 0;
                int bottom = 0;
                int left = 0;
                int right = 0;
                
                if(n.bottom != n) {
                    bottom = 2;
                }
                if(n.top != n) {
                    top = 2;
                }
                if(n.left != n) {
                    left = 2;
                }
                if(n.right != n) {
                    right = 2;
                }
                
                
                this.background = new OverlayImages(this.background,
                        new RectangleImage(new Posn((n.x * NODE_SIZE) + right / 2 - left / 2, (n.y * NODE_SIZE) + bottom / 2 - top / 2),
                                NODE_SIZE - 4 + left + right, NODE_SIZE - 4 + top + bottom, new White()));
                
            }
        }
        
        return background;
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
    
    
    // RUN THE GAME
    MazeGame w = new MazeGame();
    boolean runAnimated = this.w.bigBang(1000, 600, 0.2);
    
    
}