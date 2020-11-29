package net.dumbcode.projectnublar.client.gui.tablet.screens;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.gui.GuiHelper;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.ModelComponent;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class EncyclopediaScreen extends TabletPage {
	
	private final int specButtonX = 120;
	private final int specButtonY = 80;
	private HashMap<String, EncyclopediaPage> pages = new HashMap<>();
	private EncyclopediaPage homePage = new HomePage(this);
	private EncyclopediaPage currentPage;
	private Map<?, ?> data;
	
	private static final Logger logger =  LogManager.getLogger();
	
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
		} catch (IOException e) {
			logger.warn("Could not load lang file for {}, defaulting to en_us", "lang");
			e.printStackTrace();
		}
		
		// Register the pages here
		pages.put("species", new SpeciesListPage(this));
	}	
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks, String route) {
//		for(int i = 10; i <= 500; i += 10) {
//			Gui.drawRect(i, 0, i+1, 280, 0xFF444444);
//		}
//		for(int i = 10; i <= 280; i += 10) {
//			Gui.drawRect(0, i, 500, i+1, 0xFF444444);
//		}
		MC.fontRenderer.drawString(this.route, 3, 18, 0xFF00FF00);
		if(!this.route.equals("encyclopedia:/")) {
			Gui.drawRect(0, 35, 15, 50, 0xFF000000);
		}
		if(this.currentPage != null) {
			MC.fontRenderer.drawString(this.currentPage.getClass().getSimpleName(), 3, 270, 0xFFFF0000);
			this.currentPage.render(mouseX, mouseY, partialTicks, route);
		} else {
			// Navigate to home page if the current page is null (likely when loading for the first time)
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
		private List<SpeciesScrollEntry> scrollEntries = new ArrayList<>();
		
		public SpeciesListPage(EncyclopediaScreen screen) {
			super(screen);
			this.scrollBox = new GuiScrollBox<>(40, 50, 420, 80, 2, () -> this.scrollEntries);
			this.scrollBox.disableDefaultCellRendering();
			// Split the list of dinosaurs into separate lists of 5 each to make the rows
			Iterator dinoIterator = ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection().iterator();
			List<Dinosaur> dinosaurs = new ArrayList<>();
			while(dinoIterator.hasNext()) {
				dinosaurs.add((Dinosaur) dinoIterator.next());
				if(dinosaurs.size() == 5) {
					this.scrollEntries.add(new SpeciesScrollEntry(new ArrayList<>(dinosaurs), this.screen));
					dinosaurs.clear();
				}
			}
			// Make sure to add the last row if the dinosaurs aren't a perfect multiple of 5
			if(dinosaurs.size() > 0) {
				this.scrollEntries.add(new SpeciesScrollEntry(new ArrayList<>(dinosaurs), this.screen));
				dinosaurs.clear();
			}
		}
		
		@Override
		public void render(int mouseX, int mouseY, float partialTicks, String route) {
			this.scrollBox.render(mouseX, mouseY);
		}
		
		@Override
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
			this.scrollBox.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		@Override
		public void onMouseInput(int mouseX, int mouseY) {
			this.scrollBox.handleMouseInput();
		}
		
		@Override
		public EncyclopediaPage getSubpage(String route) {
			return new DinosaurPage(this.screen, route);
		}
	}
	
	private class SpeciesScrollEntry implements GuiScrollboxEntry {
		private List<Dinosaur> dinos;
		private Map<?, ?> langData;
		private EncyclopediaScreen screen;
		
		public SpeciesScrollEntry(List<Dinosaur> dinos, EncyclopediaScreen screen) {
			super();
			this.dinos = dinos;
			this.langData = screen.data;
			this.screen = screen;
		}
		
		@Override
		public void draw(int x, int y, int mouseX, int mouseY, boolean mouseOver) {
			for(int i = 0; i < this.dinos.size(); i++) {
				int iconX = x + (i * 85);
				if(mouseOver && mouseX >= iconX && mouseX <= iconX + 80) {
					Gui.drawRect(iconX, y, iconX + 80, y + 80, 0xFF00FF00);
				} else {
					Gui.drawRect(iconX, y, iconX + 80, y + 80, 0xFF36393F);
				}
				String dinosaurName = this.dinos.get(i).getFormattedName();
				String localizedName = I18n.format("projectnublar.dino." + dinosaurName + ".name");
				int textHeight = MC.fontRenderer.getWordWrappedHeight(localizedName, 75);
				MC.fontRenderer.drawSplitString(localizedName, iconX + 3, y + 40 - (textHeight / 2), 75,0xFFFFFFFF);
			}
		}
		
		@Override
		public boolean onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
			for(int i = 0; i < this.dinos.size(); i++) {
				int iconX = (i * 85);
				if(relMouseX >= iconX && relMouseX <= iconX + 80) {
					EncyclopediaScreen.logger.info("Species {} was clicked", this.dinos.get(i).getFormattedName());
					this.screen.navigateRoute(this.dinos.get(i).getFormattedName());
					return true;
				}
			}
			return false;
		}
	}
	
	private class DinosaurPage extends EncyclopediaPage {
		
		public String id;
		
		private String name;
		private String scientificName;
		private String age;
		private String biomes;
		private String size;
		
		private Dinosaur dino;
		private DinosaurEntity entity;
		
		private final Framebuffer framebuffer;
		
		public DinosaurPage(EncyclopediaScreen screen, String id) {
			super(screen);
			this.dino = ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation("projectnublar:" + id));
			this.entity = this.dino.createEntity(MC.world);
			if(this.dino == null) {
				// Go back to the species list if they somehow navigated to a dinosaur that doesn't exist
				this.screen.navigateRoute("/species");
			}
			this.id = id;
			Map<?, ?> dinoData = (Map<?, ?>) this.screen.data.get(id);
			this.name = I18n.format("projectnublar.dino." + id + ".name");
			this.scientificName = (String) dinoData.get("scientificName");
			this.age = (String) dinoData.get("age");
			this.biomes = this.dino.getDinosaurInfomation().getBiomeTypes().stream().map(BiomeDictionary.Type::getName).collect(Collectors.joining("; "));
			this.size = (String) dinoData.get("size");
			
			this.framebuffer = new Framebuffer(180, 190, true);
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
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.biomes"), 55, 105, 0xFFFFFFFF);
			int extraLength = MC.fontRenderer.getWordWrappedHeight(this.biomes, 200);
			extraLength -= 15; // Subtract the extra 15 that I already factored in from the first line
			MC.fontRenderer.drawSplitString(this.biomes, 55, 116, 200,0xFFFFFFFF);
			// Draw the rarity
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.rarity", "§a", "Uncommon"), 55, 135 + extraLength, 0xFFFFFFFF);
			// Draw the diet
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.diet", "Piscovore"), 55, 150 + extraLength, 0xFFFFFFFF);
			// Draw the size
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.size", this.size), 55, 165 + extraLength, 0xFFFFFFFF);
			// Draw the genome percent
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.genome_percent", "§6", "100%"), 55, 180 + extraLength, 0xFFFFFFFF);
			// Draw the cloned assets
			MC.fontRenderer.drawString(I18n.format("projectnublar.gui.encyclopedia.cloned_assets", "2"), 55, 195 + extraLength, 0xFFFFFFFF);
			this.drawDinosaur();
		}
		
		private void drawDinosaur() {
			// TODO make it draw the dinosaur
			this.framebuffer.bindFramebuffer(true);
			GuiHelper.prepareModelRendering(280, 50, 30f, 0f, 90f);
			
			Optional<ModelComponent> model = this.entity.getComponentMap().get(EntityComponentTypes.MODEL);
			ResourceLocation texture = model.map(ModelComponent::getTexture).map(RenderLocationComponent.ConfigurableLocation::getLocation).orElse(TextureMap.LOCATION_MISSING_TEXTURE);
			MC.getTextureManager().bindTexture(texture);
			TabulaModel tabModel = this.entity.getOrExcept(EntityComponentTypes.MODEL).getModelCache();
			tabModel.renderBoxes(1f/16f);
			
			GuiHelper.cleanupModelRendering();
			
			MC.getFramebuffer().bindFramebuffer(true);
		}
		
		@Override
		public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
			
		}
	}
}
