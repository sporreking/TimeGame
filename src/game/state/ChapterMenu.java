package game.state;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;

import game.TG;
import sk.entity.Container;
import sk.entity.Entity;
import sk.gamestate.GameState;
import sk.gamestate.GameStateManager;
import sk.gfx.gui.GUIButton;
import sk.gfx.gui.GUIText;
import sk.sst.SST;

public class ChapterMenu implements GameState {
	
	public static final String BASE_FOLDER = "res/level/";
	public static final int ITEMS_PER_ROW = 3;
	public static final int ITEMS_PER_COLUMN = 3;
	public static final int SIZE = 100;
	public static final float PADDING = 1.1f;
	
	private int current = 0;
	private ArrayList<String> chapters;
	
	private Container page;
	
	private Entity exit, back, next;
	
	@Override
	public void init() {
		chapters = new ArrayList<>();
		
		File base = new File(BASE_FOLDER);
		
		for (File f : base.listFiles((current, name) -> {
			return new File(current, name).isDirectory(); })) {
			
			chapters.add(f.getName());
		}
		
		addNavigationButtons();
		
		loadPage();
	}
	
	private void addNavigationButtons() {
		
		// Exit
		int x = (int) (((ITEMS_PER_ROW - 1) / (-2)) * SIZE * PADDING);
		int y = (int) (((ITEMS_PER_COLUMN - 1) / (-2) - 1) * SIZE * PADDING);
		
		GUIButton b_exit = new GUIButton(0, 0, x, y, SIZE, SIZE);
		b_exit.setText(new GUIText("Exit", SIZE, SIZE, Font.getFont(Font.MONOSPACED)));
		exit = new Entity().add(b_exit);
		b_exit.setOnClick((sst) -> GameStateManager.enterState(TG.GS_MAIN_MENU));
		
		// Back
		x = 0;
		y = (int) (((ITEMS_PER_COLUMN - 1) / (-2) - 1) * SIZE * PADDING);
		
		GUIButton b_back = new GUIButton(0, 0, x, y, SIZE, SIZE);
		b_back.setText(new GUIText("<", SIZE, SIZE, Font.getFont(Font.MONOSPACED)));
		back = new Entity().add(b_back);
		b_back.setOnClick((sst) -> page(-1));
		
		// Next
		x = (int) (((ITEMS_PER_ROW - 1) / (2)) * SIZE * PADDING);
		y = (int) (((ITEMS_PER_COLUMN - 1) / (-2) - 1) * SIZE * PADDING);
		
		GUIButton b_next = new GUIButton(0, 0, x, y, SIZE, SIZE);
		b_next.setText(new GUIText(">", SIZE, SIZE, Font.getFont(Font.MONOSPACED)));
		next = new Entity().add(b_next);
		b_next.setOnClick((sst) -> page(1));
	}
	
	private void page(int inc) {
		current = Math.max(0, Math.min(current + inc, Math.floorDiv(chapters.size(),
				ITEMS_PER_ROW * ITEMS_PER_COLUMN)));
		
		loadPage();
	}
	
	private void loadPage() {
		if(page != null)
			page.destroy();
		
		page = new Container();
		
		for(int i = 0; i < ITEMS_PER_COLUMN; i++) {
			for(int j = 0; j < ITEMS_PER_ROW; j++) {
				int index = current * ITEMS_PER_ROW * ITEMS_PER_COLUMN + i * ITEMS_PER_ROW + j;
				
				if(index >= chapters.size()) {
					return;
				}
				
				String caption = chapters.get(index);
				
				int x = (int) (((ITEMS_PER_ROW - 1) / (-2) + j) * SIZE * PADDING);
				int y = (int) (((ITEMS_PER_COLUMN - 1) / 2 - i) * SIZE * PADDING);
				
				GUIButton b = new GUIButton(0, 0, x, y, SIZE, SIZE);
				b.setText(new GUIText(caption, SIZE, SIZE, Font.getFont(Font.MONOSPACED)));
				Entity e = new Entity().add(b);
				e.get(SST.class).store("chapter", caption);
				page.add(e);
				b.setOnClick((sst) -> {
					TG.GS_PLAYING.chapter = (String) sst.get("chapter");
					GameStateManager.enterState(TG.GS_PLAYING);
				});
				
			}
		}
	}
	
	@Override
	public void update(double delta) {
		page.update(delta);
		
		exit.update(delta);
		back.update(delta);
		next.update(delta);
	}
	
	@Override
	public void draw() {
		page.draw();
		exit.draw();
		back.draw();
		next.draw();
	}
	
	@Override
	public void exit() {
		// TODO Auto-generated method stub
		
	}
}