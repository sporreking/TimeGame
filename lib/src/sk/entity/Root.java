package sk.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class Root extends Node {
	
	private TreeMap<Integer, List<String>> priorities;
	private HashMap<String, Node> nodes;
	
	/**
	 * 
	 * Creates a new empty root.
	 * 
	 */
	public Root() {
		priorities = new TreeMap<>();
		nodes = new HashMap<>();
	}
	
	@Override
	public void update(double delta) {
		for(List<String> keys : priorities.values())
			for(String key : keys)
				nodes.get(key).update(delta);
	}
	
	@Override
	public void draw() {
		for(List<String> keys : priorities.values())
			for(String key : keys)
				nodes.get(key).draw();
	}
	
	/**
	 * 
	 * Adds a new node to this root.
	 * 
	 * @param priority the lowest priority will be updated first.
	 * @param key the key to associate the node with.
	 * @param node the node to be added.
	 * @return this root instance.
	 */
	public Root add(int priority, String key, Node node) {
		if(nodes.containsKey(key))
			throw new IllegalArgumentException("A node with the key \""
					+ key + "\" already exists in this root");
		
		if(priorities.get(priority) == null)
			priorities.put(priority, new ArrayList<String>());
		
		priorities.get(priority).add(key);
		nodes.put(key, node);
		
		return this;
	}
	
	/**
	 * 
	 * Checks whether or not this a node with the specified key exists within this root.
	 * 
	 * @param key the key to check.
	 * @return {@code true} if the a matching node exists.
	 */
	public boolean has(String key) {
		return nodes.containsKey(key);
	}
	
	/**
	 * 
	 * Returns the node with the specified key, or {@code null} if no such node exists.
	 * 
	 * @param key the key to fetch with.
	 * @return the node with the specified key, or {@code null} if no such node exists.
	 */
	public Node get(String key) {
		return nodes.get(key);
	}
	
	/**
	 * 
	 * Returns the priority of the node with the specified key.
	 * 
	 * @param key the key of the node.
	 * @return the priority of the node.
	 */
	public int getPriority(String key) {
		for(int i : priorities.keySet())
			if(priorities.get(i).contains(key))
				return i;
		
		throw new IllegalArgumentException("There is no node with the key \""
				+ key + "\" in this root");
	}
	
	/**
	 * 
	 * Returns a list containing all keys of the nodes with the specified priorities.
	 * 
	 * @param priorities the priorities to fetch with.
	 * @return a list containing the keys.
	 */
	public List<String> getKeys(int... priorities) {
		List<String> keys = new ArrayList<>();
		
		for(int i : priorities) {
			if(this.priorities.get(i) == null)
				throw new IllegalArgumentException("There is no node with the priority \""
						+ i + "\" in this root");
			
			for(String key : this.priorities.get(i))
				keys.add(key);
		}
		
		return keys;
	}
	
	/**
	 * 
	 * Returns a list containing all nodes with the specified priorities.
	 * 
	 * @param priorities the priorities to fetch with.
	 * @return a list containing the nodes.
	 */
	public List<Node> getNodes(int... priorities) {
		List<Node> nodes = new ArrayList<>();
		
		for(int i : priorities) {
			if(this.priorities.get(i) == null)
				throw new IllegalArgumentException("There is no node with the priority \""
						+ i + "\" in this root");
			
			for(String key : this.priorities.get(i))
				nodes.add(this.nodes.get(key));
		}
		
		return nodes;
	}
	
	/**
	 * 
	 * Returns a list containing all keys of this root.
	 * 
	 * @return all keys of this root.
	 */
	public List<String> getKeys() {
		List<String> keys = new ArrayList<>();
		
		for(String key : nodes.keySet())
			keys.add(key);
		
		return keys;
	}
	
	/**
	 * 
	 * Returns a list containing all node of this root.
	 * 
	 * @return all nodes of this root.
	 */
	public List<Node> getNodes() {
		List<Node> nodes = new ArrayList<>();
		
		for(Node node : this.nodes.values())
			nodes.add(node);
		
		return nodes;
	}
	
	/**
	 * 
	 * Removes the node with the specified key from this root.
	 * 
	 * @param key the key of the node to remove.
	 * @return this root instance.
	 */
	public Root remove(String key) {
		for(int i = 0; i < priorities.size(); i++) {
			List<String> list = priorities.get(i);
			for(int j = 0; j < list.size(); j++) {
				if(list.get(j).equals(key)) {
					priorities.get(i).remove(j);
					nodes.remove(key);
					return this;
				}
			}
		}
		
		throw new IllegalArgumentException("There is no node with the key \""
				+ key + "\" in this root");
	}
	
	/**
	 * 
	 * Returns the number of nodes contained by this root.
	 * 
	 * @return the number of nodes.
	 */
	public int getNumOfNodes() {
		return nodes.size();
	}
	
	@Override
	public void destroy() {
		for(List<String> keys : priorities.values())
			for(String key : keys)
				nodes.get(key).destroy();
		
		priorities.clear();
		nodes.clear();
	}

	/**
	 * Calls a get, but automatically casts it to an entity
	 * @param string the name of the entity
	 * @return the node casted to an entity
	 */
	public Entity gete(String string) {
		return (Entity) get(string);
	}
}