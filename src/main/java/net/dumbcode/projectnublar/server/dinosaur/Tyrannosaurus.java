package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.val;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurPeriod;
import net.dumbcode.projectnublar.server.dinosaur.data.FeedingDiet;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Tyrannosaurus extends Dinosaur {

    public Tyrannosaurus() {
        val map = getModelProperties().getMainModelMap();
        map.put(ModelStage.ADULT, "tyrannosaurus_adult_idle");
        map.put(ModelStage.SKELETON, "tyrannosaurus_skeleton_idle");

        getItemProperties()
                .setCookedMeatHealAmount(10)
                .setCookedMeatSaturation(1f)
                .setRawMeatHealAmount(4)
                .setRawMeatSaturation(0.6f)
                .setCookingExperience(1f);

        DinosaurInformation dinosaurInfomation = this.getDinosaurInfomation();
        dinosaurInfomation.setPeriod(DinosaurPeriod.CRETACEOUS);
        dinosaurInfomation.getBiomeTypes().addAll(Lists.newArrayList(
                BiomeDictionary.Type.CONIFEROUS,
                BiomeDictionary.Type.DRY,
                BiomeDictionary.Type.PLAINS,
                BiomeDictionary.Type.MESA,
                BiomeDictionary.Type.FOREST,
                BiomeDictionary.Type.MOUNTAIN
        ));
    }

    @Override
    public void attachDefaultComponents() {
        addComponent(EntityComponentTypes.METABOLISM)
                .setDistanceSmellFood(30)
                .setDiet(new FeedingDiet()
                        .add(new ItemStack(Items.APPLE)))
                .setMaxFood(7500)
                .setMaxWater(6000);

        Map<ModelStage, List<String>> entity = Maps.newEnumMap(ModelStage.class);
        entity.put(ModelStage.ADULT, Lists.newArrayList(
                "tail4",
                "Tail3",
                "tail2",
                "tail1",

                "legUpperRight",
                "legMiddleRight",
                "legLowerRight",

                "legUpperLeft",
                "legMiddleLeft",
                "legLowerLeft",

                "neck3",

                "hips",
                "chest",
                "jawUpper1",
                "head"));
        addComponent(EntityComponentTypes.MULTIPART)
                .setDefaultStage(ModelStage.ADULT)
                .setLinkedCubeMap(entity);

        addComponent(EntityComponentTypes.ANIMATION)
                .setModelGetter(e -> {
                    ModelStage stage = this.getSystemInfo().defaultStage();
                    AgeComponent component = ((ComponentAccess) e).getOrNull(EntityComponentTypes.AGE);
                    if(component != null) {
                        stage = component.stage;
                        if (!this.getSystemInfo().allAcceptedStages().contains(stage)) {
                            stage = this.getSystemInfo().defaultStage();
                        }
                    }
                    ResourceLocation regname = this.getRegName();
                    return new ResourceLocation(regname.getNamespace(), "models/entities/" + regname.getPath() + "/" + stage.getName().toLowerCase(Locale.ROOT) + "/" + this.getModelProperties().getMainModelMap().get(stage));
                })
                .setInfo(this.getSystemInfo());


        this.addComponent(EntityComponentTypes.GENDER);
        this.addComponent(EntityComponentTypes.AGE);
        this.addComponent(EntityComponentTypes.HERD)
                .setHerdTypeID(new ResourceLocation(ProjectNublar.MODID, "dinosaur_herd_" + this.getFormattedName()));
        this.addComponent(EntityComponentTypes.WANDER_AI);


        this.addComponent(EntityComponentTypes.SKELETAL_BUILDER)
                .initializeMap(
                        "foot", "legLowerLeft",
                        "foot", "legLowerRight",
                        "leg", "legUpperLeft",
                        "leg", "legUpperRight",
                        "body", "hips"
                );
    }
}