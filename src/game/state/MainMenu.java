package game.state;

import java.awt.Font;

import game.TG;
import sk.entity.Entity;
import sk.game.Game;
import sk.gamestate.GameState;
import sk.gamestate.GameStateManager;
import sk.gfx.Texture;
import sk.gfx.gui.GUIButton;
import sk.gfx.gui.GUIText;
import sk.sst.SST;

import sk.util.vector.Vector4f;

public class MainMenu implements GameState {
	
	private Entity num[];
	
	public void menu (){
		num = new Entity[4];
		for(int x = 0; x < num.length; x++){
			num[x]= new Entity();
			num[x].add(new GUIButton(0f,0f,0,-60*x,200,50));
			num[x].get(GUIButton.class).setText(new GUIText("Hej", 50, 25, Font.getFont(Font.MONOSPACED)));
			num[x].get(GUIButton.class).setTexture(Texture.DEFAULT);
			num[x].get(GUIButton.class).setHue(new Vector4f(1f,1f,1f,.5f));
			
			num[x].get(SST.class).store("index", x);
			
			/*
			num[x].get(GUIButton.class).setOnHover((sst) -> {
				num[(int) sst.get("index")].get(GUIButton.class).setHue(COLOR_ON);
					 
			});
			
			num[x].get(GUIButton.class).setOnUnhover((sst) -> {
				num[(int) sst.get("index")].get(GUIButton.class).setHue(COLOR_OFF);
				 
			});
			*/
		}
		
		num[0].get(GUIButton.class).setText(new GUIText("START", 50, 25, Font.getFont(Font.MONOSPACED)));
		num[1].get(GUIButton.class).setText(new GUIText("CREDITS", 50, 25, Font.getFont(Font.MONOSPACED)));
		num[2].get(GUIButton.class).setText(new GUIText("KEYS", 50, 25, Font.getFont(Font.MONOSPACED)));
		num[3].get(GUIButton.class).setText(new GUIText("EXIT", 50, 25, Font.getFont(Font.MONOSPACED)));
	
		
		((GUIButton) num[0].get(GUIButton.class)).setOnClick((sst) -> {
			GameStateManager.enterState(TG.GS_CHAPTER_MENU);
		});
		
		((GUIButton) num[1].get(GUIButton.class)).setOnClick((sst) -> {
			System.out.println("Insert Credits screen");
		});	
		
		((GUIButton) num[2].get(GUIButton.class)).setOnClick((sst) -> {
			System.out.println("Insert options screen");
		});	
		
		((GUIButton) num[3].get(GUIButton.class)).setOnClick((sst) -> {
			Game.stop();
		});
	} 
	
	
	
	
	@Override
	public void init() {
		menu();
	}
	
	@Override
	public void update(double delta) {
		
		for(int x=0;x<num.length;x++){
			num[x].update(delta);
		}
	}
	
	@Override
	public void draw() {
			
		for(int x=0;x<num.length;x++){
			num[x].draw();
		}
	}
	
	@Override
	public void exit() {
		for(Entity e : num) {
			e.destroy();
		}
	}
	
	public static final Vector4f COLOR_ON = new Vector4f(0f,1f,0f,.5f);
	public static final Vector4f COLOR_OFF = new Vector4f(1f,1f,1f,.5f);
}