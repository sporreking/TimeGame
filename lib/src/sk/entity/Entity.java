package sk.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import sk.gfx.Renderer;

public class Entity extends Node {
	
	private TreeMap<Integer, ArrayList<Component>> components;
	
	/**
	 * 
	 * Creates a new empty entity.
	 * 
	 */
	public Entity() {
		components = new TreeMap<>();
	}
	
	@Override
	public void update(double delta) {
		for(int i : components.keySet())
			for(Component c : components.get(i))
				c.update(delta);
	}
	
	@Override
	public void draw() {
		for(int i : components.keySet())
			for(Component c : components.get(i))
				c.draw();
	}
	
	/**
	 * 
	 * Checks whether or not a component of the specified type is attached to this entity.
	 * 
	 * @param c the component type to check.
	 * @return {@code true} if the component is attached.
	 */
	public <T extends Component> boolean has(Class<T> c) {
		for(List<Component> comps : components.values())
			for(Component comp : comps)
				if(comp.getClass() == c)
					return true;
		
		return false;
	}
	
	/**
	 * 
	 * Adds a new component to this entity.
	 * 
	 * @param priority the lowest priority will be updated first.
	 * @param comp the component to add.
	 * @return this entity instance.
	 */
	public Entity add(int priority, Component comp) {
		if(has(comp.getClass()))
			throw new IllegalArgumentException("The component ("
						+ comp.getClass().getSimpleName() + ") is already part of this entity");
		
		//Check if the prerequisite components are attached
		if(comp.requirements() != null) {
			ArrayList<String> requirements = new ArrayList<>();
			
			for(Class<? extends Component> req : comp.requirements())
				if(!has(req))
					requirements.add(req.getName());
			
			if(!requirements.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				
				sb.append("\"" + comp.getClass().getSimpleName() + "\" requires"
						+ " the following component(s): ");
				
				for(String c : requirements) {
					
					sb.append(c);
					
					if(requirements.indexOf(c) != requirements.size() - 1)
						sb.append(", ");
				}
				
				throw new IllegalStateException(sb.toString());
			}
		}
		
		if(components.get(priority) == null)
			components.put(priority, new ArrayList<Component>());
		
		components.get(priority).add(comp);
		
		comp.setParent(this);
		
		comp.init();
		
		return this;
	}
	
	/**
	 * 
	 * Returns the component of the specified type, or {@code null} if no such component is present.
	 * 
	 * @param c the type of component to fetch.
	 * @return the component, or {@code null} if it is not present.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Component> T get(Class<T> c) {
		for(List<Component> comps : components.values())
			for(Component comp : comps)
				if(comp.getClass() == c)
					return (T) comp;
		
		return null;
	}
	
	/**
	 * 
	 * Returns all components with the specified priority.
	 * 
	 * @param priority the priority to fetch from.
	 * @return all components with the specified priority.
	 */
	@SuppressWarnings("unchecked")
	public List<Component> getComponents(int priority) {
		if(components.get(priority) == null)
			throw new IllegalArgumentException("There are no components with the priority \""
					+ priority + "\"");
		
		return (List<Component>) components.get(priority).clone();
	}
	
	/**
	 * 
	 * Returns the priority of the specified component.
	 * 
	 * @param c the type of the component.
	 * @return the priority of the component or {@code -1}.
	 */
	public int getPriority(Class<? extends Component> c) {
		for(int i : components.keySet()) {
			for(Component comp : getComponents(i))
				if(comp.getClass() == c)
					return i;
		}
		
		return -1;
	}
	
	/**
	 * 
	 * Removes the component of the specified type.
	 * 
	 * @param c the type of the component.
	 * @return this entity instance.
	 */
	public Entity remove(Class<? extends Component> c) {
		if(!has(c))
			throw new IllegalArgumentException("The component \"" + c.getSimpleName()
					+ "\" is not a part of this entity");
		
		int p = getPriority(c);
		
		for(Component comp : components.get(p)) {
			if(comp.getClass() == c) {
				comp.removeParent();
				components.get(p).remove(comp);
			}
		}
		
		return this;
	}
	
	/**
	 * 
	 * Calls exit on all components and clears itself from them.
	 * 
	 */
	@Override
	public void destroy() {
		for(List<Component> cl : components.values()) {
			for(Component c : cl) {
				c.exit();
				c.removeParent();
			}
		}
		
		components.clear();
	}
}