// Assignment 10
// Martin John
// jmartin4
// Ben Campbell
// nampb

import java.awt.Color;
import java.util.ArrayList;
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
    int halfWeight;
    // the four adjacent cells to this one
    Node left;
    Node top;
    Node right;
    Node bottom;
    
    Node(int x, int y, int weight) {
        this.x = x;
        this.y = y;
        this.halfWeight = weight;
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
    static final int BOARD_WIDTH = 64; // in Nodes
    static final int BOARD_HEIGHT = 64; // in Nodes
    static final int NODE_SIZE = 20; // in px
    
    ArrayList<ArrayList<Node>> map;
    
    MazeGame() {
        
    }
    
    void initMaze() {
        ArrayList<ArrayList<Node>> tempMatrix = this.buildMatrix();
        tempMatrix = this.massageMatrix(tempMatrix);
        ArrayList<Edge> edges = this.extractEdges(tempMatrix);
    }
    
    ArrayList<ArrayList<Node>> buildMatrix() {
        ArrayList<ArrayList<Node>> result =
                new ArrayList<ArrayList<Node>>();
        Random rand = new Random();
        
        for (int indexY = 0; indexY < BOARD_HEIGHT; indexY = indexY + 1) {
            ArrayList<Node> tempX = new ArrayList<Node>();

            for (int indexX = 0; indexX < BOARD_WIDTH; indexX = indexX + 1) {
                tempX.add(new Node(indexX, indexY, rand.nextInt(100)));
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
        
        ArrayList<Edge> result = new ArrayList<Edge>();
        
        for (ArrayList<Node> arr : matrix) {
            for (Node n : arr) {
                if(n.x != BOARD_WIDTH) {
                    // Right
                    result.add(new Edge(n, n.right, n.halfWeight + n.right.halfWeight));
                }
                if(n.y != BOARD_HEIGHT) {
                    // Bottom
                    result.add(new Edge(n, n.bottom, n.halfWeight + n.bottom.halfWeight));
                }
            }
        }
        
        return result;
    }
    
}