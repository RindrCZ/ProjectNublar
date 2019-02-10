package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.projectnublar.client.render.SkeletonBuilderScene;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalHistory;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.vecmath.Vector3f;
import java.util.Map;

import static net.dumbcode.projectnublar.server.ProjectNublar.DINOSAUR_REGISTRY;

@Getter
@Setter
public class SkeletalBuilderBlockEntity extends SimpleBlockEntity implements ITickable {
    private final ItemStackHandler boneHandler = new ItemStackHandler();
    private final SkeletalProperties skeletalProperties = new SkeletalProperties();
    private Dinosaur dinosaur = Dinosaur.MISSING;
    @SideOnly(Side.CLIENT)
    private SkeletonBuilderScene scene;

    @Getter(lazy = true)
    private final DinosaurEntity dinosaurEntity = createEntity();

    @Getter private final SkeletalHistory history = new SkeletalHistory();


    // Not saved to NBT, player-specific only to help with posing

    private float cameraPitch;
    private float cameraYaw = 90f;
    private double cameraZoom = 1.0;

    public SkeletalBuilderBlockEntity() {
        this.reassureSize();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("Dinosaur", this.dinosaur.getRegName().toString());
        nbt.setTag("Inventory", this.boneHandler.serializeNBT());
        nbt.setTag("History", history.writeToNBT(new NBTTagCompound()));

        // save pose data
        nbt.setTag("SkeletalProperties", this.skeletalProperties.serialize(new NBTTagCompound()));
        return super.writeToNBT(nbt);
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        setDinosaur(DINOSAUR_REGISTRY.getValue(new ResourceLocation(nbt.getString("Dinosaur"))));
        this.boneHandler.deserializeNBT(nbt.getCompoundTag("Inventory"));
        // load pose data
        NBTTagCompound pose = nbt.getCompoundTag("Pose");
        this.reassureSize();
        this.history.readFromNBT(nbt.getCompoundTag("History"));
        this.skeletalProperties.deserialize(nbt.getCompoundTag("SkeletalProperties"));
        super.readFromNBT(nbt);
    }

    private void reassureSize() {
        int size = this.dinosaur.getSkeletalInformation().getBoneListed().size();
        if(size != this.boneHandler.getSlots()) {
            this.boneHandler.setSize(size); //TODO: Maybe make a diffrent method that keeps the items?
        }
    }

    public Dinosaur getDinosaur() {
        return dinosaur;
    }

    public void setDinosaur(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
        this.getDinosaurEntity().getOrExcept(EntityComponentTypes.DINOSAUR).dinosaur = dinosaur;
        this.history.clear();
        this.reassureSize();
    }

    public TabulaModel getModel() {
        if(this.dinosaur == Dinosaur.MISSING) {
            return null;
        }
        DinosaurEntity de = this.getDinosaurEntity();
        return de.getDinosaur().getModelContainer().getModelMap().get(de.getOrExcept(EntityComponentTypes.SKELETAL_BUILDER).stage);
    }

    public ItemStackHandler getBoneHandler() {
        return boneHandler;
    }

    private DinosaurEntity createEntity() {
        DinosaurEntity entity = this.dinosaur.createEntity(this.world);
        entity.attachComponent(EntityComponentTypes.SKELETAL_BUILDER);
        entity.getOrExcept(EntityComponentTypes.SKELETAL_BUILDER).stage = ModelStage.SKELETON; //TODO: change life?
        return entity;
    }

    public Map<String, Vector3f> getPoseData() {
        //todo caching
        Map<String, Vector3f> map = Maps.newHashMap();

        this.history.getHistory().forEach(c -> {
            if(c.getPart().equals(SkeletalHistory.RESET_NAME)) {
                map.clear();
            } else {
                map.put(c.getPart(), c.getAngle());
            }
        });

        map.putAll(this.history.getEditingData());

        return map;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB; //TODO: get size of dinosaur, and fit this to it
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    public void setCameraAngles(float cameraYaw, float cameraPitch) {
        this.cameraYaw = cameraYaw;
        this.cameraPitch = cameraPitch;
    }

    @Override
    public void update() {
        this.getSkeletalProperties().setPrevRotation(this.getSkeletalProperties().getRotation());
    }

    @Value
    public class Record { private String part; private Vector3f newAngles; }
}
