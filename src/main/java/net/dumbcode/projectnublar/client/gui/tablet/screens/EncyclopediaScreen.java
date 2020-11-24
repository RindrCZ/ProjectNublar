package net.dumbcode.projectnublar.client.gui.tablet.screens;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class EncyclopediaScreen extends TabletPage {
	
	private final int specButtonX = 120;
	private final int specButtonY = 80;
	private HashMap<String, EncyclopediaPage> pages = new HashMap<String, EncyclopediaPage>();
	private EncyclopediaPage homePage = new HomePage(this);
	private EncyclopediaPage currentPage;
	private boolean loadedData = false;
	private Map<?, ?> data;
	private Map<?, ?> dinosaurs;
	private Map<?, ?> plants;
	
	@Override
	public void onSetAsCurrentScreen() {
		// Load the data for the encyclopedia entries
		try {
			Gson gson = new Gson();
			InputStream stream = EncyclopediaScreen.MC.getResourceManager().getResource(new ResourceLocation(ProjectNublar.MODID, "lang/encyclopedia/en_us.json")).getInputStream();
			Scanner s = new Scanner(stream).useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";
			s.close();
			this.data = gson.fromJson(result, Map.class);
			this.dinosaurs = (Map<?, ?>) data.get("dinosaurs");
			this.plants = (Map<?, ?>) data.get("plants");
			loadedData = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Register the pages here
		pages.put("species", new SpeciesListPage(this));
	}	
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks, String route) {
		for(int i = 10; i <= 500; i += 10) {
			Gui.drawRect(i, 0, i+1, 280, 0xFF444444);
		}
		for(int i = 10; i <= 280; i += 10) {
			Gui.drawRect(0, i, 500, i+1, 0xFF444444);
		}
		MC.fontRenderer.drawString(this.route, 3, 18, 0xFF00FF00);
		if(!this.route.equals("encyclopedia:/")) {
			Gui.drawRect(0, 35, 15, 50, 0xFF000000);
		}
		if(this.currentPage != null) {
			MC.fontRenderer.drawString(this.currentPage.getClass().getSimpleName(), 3, 270, 0xFFFF0000);
			this.currentPage.render(mouseX, mouseY, partialTicks, route);
		} else {
			// Navigate to home page
			this.navigateRoute("/");
		}
	}
	
	@Override
	public void onMouseInput(int mouseX, int mouseY) {
		if(this.currentPage != null) {
			this.currentPage.onMouseInput(mouseX, mouseY);
		}
	}
	
	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if(this.currentPage != null) {
			this.currentPage.onMouseClicked(mouseX, mouseY, mouseButton);
		}
		if(mouseX >= 0 && mouseY >= 35 && mouseX <= 15 && mouseY <= 50) {
			this.navigateRoute("..");
		}
    }
	
	@Override
	public void navigateRoute(String link) {
		super.navigateRoute(link);
		this.updatePage();
	}
	
	public void updatePage() {
		String[] routePieces = this.route.split("/");
		if(routePieces.length > 1) {
			if(routePieces.length > 2) {
				this.currentPage = pages.get(routePieces[1]).getSubpage(routePieces[2]);
			} else {
				this.currentPage = pages.get(routePieces[1]);
			}
		} else {
			this.currentPage = this.homePage;
		}
	}
	
	public void renderHome(int mouseX, int mouseY, float partialTicks) {
		Gui.drawRect(this.specButtonX, this.specButtonY, this.specButtonX + 80, this.specButtonY + 80, 0xFF000000);
	}
	
	@RequiredArgsConstructor
	private class EncyclopediaPage {
		protected final EncyclopediaScreen screen;
		
		public void render(int mouseX, int mouseY, float partialTicks, String route) {}
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {}
		public void onMouseInput(int mouseX, int mouseY) {}
		public EncyclopediaPage getSubpage(String route) {
			return this;
		}
	}
	
	private class HomePage extends EncyclopediaPage {
		public HomePage(EncyclopediaScreen screen) {
			super(screen);
		}
		
		@Override
		public void render(int mouseX, int mouseY, float partialTicks, String route) {
			Gui.drawRect(120, 100, 160, 140, 0xFF00FF00);
		}
		
		@Override
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
			if(mouseX >= 120 && mouseY >= 100 && mouseX <= 160 && mouseY <= 140) {
				this.screen.navigateRoute("/species");
			}
		}
	}
	
	private class SpeciesListPage extends EncyclopediaPage {
		private GuiScrollBox<SpeciesScrollEntry> scrollBox;
		private List<SpeciesScrollEntry> scrollEntries = new ArrayList<SpeciesScrollEntry>();
		
		public SpeciesListPage(EncyclopediaScreen screen) {
			super(screen);
			this.scrollBox = new GuiScrollBox<SpeciesScrollEntry>(40, 50, 420, 80, 2, () -> this.scrollEntries);
			// Split the list of dinosaurs into separate lists of 5 each to make the rows
			List<Set<String>> rows = partitionSet((Set<String>) this.screen.dinosaurs.keySet(), 5);
			rows.forEach(row -> {
				this.scrollEntries.add(new SpeciesScrollEntry(row, this.screen.dinosaurs));
			});
		}
		
		@Override
		public void render(int mouseX, int mouseY, float partialTicks, String route) {
			//Gui.drawRect(120, 100, 160, 140, 0xFFFF0000);
			scrollBox.render(mouseX, mouseY);
		}
		
		@Override
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
			if(mouseX >= 120 && mouseY >= 100 && mouseX <= 160 && mouseY <= 140) {
				this.screen.navigateRoute("mosasaurus");
			}
		}
		
		@Override
		public void onMouseInput(int mouseX, int mouseY) {
			scrollBox.handleMouseInput();
		}
		
		@Override
		public EncyclopediaPage getSubpage(String route) {
			return new DinosaurPage(this.screen, route);
		}
		
		private List<Set<String>> partitionSet(Set<String> set, int partitionSize)
		{
			List<Set<String>> list = new ArrayList<>();
			int setSize = set.size();
			
			Iterator iterator = set.iterator();
			
			while(iterator.hasNext())
			{
				Set newSet = new HashSet();
				for(int j = 0; j < partitionSize && iterator.hasNext(); j++)
				{
					String s = (String)iterator.next();
					newSet.add(s);
				}
				list.add(newSet);
			}
			return list;
		}
	}
	
	private class SpeciesScrollEntry implements GuiScrollboxEntry {
		private List<String> speciesKeys;
		private Map<?, ?> dinos;
		
		public SpeciesScrollEntry(Set<String> speciesKeys, Map<?, ?> dinos) {
			super();
			this.speciesKeys = new ArrayList<>(speciesKeys);
			this.dinos = dinos;
		}
		
		@Override
		public void draw(int x, int y, int mouseX, int mouseY, boolean mouseOver) {
			for(int i = 0; i < speciesKeys.size(); i++) {
				int iconX = x + (i * 85);
				Gui.drawRect(iconX, y, iconX + 80, y + 80, 0xFF36393F);
				MC.fontRenderer.drawString((String) ((Map<?, ?>) dinos.get(speciesKeys.get(i))).get("name"), iconX + 3, y + 35, 0xFFFFFFFF);
			}
		}
	}
	
	private class DinosaurPage extends EncyclopediaPage {
		public String id;
		private String name;
		private String scientificName;
		private String age;
		private String biomes;
		private String size;
		
		public DinosaurPage(EncyclopediaScreen screen, String id) {
			super(screen);
			this.id = id;
			Map<?, ?> dinoData = (Map<?, ?>) this.screen.dinosaurs.get(id);
			this.name = (String) dinoData.get("name");
			this.scientificName = (String) dinoData.get("scientificName");
			this.age = (String) dinoData.get("age");
			this.biomes = (String) dinoData.get("biomes");
			this.size = (String) dinoData.get("size");
		}
		
		@Override
		public void render(int mouseX, int mouseY, float partialTicks, String route) {
			// Draw the info box
			Gui.drawRect(50, 50, 260, 240, 0xFF00A5CF);
			// Draw what will later become the image of the dino
			Gui.drawRect(280, 50, 460, 240, 0xFF00FF00);
			// Draw the name
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.name", this.name), 55, 60, 0xFFFFFFFF);
			// Draw the scientific name
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.scientific_name", "§o", this.scientificName), 55, 75, 0xFFFFFFFF);
			// Draw the age
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.age", this.age), 55, 90, 0xFFFFFFFF);
			// Draw the biomes
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.biomes", this.biomes), 55, 105, 0xFFFFFFFF);
			// Draw the rarity
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.rarity", "§a", "Uncommon"), 55, 120, 0xFFFFFFFF);
			// Draw the diet
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.diet", "Piscovore"), 55, 135, 0xFFFFFFFF);
			// Draw the size
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.size", this.size), 55, 150, 0xFFFFFFFF);
			// Draw the genome percent
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.genome_percent", "§a", "90%"), 55, 165, 0xFFFFFFFF);
			// Draw genome progress bar
			Gui.drawRect(55, 175, 255, 190, 0xFFFF0000);
			Gui.drawRect(55, 175, 235, 190, 0xFF00FF00);
			// Draw the cloned assets
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.cloned_assets", "2"), 55, 195, 0xFFFFFFFF);
		}
		
		private void drawDinosaur() {
			// TODO make it draw the dinosaur
		}
		
		@Override
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
			
		}
	}
}
