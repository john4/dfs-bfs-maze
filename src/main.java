// Assignment 10
// Martin John
// jmartin4
// Ben Campbell
// nampb

import java.awt.Color;
import java.util.ArrayDeque;
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
    // State determines color
    String state;
    
    Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.left = this;
        this.right = this;
        this.top = this;
        this.bottom = this;
        this.state = "unvisited";
    }
    
    public String toString() {
        return "(" + Integer.toString(this.x) + ", " + Integer.toString(this.y) + ")";
    }
    
    // Returns a list of Nodes adjacent to this Node that are not itself.
    ArrayList<Node> getLinkedNodes() {
        ArrayList<Node> result = new ArrayList<Node>();
        
        if(this.left != this) {
            result.add(this.left);
        }

        if(this.top != this) {
            result.add(this.top);
        }

        if(this.right != this) {
            result.add(this.right);
        }

        if(this.bottom != this) {
            result.add(this.bottom);
        }
        
        return result;
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
    
    // Determines the mode of the game:
    //  - DFS
    //  - BFS
    //  - human
    String gameMode;
    
    // The Node that is being controlled by the human.
    Node seeker;
    // The Node(s) that are actively searching in an automated search.
    ArrayList<Node> seekers;
    // The worklist Stack for the depth first search. 
    ArrayDeque<Node> worklist;
    
    MazeGame(String mode) {
        if(mode.equals("DFS") || mode.equals("BFS") || mode.equals("human")) {
            this.gameMode = mode;
            
            // Create a new maze
            this.initMaze();
            this.linkNodes(this.kruskal(this.edges, this.map));
            
            for(ArrayList<Node> arr : this.map) {
                for(Node n : arr) {
                    System.out.println("THIS: " + n.toString() + "    Top: " + n.top.toString() + " bottom: " + n.bottom.toString()
                            + " left: " + n.left.toString() + " right: " + n.right.toString());
                }
            }
            
            // Initialize game play necessities.
            Node start = this.map.get(0).get(0);
            this.seeker = start;
            this.seekers = new ArrayList<Node>();
            this.worklist = new ArrayDeque<Node>();
            
            // Set initial states based on selected game mode
            if(mode.equals("DFS")) {
                System.out.println("YUP");
                this.InitDFS(start);
            }
        }
        else {
            throw new RuntimeException("Constructed MazeGame with an invalid mode setting.");
        }
    }
    
    // Convenience constructor for a human-controlled game.
    MazeGame() {
        this("human");
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
        
        result.get(0).get(0).state = "start";
        result.get(BOARD_HEIGHT - 1).get(BOARD_WIDTH - 1).state = "end";
        
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
    
    /////////////////////////////////////////////////////////////////////////
    
    // Initiates an automated depth first search of this maze, starting
    // at the given Node.
    // SIDEEFFECT: Sets this worklist and this seekers.
    void InitDFS(Node start) {
        ArrayDeque<Node> worklist = new ArrayDeque<Node>();
        
        // Initialize worklist to the nodes adjacent to the start.
        for(Node n : start.getLinkedNodes()) {
            worklist.push(n);
        }
        
        this.worklist = worklist;
        this.seekers.add(start);
    }
    
    // Steps the automated depth first search of this maze, to be
    // called by this onTick().
    // SIDEEFFECT: Updates this worklist, this seekers, and the
    // states of seeking nodes. 
    void StepDFS() {
        // NOTE: in DFS, we only have a single seeker in our seekers list.
        Node seeker = this.seekers.get(0);
        
        // Pop the next node to become the seeker.
        Node nextSeeker = this.worklist.pop();
        
        // Update states.
        seeker.state = "visited";
        nextSeeker.state = "seeker";
        
        this.seekers.remove(0);
        this.seekers.add(nextSeeker);
        
        // Add to the worklist all, if any, of the adjacent nodes of the new seeker.
        for(Node n : nextSeeker.getLinkedNodes()) {
            if(n.state != "visited") {
                worklist.push(n);
            }
        }
    }

    // Operates the world on each tick.
    public World onTick() {
        if(this.gameMode.equals("DFS")) {
            this.StepDFS();
        }
        else if(this.gameMode.equals("BFS")) {
            
        }
        else {
            
        }
        
        return this;
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
        
        // Draw the Nodes onto the background.
        for (ArrayList<Node> arr : this.map) {
            
            for(Node n : arr) {
                // Determine color.
                Color c;
                
                if(n.state.equals("visited")) {
                    c = new Color(255, 178, 201);
                }
                else if(n.state.equals("seeker")) {
                    c = new Color(153, 76, 0);
                }
                else if(n.state.equals("highlight")) {
                    c = new Color(255, 128, 0);
                }
                else if(n.state.equals("start")) {
                    c = new Color(0, 204, 0);
                }
                else if(n.state.equals("end")) {
                    c = new Color(200, 0, 0);
                }
                else {
                    c = new Color(255, 255, 255);
                }
                
                // Determine size and position.
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
                
                // Overlay.
                this.background = new OverlayImages(this.background,
                        new RectangleImage(new Posn((n.x * NODE_SIZE) + right / 2 - left / 2 + HALF_NODE_SIZE, (n.y * NODE_SIZE) + bottom / 2 - top / 2 + HALF_NODE_SIZE),
                                NODE_SIZE - 4 + left + right, NODE_SIZE - 4 + top + bottom, c));
                
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
    MazeGame w = new MazeGame("DFS");
    boolean runAnimated = this.w.bigBang(1000, 600, 0.01);
    
    
}