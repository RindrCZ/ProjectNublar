package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Set;

public class BlockEntityElectricFencePole extends SimpleBlockEntity {
    public List<Connection> fenceConnections = Lists.newArrayList();

    public int renderList = -1;
    public boolean rotatedAround = false;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbt = new NBTTagList();
        for (Connection connection : this.fenceConnections) {
            nbt.appendTag(connection.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("connections", nbt);
        compound.setBoolean("rotated", this.rotatedAround);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.rotatedAround = compound.getBoolean("rotated");
        this.fenceConnections.clear();
        NBTTagList nbt = compound.getTagList("connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbt.tagCount(); i++) {
            this.fenceConnections.add(Connection.fromNBT(nbt.getCompoundTagAt(i), this));
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB; //TODO:change this
    }
}