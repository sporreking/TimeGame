package editor;

public class FixedStack<T> {
	private T[] stack;
	private int size;
	private int top;

	public FixedStack(int size) {
		this.stack = (T[]) new Object[size];
		this.top = -1;
		this.size = size;
	}
	
	public void push(T obj) {
		if (top >= size - 1) {
			
			T[] temp = (T[]) new Object[size];
			
			for(int i = 0; i < size - 1; i++) {
				temp[i] = stack[i + 1];
			}
			
			stack = temp;
			
			stack[size - 1] = obj;
			
		} else {
			stack[++top] = obj;
		}
	}
	
	public T pop() {
		if (top < 0) return null;
		T obj = stack[top--];
		stack[top + 1] = null;
		return obj;
	}
	
	public int size() {
		return size;
	}
	
	public int elements() {
		return top + 1;
	}
}