package com.utils.firebase.model;

import java.util.ArrayList;

/**
 * Root:
 * Parent:
 * Child
 * Child
 * Child:
 * Child of Child
 * Child of Child
 * <p>
 * FireNode = root;
 * <p>
 * root.searchNode(["temp","user",""]);
 */
public class FireNode {
    private ArrayList<FireNode> children;
    private FireNode parent;
    private String key;
    private String value;
    private String path;

    public FireNode(String key) {
        this.children = new ArrayList<>();
        this.key = key;
    }

    public static void printTree(FireNode source) {
        for (FireNode data : source.getChildren()) {
            printTree(data);
        }
    }

    @Override
    public String toString() {
        return this.key;
    }

    public String getPath() {
        if (this.isRoot())
            path = "Root";
        return this.path;
    }

    public void setPath(String pathToNode) {
        this.path = pathToNode;
    }

    public ArrayList<FireNode> getChildren() {
        return children;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public void addChild(FireNode node) {
        node.setParent(this);
        node.path = this.getPath() + "/" + node.getKey();
        this.children.add(node);
    }

    public FireNode getParent() throws Exception {
        if (this.parent == null)
            throw new Exception("Node has no parent");
        return this.parent;
    }

    private void setParent(FireNode parent) {
        this.parent = parent;
    }

    public boolean hasChildren() {
        return this.getChildren().size() > 0;
    }

    public FireNode getRoot() {
        FireNode currentNode = this;
        // get root object
        while (currentNode.parent != null) {
            currentNode = currentNode.parent;
        }
        return currentNode;
    }

    public FireNode searchNode(String... key) {
        FireNode root = this.getRoot();

        // traverse down the tree to find the node with the key
        return this.find(root.getChildren(), key[0]);
    }

    private FireNode find(ArrayList<FireNode> children, String key) {
        for (FireNode fd : children) {
            if (fd.key.equals(key))
                return fd;
            else
                return find(fd.getChildren(), key);
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
