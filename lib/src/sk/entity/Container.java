package sk.entity;

import java.util.ArrayList;
import java.util.List;

public class Container extends Node {
	
	private ArrayList<Node> nodes;
	
	/**
	 * 
	 * Creates a new empty node container.
	 * 
	 */
	public Container() {
		nodes = new ArrayList<>();
	}
	
	@Override
	public void update(double delta) {
		for(Node node : nodes)
			node.update(delta);
	}
	
	@Override
	public void draw() {
		for(Node node : nodes)
			node.draw();
	}
	
	/**
	 * 
	 * Adds a node to this container.
	 * 
	 * @param node the node to be added.
	 * @return this container instance.
	 */
	public Container add(Node node) {
		nodes.add(node);
		
		return this;
	}
	
	/**
	 * 
	 * Checks whether or not the specified node exists within this container.
	 * 
	 * @param node the node to check.
	 * @return {@code true} if the node exists.
	 */
	public boolean has(Node node) {
		return nodes.contains(node);
	}
	
	/**
	 * 
	 * Returns the node at the provided index.
	 * 
	 * @param i the index.
	 * @return the node at the provided index.
	 */
	public Node get(int i) {
		return nodes.get(i);
	}
	
	/**
	 * 
	 * Returns the index of the specified node.
	 * 
	 * @param node the node.
	 * @return the index of the specified node.
	 */
	public int getIndex(Node node) {
		for(int i = 0; i < nodes.size(); i++)
			if(nodes.get(i) == node)
				return i;
		
		return -1;
	}
	
	/**
	 * 
	 * Returns a list with all nodes that currently exists in this container.
	 * 
	 * @return a list containing all nodes.
	 */
	@SuppressWarnings("unchecked")
	public List<Node> getNodes() {
		return (ArrayList<Node>) nodes.clone();
	}
	
	/**
	 * 
	 * Removes the node at the specified index.
	 * 
	 * @param i the index.
	 * @return the container instance.
	 */
	public Container remove(int i) {
		nodes.remove(i);
		
		return this;
	}
	
	/**
	 * 
	 * Returns the number of nodes that currently exist in this container.
	 * 
	 * @return the number of nodes.
	 */
	public int getNumOfNodes() {
		return nodes.size();
	}
	
	@Override
	public void destroy() {
		for(Node n : nodes)
			n.destroy();
		nodes.clear();
	}
}