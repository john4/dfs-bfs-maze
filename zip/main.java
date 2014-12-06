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
    // State determines color and behavior
    boolean isStart;
    boolean isEnd;
    boolean isVisited;
    boolean isSeeker;
    boolean isHighlighted;
    String state;

    Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.left = this;
        this.right = this;
        this.top = this;
        this.bottom = this;
        this.isVisited = false;
        this.isStart = false;
        this.isEnd = false;
        this.isSeeker = false;
        this.isHighlighted = false;
    }

    // Return a String representation of this Node.
    public String toString() {
        return "(" + Integer.toString(this.x) + ", "
                + Integer.toString(this.y) + ")";
    }

    // Returns a list of Nodes adjacent to this Node that are not itself.
    ArrayList<Node> getLinkedNodes() {
        ArrayList<Node> result = new ArrayList<Node>();

        if (this.left != this) {
            result.add(this.left);
        }

        if (this.top != this) {
            result.add(this.top);
        }

        if (this.right != this) {
            result.add(this.right);
        }

        if (this.bottom != this) {
            result.add(this.bottom);
        }

        return result;
    }

    // Return this Node drawn accorind to its state and position.
    WorldImage draw() {

        // Determine color.
        Color c;

        if (this.isHighlighted) {
            c = new Color(255, 128, 0);
        }

        else if (this.isStart) {
            c = new Color(0, 204, 0);
        }

        else if (this.isEnd) {
            c = new Color(200, 0, 0);
        }

        else if (this.isSeeker) {
            c = new Color(153, 76, 0);
        }

        else if (this.isVisited) {
            c = new Color(255, 178, 102);
        }

        else {
            c = new Color(255, 255, 255);
        }

        // Determine size and position.
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;

        if (this.bottom != this) {
            bottom = 2;
        }
        if (this.top != this) {
            top = 2;
        }
        if (this.left != this) {
            left = 2;
        }
        if (this.right != this) {
            right = 2;
        }

        return new RectangleImage(new Posn((this.x * MazeGame.NODE_SIZE)
                + right / 2 - left / 2 + MazeGame.HALF_NODE_SIZE,
                (this.y * MazeGame.NODE_SIZE) + bottom / 2 - top / 2
                        + MazeGame.HALF_NODE_SIZE), MazeGame.NODE_SIZE - 4
                + left + right, MazeGame.NODE_SIZE - 4 + top + bottom, c);

    }
}

// To represent a weighted, undirected edge between two nodes.
class Edge {
    int weight;
    Node a;
    Node b;

    Edge(Node a, Node b, int weight) {
        this.weight = weight;
        this.a = a;
        this.b = b;
    }

    // Convenience constructor for Edges where weight is negligible.
    Edge(Node a, Node b) {
        this(a, b, -1);
    }
}

// To represent a Maze Game.
class MazeGame extends World {
    // Constants
    static final int BOARD_WIDTH = 4; // in Nodes
    static final int BOARD_HEIGHT = 4; // in Nodes
    static final int HALF_BOARD_WIDTH = BOARD_WIDTH / 2; // in Nodes
    static final int HALF_BOARD_HEIGHT = BOARD_HEIGHT / 2; // in Nodes
    static final int NODE_SIZE = 30; // in px
    static final int HALF_NODE_SIZE = NODE_SIZE / 2; // in px

    // The matrix of this maze's nodes.
    ArrayList<ArrayList<Node>> map;
    // The list of weighted edges as extracted from the node matrix.
    ArrayList<Edge> edges;

    Random rand = new Random();

    // Determines the mode of the game:
    // - DFS
    // - BFS
    // - human
    String gameMode;

    // The Node of the maze that represents the end.
    Node end;
    Node start;

    // The Node that is being controlled by the human.
    Node seeker;
    // The worklist Stack in a depth first search,
    // and the worklist Queue in a breadth first search.
    ArrayDeque<Node> worklist;
    // The Node that is actively searching in a depth first search.
    Node seekerDFS;

    // Hashmap to keep track of where the seeker has been in order to display
    // the shortest path.
    HashMap<Node, Edge> traceBack;

    // The total number of moves made by an automatic search.
    int totalMoves;
    // The total number of correct moves made by an automatic search;
    // this number only becomes non-zero when the search has finished.
    int correctMoves;

    // Main constructor. Pass a negative seed for true random.
    // SIDEEFFECT: Initializes all the components of this game based on
    // passed mode argument.
    MazeGame(String mode, int seed) {
        if (seed >= 0) {
            this.rand = new Random(seed);
        }

        if (mode.equals("DFS") || mode.equals("BFS") || mode.equals("human")) {
            this.gameMode = mode;
            this.totalMoves = 0;

            // Create a new maze
            this.initMaze();
            this.linkNodes(this.kruskal(this.edges, this.map));

            // Initialize game play necessities.
            this.start = this.map.get(0).get(0);
            this.end = this.map.get(BOARD_HEIGHT - 1).get(BOARD_WIDTH - 1);
            this.seeker = start;
            this.worklist = new ArrayDeque<Node>();
            this.traceBack = new HashMap<Node, Edge>();

            // Set initial states based on selected game mode
            if (mode.equals("DFS")) {
                this.initDFS(this.start);
            }
            else if (mode.equals("BFS")) {
                this.initBFS(this.start);
            }
        }
        else {
            throw new RuntimeException(
                    "Constructed MazeGame with an invalid mode setting.");
        }
    }

    // Convenience constructor for a specific game mode without seeding.
    MazeGame(String mode) {
        this(mode, -1);
    }

    // Convenience constructor for a human-controlled game.
    MazeGame() {
        this("human", -1);
    }

    // Initializes this maze, which is a matrix of Nodes.
    // SIDEEFFECT: Updates this edges and this map to their populated states.
    void initMaze() {
        Utils utils = new Utils();

        ArrayList<ArrayList<Node>> tempMatrix = this.buildMatrix();
        ArrayList<Edge> edges = this.extractEdges(tempMatrix);
        utils.sort(edges);

        this.edges = edges;
        this.map = tempMatrix;
    }

    // Creates a matrix of nodes based on the sizes of this game.
    ArrayList<ArrayList<Node>> buildMatrix() {
        ArrayList<ArrayList<Node>> result = new ArrayList<ArrayList<Node>>();

        for (int indexY = 0; indexY < BOARD_HEIGHT; indexY = indexY + 1) {
            ArrayList<Node> tempX = new ArrayList<Node>();

            for (int indexX = 0; indexX < BOARD_WIDTH; indexX = indexX + 1) {
                tempX.add(new Node(indexX, indexY));
            }

            result.add(tempX);
        }

        result.get(0).get(0).isStart = true;
        result.get(BOARD_HEIGHT - 1).get(BOARD_WIDTH - 1).isEnd = true;

        return result;
    }

    // Returns a list of the Edges as implied by the connections in the given
    // matrix of Nodes.
    ArrayList<Edge> extractEdges(ArrayList<ArrayList<Node>> matrix) {
        ArrayList<Edge> result = new ArrayList<Edge>();

        for (int indexY = 0; indexY < matrix.size(); indexY = indexY + 1) {
            ArrayList<Node> row = matrix.get(indexY);

            for (int indexX = 0; indexX < row.size(); indexX = indexX + 1) {
                Node n = row.get(indexX);

                if (n.x != BOARD_WIDTH - 1) {
                    // Right
                    result.add(new Edge(n, row.get(indexX + 1), this.rand
                            .nextInt(100)));
                }
                if (n.y != BOARD_HEIGHT - 1) {
                    // Bottom
                    result.add(new Edge(n,
                            matrix.get(indexY + 1).get(indexX), this.rand
                                    .nextInt(100)));
                }
            }
        }

        return result;
    }

    // Takes a list of Nodes and list of Edges with weights for those nodes and
    // returns a list of Edges that are representative of what is actually to be
    // used for this game's spanning tree.
    ArrayList<Edge> kruskal(ArrayList<Edge> edges,
            ArrayList<ArrayList<Node>> nodes) {
        HashMap<String, String> reps = new HashMap<String, String>();
        ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
        Utils utils = new Utils();

        // Init all representations to themselves.
        for (ArrayList<Node> arr : nodes) {
            for (Node n : arr) {
                reps.put(n.toString(), n.toString());
            }
        }

        for (int index = 1; index < edges.size(); index = index + 1) {
            if (!utils.find(reps, edges.get(index).a.toString()).equals(
                    utils.find(reps, (edges.get(index).b.toString())))) {
                edgesInTree.add(edges.get(index));
                utils.union(reps, edges.get(index).a.toString(),
                        edges.get(index).b.toString());
            }
        }

        return edgesInTree;
    }

    // Walks over a list of Edges and links their corresponding nodes to
    // eachother appropriately.
    // SIDEEFFECT: Updates the top, bottom, left, and right fields of
    // the Nodes in the given list of Edges.
    void linkNodes(ArrayList<Edge> edges) {
        for (Edge edge : edges) {
            // Above
            if (edge.a.x == edge.b.x && edge.a.y < edge.b.y) {
                edge.a.bottom = edge.b;
                edge.b.top = edge.a;
            }
            // Below
            else if (edge.a.x == edge.b.x && edge.a.y > edge.b.y) {
                edge.b.bottom = edge.a;
                edge.a.top = edge.b;
            }
            // Left
            else if (edge.a.x < edge.b.x && edge.a.y == edge.b.y) {
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

    // ///////////////////////////////////////////////////////////////////////
    // /// FROM HERE DOWN RESIDES GAMEPLAY FUNCTIONS
    // ///////////////////////////////////////////////////////////////////////

    // Initiates an automated depth first search of this maze, starting
    // at the given Node.
    // SIDEEFFECT: Sets this worklist, used as a Stack, and this seekerDFS.
    void initDFS(Node start) {
        ArrayDeque<Node> worklist = new ArrayDeque<Node>();
        worklist.push(start);

        this.worklist = worklist;
        this.seekerDFS = start;
    }

    // Steps the automated depth first search of this maze, to be
    // called by this onTick().
    // SIDEEFFECT: Updates this worklist, this seekerDFS, and the
    // states of seeking nodes.
    void stepDFS() {
        // Pop the next node to become the seeker.
        Node nextSeeker = this.worklist.pop();

        // Update states.
        this.seekerDFS.isVisited = true;
        nextSeeker.isSeeker = true;
        this.seekerDFS.isSeeker = false;

        this.seekerDFS = nextSeeker;

        // Push to the worklist all, if any, of the adjacent
        // nodes of the new seeker.
        for (Node n : nextSeeker.getLinkedNodes()) {
            if (!n.isVisited) {
                this.trace(n, nextSeeker);
                worklist.push(n);
            }
        }
    }

    // Initiates an automated breadth first search of this maze, starting
    // at the given Node.
    // SIDEEFFECT: Sets this worklist, used as a Queque
    void initBFS(Node start) {
        this.worklist.addFirst(start);
    }

    // Steps the automated breath first search of this maze, to be
    // called by this onTick().
    // SIDEEFFECT: Updates this worklist and the states of seeking
    // Nodes
    void stepBFS() {
        Node lastSeeker = this.worklist.pop();

        lastSeeker.isVisited = true;
        lastSeeker.isSeeker = false;

        for (Node n : lastSeeker.getLinkedNodes()) {
            if (!n.isVisited) {
                n.isSeeker = true;
                worklist.addLast(n);
                this.trace(n, lastSeeker);
            }
        }
    }

    // Move the human player to the given node.
    // SIDEEFFECT: Updates the seeker.
    void stepHuman(Node to) {
        if (this.seeker != to) {
            this.seeker.isVisited = false;
            to.isSeeker = true;
            this.seeker.isSeeker = false;
        }

        this.seeker = to;
    }

    // Adds the given to and from Nodes to this traceBack HashMap, mapping
    // each given to Node to an Edge consisting of them both.
    // SIDEEFFECT: Updates this traceBack
    void trace(Node to, Node from) {
        this.traceBack.put(to, new Edge(to, from));
    }

    // Walks through this traceBack, starting at the even end Node, updating
    // the states of the Nodes to be highlighted paths.
    // SIDEEFFECT: Updates the state fields of the Nodes in this, and
    // decrements this totalMoves so that on termination this totalMoves
    // effectively represents only the number of incorrect moves.
    void reconstruct(Node end) {
        this.correctMoves = this.correctMoves + 1;

        Edge e = this.traceBack.get(end);

        e.b.isHighlighted = true;

        Node next = e.b;

        if (next != this.start) {
            this.reconstruct(next);
        }

        else {
            this.end.isHighlighted = true;
        }
    }

    // Operates the world on each tick, based on the mode.
    public World onTick() {
        this.totalMoves = this.totalMoves + 1;

        if (this.gameMode.equals("DFS")) {
            this.stepDFS();
        }
        else if (this.gameMode.equals("BFS")) {
            this.stepBFS();
        }

        return this;
    }

    // Handle key events.
    public World onKeyEvent(String ke) {

        if (ke.equals("b")) {
            return new MazeGame("BFS");
        }
        else if (ke.equals("d")) {
            return new MazeGame("DFS");
        }
        else if (ke.equals("h")) {
            return new MazeGame("human");
        }

        if (this.gameMode.equals("human")) {
            this.totalMoves = this.totalMoves + 1;

            if (ke.equals("up")) {
                this.stepHuman(this.seeker.top);
            }
            else if (ke.equals("down")) {
                this.stepHuman(this.seeker.bottom);
            }
            else if (ke.equals("left")) {
                this.stepHuman(this.seeker.left);
            }
            else if (ke.equals("right")) {
                this.stepHuman(this.seeker.right);
            }
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

            for (Node n : arr) {
                // Overlay.
                this.background = new OverlayImages(this.background, n.draw());
            }
        }

        // If there is an available score to display, do so.
        if (this.correctMoves > 0) {
            this.background = new OverlayImages(
                    this.background,
                    new TextImage(new Posn(HALF_BOARD_WIDTH * NODE_SIZE,
                            HALF_BOARD_HEIGHT * NODE_SIZE),
                            "INCORRECT MOVES MADE : "
                                    + Integer.toString(this.totalMoves
                                            - this.correctMoves), Color.green));
        }

        return background;
    }

    // Check if any of the seeker Nodes have the "end" state.
    public WorldEnd worldEnds() {
        WorldEnd state = new WorldEnd(false, this.background);

        if (this.gameMode.equals("human")) {
            if (this.seeker == this.end) {
                state = new WorldEnd(true, new OverlayImages(
                        this.makeImage(), new TextImage(new Posn(BOARD_WIDTH,
                                BOARD_HEIGHT), "GAME OVER", Color.red)));
            }
        }

        else if (this.gameMode.equals("BFS")) {
            for (Node n : this.worklist) {
                if (n == this.end) {
                    this.reconstruct(this.end);

                    state = new WorldEnd(true, new OverlayImages(
                            this.makeImage(), new TextImage(new Posn(
                                    BOARD_WIDTH, BOARD_HEIGHT), "GAME OVER",
                                    Color.red)));
                }
            }
        }

        else if (this.gameMode.equals("DFS")) {
            if (this.seekerDFS == this.end) {
                this.reconstruct(this.end);

                state = new WorldEnd(true, new OverlayImages(
                        this.makeImage(), new TextImage(new Posn(BOARD_WIDTH,
                                BOARD_HEIGHT), "GAME OVER", Color.red)));
            }
        }

        return state;
    }
}

// Represent a big red toolbox of utilities!
class Utils {
    // Effect swap the values at 2 indices in given array
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
            if (minSoFar.weight >= arr.get(index).weight) {
                minSoFar = arr.get(index);
                indexOfMin = index;
            }
        }
        return indexOfMin;
    }

    // Sorts the given list of Edges by weight, smallest to largest.
    // SIFEEFFECT: Changes the arrangement of the given arr
    void sort(ArrayList<Edge> arr) {
        for (int index = 0; index < arr.size(); index = index + 1) {
            int indexOfMin = this.findMin(arr, index);
            this.swap(arr, indexOfMin, index);
        }
    }

    // Find and return the given key's last representative in the given hashMap.
    String find(HashMap<String, String> hashMap, String key) {
        String k = hashMap.get(key);

        if (k.equals(key)) {
            return k;
        }
        else {
            return this.find(hashMap, k);
        }
    }

    // Sets rep1's final representative in the given hashMap to rep2's.
    void union(HashMap<String, String> hashMap, String rep1, String rep2) {
        hashMap.put(this.find(hashMap, rep1), this.find(hashMap, rep2));
    }
}

class ExamplesMaze {
    MazeGame game;

    void setupHuman() {
        this.game = new MazeGame("human", 123);
    }

    void setupDFS() {
        this.game = new MazeGame("DFS", 123);
    }

    void setupBFS() {
        this.game = new MazeGame("BFS", 123);
    }

    void setup() {
        this.game = new MazeGame();
    }

    void testConstructor(Tester t) {
        this.setup();
        t.checkExpect(this.game.edges.size(), 24);
    }

    void testBuildMatrix(Tester t) {
        this.setup();
        ArrayList<ArrayList<Node>> s = this.game.buildMatrix();

        t.checkExpect(this.game.map.get(0).get(0) == s.get(0).get(0), false);
        t.checkExpect(this.game.map.size() == s.size(), true);
        t.checkExpect(this.game.map.get(0).size() == s.get(0).size(), true);
    }

    void testKuskal(Tester t) {
        this.setup();
        t.checkExpect(this.game.kruskal(this.game.edges, this.game.map)
                .size(), 15);
    }

    void testUtils(Tester t) {
        ArrayList<Edge> test = new ArrayList<Edge>(Arrays.asList(new Edge(
            new Node(1, 1), new Node(1, 1), 10), new Edge(new Node(1, 1),
                new Node(1, 1), 9), new Edge(new Node(1, 1), new Node(1, 1),
                    5), new Edge(new Node(1, 1), new Node(1, 1), 15)));
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

        utils.union(hash, "A", "C");
        t.checkExpect(hash.get("A"), "C");
        utils.union(hash, "B", "E");
        t.checkExpect(hash.get("B"), "C");
        t.checkExpect(hash.get("C"), "C");
        utils.union(hash, "E", "C");
        t.checkExpect(hash.get("C"), "C");
    }

    void testSearch(Tester t) {
        // Testing Depth First Search
        setupDFS();
        this.game.onTick();
        t.checkExpect(this.game.map.get(1).get(0).isVisited, false);
        this.game.onTick();
        t.checkExpect(this.game.map.get(1).get(0).isSeeker, true);
        this.game.onTick();
        t.checkExpect(this.game.map.get(1).get(0).isVisited, true);

        // At this point the seeker has the choice of right or down, it chooses
        // Down right stays unvisited
        t.checkExpect(this.game.map.get(2).get(1).isVisited, false);
        t.checkExpect(this.game.map.get(1).get(2).isVisited, false);
        this.game.onTick();
        t.checkExpect(this.game.map.get(2).get(1).isSeeker, true);
        t.checkExpect(this.game.map.get(1).get(2).isVisited, false);

        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();

        // After 9 ticks the search has reached the end and reconstruct
        // sets the correct nodes to highlighted
        this.game.reconstruct(this.game.end);

        t.checkExpect(this.game.map.get(3).get(3).isHighlighted, true);
        t.checkExpect(this.game.map.get(3).get(2).isHighlighted, true);
        t.checkExpect(this.game.map.get(0).get(0).isHighlighted, true);

        // this node was visited but not in the shortest path so is not
        // highlighted
        t.checkExpect(!this.game.map.get(2).get(2).isHighlighted
                && this.game.map.get(2).get(2).isVisited, true);

        // Testing Breadth First Search
        setupBFS();
        t.checkExpect(this.game.map.get(1).get(0).isVisited, false);
        this.game.onTick();
        t.checkExpect(this.game.map.get(1).get(0).isSeeker, true);
        this.game.onTick();
        t.checkExpect(this.game.map.get(1).get(0).isVisited, true);

        // At this point the seeker has the choice of right or down, it
        // chooses both right and down creating a second seeker
        t.checkExpect(this.game.map.get(1).get(2).isVisited, false);
        t.checkExpect(this.game.map.get(2).get(1).isVisited, false);
        this.game.onTick();
        t.checkExpect(this.game.map.get(1).get(2).isSeeker, true);
        t.checkExpect(this.game.map.get(2).get(1).isSeeker, true);

        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();

        // After 15 ticks the search has reached the end and reconstruct
        // sets the correct nodes to highlighted
        this.game.reconstruct(this.game.end);

        t.checkExpect(this.game.map.get(3).get(3).isHighlighted, true);
        t.checkExpect(this.game.map.get(3).get(2).isHighlighted, true);
        t.checkExpect(this.game.map.get(0).get(0).isHighlighted, true);

        // this node was visited but not in the shortest path so is not
        // highlighted
        t.checkExpect(!this.game.map.get(2).get(2).isHighlighted
                && this.game.map.get(2).get(2).isVisited, true);

    }

    void testMove(Tester t) {

        // Test down, by showing the tile below our seeker changes to
        // a seeker and shows the previous seeker is no longer a seeker
        setupHuman();
        boolean initialState = this.game.map.get(1).get(0).isSeeker;
        this.game.onKeyEvent("down");
        t.checkExpect(this.game.map.get(1).get(0).isSeeker != initialState
                && this.game.map.get(1).get(0).isSeeker, true);
        t.checkExpect(this.game.map.get(0).get(0).isSeeker, false);

        // Test right, by showing the tile to the right of our seeker changes
        // to a seeker, and shows the previous seeker is no longer a seeker
        setupHuman();
        initialState = this.game.map.get(1).get(1).isSeeker;
        this.game.onKeyEvent("down");
        this.game.onKeyEvent("right");
        t.checkExpect(this.game.map.get(1).get(1).isSeeker != initialState
                && this.game.map.get(1).get(1).isSeeker, true);
        t.checkExpect(this.game.map.get(1).get(0).isSeeker, false);

        // Test left, by showing the tile to the right of our seeker changes
        // to a seeker and shows the previous seeker is no longer a seeker
        setupHuman();
        initialState = this.game.map.get(1).get(0).isSeeker;
        this.game.onKeyEvent("down");
        this.game.onKeyEvent("right");
        this.game.onKeyEvent("left");
        t.checkExpect(this.game.map.get(1).get(0).isSeeker != initialState
                && this.game.map.get(1).get(0).isSeeker, true);
        t.checkExpect(this.game.map.get(1).get(1).isSeeker, false);

        // Test up, by showing the tile to the right of our seeker changes
        // to a seeker and shows the previous seeker is no longer a seeker
        setupHuman();
        initialState = this.game.map.get(1).get(1).isSeeker;
        this.game.onKeyEvent("down");
        this.game.onKeyEvent("right");
        this.game.onKeyEvent("down");
        this.game.onKeyEvent("up");
        t.checkExpect(this.game.map.get(1).get(1).isSeeker != initialState
                && this.game.map.get(1).get(1).isSeeker, true);
        t.checkExpect(this.game.map.get(2).get(1).isSeeker, false);
    }

    void testCantMove(Tester t) {

        // Test noUp by showing the up key doesnt move the seeker
        setupHuman();
        this.game.onKeyEvent("down");
        this.game.onKeyEvent("up");
        boolean initialState = this.game.map.get(0).get(0).isSeeker;
        this.game.onKeyEvent("up");
        t.checkExpect(this.game.map.get(0).get(0).isSeeker == initialState
                && this.game.map.get(0).get(0).isSeeker, true);

        // Test noDown by showing the down key doesn't move the seeker
        setupHuman();
        this.game.onKeyEvent("down");
        initialState = this.game.map.get(1).get(0).isSeeker;
        this.game.onKeyEvent("down");
        t.checkExpect(this.game.map.get(1).get(0).isSeeker == initialState
                && this.game.map.get(1).get(0).isSeeker, true);

        // Test noLeft by showing the left key doesn't move the seeker
        setupHuman();
        this.game.onKeyEvent("down");
        initialState = this.game.map.get(1).get(0).isSeeker;
        this.game.onKeyEvent("left");
        t.checkExpect(this.game.map.get(1).get(0).isSeeker == initialState
                && this.game.map.get(1).get(0).isSeeker, true);

        // Test noRight by showing the right key doesn't move the seeker
        setupHuman();
        this.game.onKeyEvent("down");
        this.game.onKeyEvent("up");
        initialState = this.game.map.get(0).get(0).isSeeker;
        this.game.onKeyEvent("right");
        t.checkExpect(this.game.map.get(0).get(0).isSeeker == initialState
                && this.game.map.get(0).get(0).isSeeker, true);

    }

    // RUN THE GAME
    MazeGame w = new MazeGame("human");
    boolean runAnimated = this.w.bigBang(1000, 600, 1);

}