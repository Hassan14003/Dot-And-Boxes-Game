package com.dotsandboxes.game.models;

import java.util.ArrayList;


public class Node {
    public String board;           // name used for hashing
    public ArrayList<Edge> links;  // the links to child nodes
    public int value;              // the score value of the node

    public Node(String board, int value) {
        this.links = new ArrayList<>();
        this.value = value;
        this.board = board;
    }
}
