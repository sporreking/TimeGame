package sk.gfx;

import java.util.ArrayList;

import sk.util.vector.Vector;


@SuppressWarnings("rawtypes")
public abstract class Vertex {
	
	protected ArrayList<Vector> components = new ArrayList<>();
	
	/**
	 * 
	 * Fetches all values from this vertex in component-order.
	 * 
	 * @return all values from this vertex in component-order.
	 */
	public float[] getData() {
		float[] data = new float[getLength()];
		
		int offset = 0;
		
		for(int i = 0; i < components.size(); i++) {
			for(int j = 0; j < components.get(i).getData().length; j++)
				data[offset + j] = components.get(i).get(j);
			
			offset += getLength(i);
		}
		
		return data;
	}
	
	/**
	 * 
	 * Gets the length of all components combined.
	 * 
	 * @return the length of all components combined.
	 */
	public int getLength() {
		int length = 0;
		for(Vector v : components)
			length += v.numValues();
		
		return length;
	}
	
	/**
	 * 
	 * Returns the number of components used by this vertex.
	 * 
	 * @return the number of components used by this vertex.
	 */
	public int getNumComponents() {
		return components.size();
	}
	
	/**
	 * 
	 * Fetches all values contained in the specified component.
	 * 
	 * @param component the index of the component to fetch from.
	 * @return all values contained in the specified component.
	 */
	public float[] getData(int component) {
		return components.get(component).getData();
	}
	
	/**
	 * 
	 * Returns the length of the specified component.
	 * 
	 * @param component the index of the component whose length to fetch.
	 * @return the length of the specified component.
	 */
	public int getLength(int component) {
		return components.get(component).numValues();
	}
	
	/**
	 * 
	 * Returns the size of the specified component in bytes.
	 * 
	 * @param component the index of the component whose size to return.
	 * @return the size of the specified component in bytes.
	 */
	public int bytes(int component) {
		return components.get(component).bytes();
	}
}