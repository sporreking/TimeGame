package game.level.resources;

import sk.entity.Component;

public abstract class Connectable extends Component {

	// Does something when the trigger is pressed
	public abstract void pressed();
	
	// Does something when the trigger is released
	public abstract void released();
	
}
