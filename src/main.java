// Assignment 10
// Martin John
// jmartin4
// Ben Campbell
// campb

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
    // the four adjacent cells to this one
    Edge left;
    Edge top;
    Edge right;
    Edge bottom;
    
    Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.left = new NullEdge(this);
        this.top = new NullEdge(this);
        this.right = new NullEdge(this);
        this.bottom = new NullEdge(this);
    }
    
}

class Edge {
    int weight;
    Node dest;
    
    Edge(int weight, Node dest) {
        this.weight = weight;
        this.dest = dest;
    }
}

class NullEdge extends Edge {
    
    NullEdge(Node dest) {
        super(-1, dest);
    }
}

class MazeGame {
    // Constants
    static final int BOARD_WIDTH = 64; // in Nodes
    static final int BOARD_HEIGHT = 64; // in Nodes
    static final int NODE_SIZE = 20; // in px
    
    ArrayList<ArrayList<Node>> map;
    
    void initMap() {
        ArrayList<ArrayList<Node>> tempMatrix = this.buildMatrix();
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
    
    ArrayList<ArrayList<Node>> massageMatrix(ArrayList<ArrayList<Node>>
        before) {
        
        for (int indexY = 0; indexY < BOARD_HEIGHT; indexY = indexY + 1) {
            ArrayList<Node> tempX = new ArrayList<Node>();

            for (int indexX = 0; indexX < BOARD_WIDTH; indexX = indexX + 1) {
                tempX.add(new Node(indexX, indexY));   
            }
            
            result.add(tempX);
        }
    }
}