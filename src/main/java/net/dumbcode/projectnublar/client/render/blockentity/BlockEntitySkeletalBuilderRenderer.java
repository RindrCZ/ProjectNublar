package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.block.SkeletalBuilder;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Vector3f;

public class BlockEntitySkeletalBuilderRenderer extends TileEntitySpecialRenderer<BlockEntitySkeletalBuilder> {
    private Minecraft mc = Minecraft.getMinecraft();
    @Override
    public void render(BlockEntitySkeletalBuilder te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(SkeletalBuilder.FACING);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.enableLighting();
        Vector3f rotation = new Vector3f();
        float angle = 90F;
        switch (facing.getAxis()) {
            case X:
                rotation = new Vector3f(0, 0, 1);
                angle = facing == EnumFacing.WEST ? 90F : 270F;
                break;
            case Y:
                rotation = new Vector3f(1, 0, 0);
                angle = facing == EnumFacing.UP ? 0 : 180F;
                break;
            case Z:
                rotation = new Vector3f(1, 0, 0);
                angle = facing == EnumFacing.SOUTH ? 90F : 270F;
                break;
        }
        if(facing != EnumFacing.UP) {
            GlStateManager.rotate(angle, rotation.x, rotation.y, rotation.z);
        }
        GlStateManager.rotate(te.getRotation().ordinal() * 90, 0, 1, 0);
        this.mc.getRenderManager().renderEntity(te.getDinosaurEntity(), 0, 0, 0, 0, partialTicks, false);
        GlStateManager.rotate(te.getRotation().ordinal() * 90, 0, -1, 0);
        if(facing != EnumFacing.UP) {
            GlStateManager.rotate(-angle, rotation.x, rotation.y, rotation.z);
        }
        GlStateManager.translate(-x - 0.5, -y - 0.5, -z - 0.5);
        GlStateManager.popMatrix();
    }
}
