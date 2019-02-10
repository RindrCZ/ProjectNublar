package net.dumbcode.projectnublar.client.render.blockentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.model.InfoTabulaModel;
import net.dumbcode.projectnublar.client.render.MoreTabulaUtils;
import net.dumbcode.projectnublar.client.render.SkeletonBuilderScene;
import net.dumbcode.projectnublar.client.render.TabulaModelClipPlane;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.SkeletalBuilderBlock;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.GenderComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.SkeletalBuilderCompoent;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;

import static java.lang.Math.PI;

public class BlockEntitySkeletalBuilderRenderer extends TileEntitySpecialRenderer<SkeletalBuilderBlockEntity> {
    private Minecraft mc = Minecraft.getMinecraft();




    @Override
    public void render(SkeletalBuilderBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        if(false) {
            this.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/low_security_electric_fence_pole_powered.png"));
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.color(1f, 1f, 1f, 1f);
            RenderHelper.disableStandardItemLighting();
            int light = te.getWorld().getCombinedLight(te.getPos(), 0);
            int ls = light % 65536;
            int lt = light / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)ls, (float)lt);
            GlStateManager.enableCull();

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.pushMatrix();
            GlStateManager.scale(2.5,2.5,2.5);
            GlStateManager.translate(0f, 1.5f, 0f);
            GlStateManager.rotate(180f, 0f, 0f, 1f);

            if(te.getDinosaur() != Dinosaur.MISSING) {

                Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/entities/tyrannosaurus/male_adult.png"));
                TabulaModelClipPlane clipPlane = new TabulaModelClipPlane(te.getDinosaur().getModelContainer().getModelMap().get(ModelStage.ADULT));
                clipPlane.render(4-((te.getWorld().getTotalWorldTime() + partialTicks) / 30D)%6, 0xbda47e);

                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.scale(2.3,2.3,2.3);
                GlStateManager.translate(0f, 1.70f, 0f);
                GlStateManager.rotate(180f, 0f, 0f, 1f);
                Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/entities/tyrannosaurus/female_skeleton.png"));
                MoreTabulaUtils.renderModelWithoutChangingPose(te.getDinosaur().getModelContainer().getModelMap().get(ModelStage.SKELETON), 1/16F);
            }
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
            return;
        }
        double scale = 2.5;

        IBlockState state = te.getWorld().getBlockState(te.getPos());
        if(state.getBlock() != BlockHandler.SKELETAL_BUILDER) { //Can sometimes happen when loading in a save. Not sure why, but it happens
            return;
        }
        GlStateManager.pushMatrix();
        EnumFacing facing = state.getValue(SkeletalBuilderBlock.FACING);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        GlStateManager.pushMatrix();
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        if(false) {//TODO: remove
            SkeletonBuilderScene scene = te.getScene();
            if(scene == null) {
                te.setScene(scene = new SkeletonBuilderScene(te));
            }
            scene.update(getWorld().getTotalWorldTime(), partialTicks);
            GlStateManager.disableAlpha();
            scene.getFramebuffer().bindFramebufferTexture();
            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buff.pos(0, 0, 0).tex(0, 0).endVertex();
            buff.pos(0, 0, 1).tex(0, 1).endVertex();
            buff.pos(2, 0, 1).tex(1, 1).endVertex();
            buff.pos(2, 0, 0).tex(1, 0).endVertex();
            Tessellator.getInstance().draw();


            RenderHelper.enableStandardItemLighting();

        }

        GlStateManager.enableAlpha();

        Vector3f rotVec = new Vector3f();
        float angle = 90F;
        switch (facing.getAxis()) { //There gotta be a better way than this
            case X:
                rotVec = new Vector3f(0, 0, 1);
                angle = facing == EnumFacing.WEST ? 90F : 270F;
                break;
            case Y:
                rotVec = new Vector3f(1, 0, 0);
                angle = facing == EnumFacing.UP ? 0 : 180F;
                break;
            case Z:
                rotVec = new Vector3f(1, 0, 0);
                angle = facing == EnumFacing.SOUTH ? 90F : 270F;
                break;
        }

        Matrix4d translateMatrix = new Matrix4d();
        Matrix4d rotateMatrix = new Matrix4d();
        Matrix4d facingMatrix = new Matrix4d();

        Vec3i facingRot = facing.getDirectionVec();
        translateMatrix.set(new Vector3d(te.getPos().getX() + facingRot.getX(), te.getPos().getY() + facingRot.getY(), te.getPos().getZ() + facingRot.getZ()));

        GlStateManager.rotate(angle, rotVec.x, rotVec.y, rotVec.z);
        if(rotVec.x != 0) {
            facingMatrix.rotX(angle / 180F * Math.PI);
        } else if(rotVec.y != 0) {
            facingMatrix.rotY(angle / 180F * Math.PI);
        } else if(rotVec.z != 0) {
            facingMatrix.rotZ(angle / 180F * Math.PI);
        }

        float teRot = te.getSkeletalProperties().getRotation();

        teRot = teRot + (te.getSkeletalProperties().getPrevRotation() - teRot) * partialTicks;
        GlStateManager.rotate(teRot, 0, 1, 0);
        rotateMatrix.rotY(teRot / 180D * PI);

        GlStateManager.translate(0, -0.5F, 0);
        GlStateManager.scale(scale,scale,scale);
        GlStateManager.translate(0f, 1.5f, 0f);
        GlStateManager.rotate(180f, 0f, 0f, 1f);

        ResourceLocation regname = te.getDinosaur().getRegName();
        GenderComponent gcomp = te.getDinosaurEntity().getOrNull(EntityComponentTypes.GENDER);
        String name = te.getDinosaurEntity().get(EntityComponentTypes.SKELETAL_BUILDER).map(s -> s.stage).orElse(ModelStage.ADULT).getName();
        ResourceLocation loc;
        if(gcomp != null) {
            loc = new ResourceLocation(regname.getResourceDomain(), "textures/entities/" + regname.getResourcePath() + "/" + (gcomp.male ? "male" : "female") + "_" + name + ".png");
        } else {
            loc = new ResourceLocation(regname.getResourceDomain(), "textures/entities/" + regname.getResourcePath() + "/" + name + ".png");
        }

        mc.getTextureManager().bindTexture(loc);

        Map<String, Vector3f> poseData = te.getPoseData();

        GlStateManager.disableCull();

        TabulaModel model = te.getModel();

        if(model != null) {
            for(ModelRenderer box : model.boxList) {
                Vector3f rotations = poseData.get(box.boxName);
                if(rotations != null) {
                    box.rotateAngleX = rotations.x;
                    box.rotateAngleY = rotations.y;
                    box.rotateAngleZ = rotations.z;
                } else {
                    box.rotateAngleX = ((AdvancedModelRenderer)box).defaultRotationX;
                    box.rotateAngleY = ((AdvancedModelRenderer)box).defaultRotationY;
                    box.rotateAngleZ = ((AdvancedModelRenderer)box).defaultRotationZ;
                }
            }

            setVisability(te.getDinosaurEntity(), model);
            MoreTabulaUtils.renderModelWithoutChangingPose(model, 1f/16f);
            resetVisability(model);

        }
        GlStateManager.popMatrix();

        GlStateManager.enableCull();

        World world = te.getWorld();

        double poleWidth = 1 / 16F;
        double baseWidth = 6 / 16F;
        double baseHeight = 1 / 16F;

        double notchWidth = 4 / 16F;
        double notchHeight = 2 / 16F;

        float textureWidth = 8F;
        float textureHeight = 16F;

        if (model != null) {
            for (SkeletalProperties.Pole pole : te.getSkeletalProperties().getPoles()) {
                String anchoredPart = pole.getCubeName();
                PoleFacing poleFacing = pole.getFacing();
                EnumFacing enumFacing = poleFacing.getFacing();

                AdvancedModelRenderer cube = model.getCube(anchoredPart);
                if (poleFacing != PoleFacing.NONE && cube != null && (cube.scaleX != 0 || cube.scaleY != 0 || cube.scaleZ != 0)) {
                    InfoTabulaModel infoModel = (InfoTabulaModel) model;
                    int[] dimensions = infoModel.getDimension(cube);
                    ModelBox box = ObfuscationReflectionHelper.<List<ModelBox>, ModelRenderer>getPrivateValue(ModelRenderer.class, cube, "cubeList", "field_78804" + "_l").get(0); //TODO: remove this god awful method of getting the offsets

                    Point3d endPoint = new Point3d((box.posX1 + dimensions[0] / 2F) / 16F, (box.posY1 + dimensions[1] / 2F) / -16F, (box.posZ1 + dimensions[2] / 2F) / -16F);

                    Matrix4d boxTranslate = new Matrix4d();
                    Matrix4d boxRotateX = new Matrix4d();
                    Matrix4d boxRotateY = new Matrix4d();
                    Matrix4d boxRotateZ = new Matrix4d();
                    boxTranslate.set(new Vector3d(cube.rotationPointX / 16, -cube.rotationPointY / 16, -cube.rotationPointZ / 16));
                    boxRotateX.rotX(cube.rotateAngleX);
                    boxRotateY.rotY(-cube.rotateAngleY);
                    boxRotateZ.rotZ(-cube.rotateAngleZ);
                    boxRotateX.transform(endPoint);
                    boxRotateY.transform(endPoint);
                    boxRotateZ.transform(endPoint);
                    boxTranslate.transform(endPoint);

                    Vec3d partOrigin = cube.getModelPos(cube, new Vec3d(endPoint.x, endPoint.y, endPoint.z));
                    partOrigin = new Vec3d(-partOrigin.x, /*No need to minus the y, as we flip the model around anyway*/partOrigin.y, -partOrigin.z);

                    Point3d rendererPos = new Point3d(partOrigin.x, partOrigin.y + 1.5, partOrigin.z);
                    rendererPos.scale(scale);
                    rotateMatrix.transform(rendererPos);
                    facingMatrix.transform(rendererPos);
                    rendererPos.y -= 1.5;
                    translateMatrix.transform(rendererPos);

                    AxisAlignedBB bounding = //TileEntity.INFINITE_EXTENT_AABB;
                            new AxisAlignedBB(0, 0, 0, enumFacing.getFrontOffsetX() * 256F, enumFacing.getFrontOffsetY() * 256F, enumFacing.getFrontOffsetZ() * 256F)
                                    .offset(rendererPos.x + 0.5F, rendererPos.y + 0.5F, rendererPos.z + 0.5F)
                                    .grow(baseWidth / 2F);

                    double cubeEndPos = -1;
                    for (BlockPos sectionPos : BlockPos.getAllInBox(new BlockPos(rendererPos.x + 0.5F - baseWidth / 2F, rendererPos.y + 0.5F - baseWidth / 2F, rendererPos.z + 0.5F - baseWidth / 2F), new BlockPos(rendererPos.x + 0.5F + baseWidth / 2F, rendererPos.y + 0.5F + baseWidth / 2F, rendererPos.z + 0.5F + baseWidth / 2F))) {
                        List<AxisAlignedBB> aabbList = Lists.newArrayList();
                        int counter = 0;
                        while (aabbList.isEmpty() && counter <= 100) {
                            world.getBlockState(sectionPos).addCollisionBoxToList(world, sectionPos, bounding, aabbList, null, false);
                            sectionPos = sectionPos.add(poleFacing.getFacing().getDirectionVec());
                            counter++;
                        }
                        if (!aabbList.isEmpty()) {
                            for (AxisAlignedBB aabb : aabbList) {
                                cubeEndPos = Math.max(cubeEndPos, poleFacing.apply(aabb) - 0.5F);
                            }
                        }
                    }
                    if (cubeEndPos == -1) {
                        continue;
                    }
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-te.getPos().getX() + rendererPos.x, -te.getPos().getY() + rendererPos.y, -te.getPos().getZ() + rendererPos.z);

                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                    GlStateManager.color(1f, 1f, 1f, 1f);
                    Tessellator tes = Tessellator.getInstance();
                    this.mc.renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/entities/skeletal_builder.png"));//TODO: cache?


                    EnumFacing rotP = enumFacing.getOpposite();
                    if (enumFacing.getAxis() == facing.getAxis()) {
                        rotP = enumFacing == facing ? EnumFacing.DOWN : EnumFacing.UP;
                    }

                    float rotation;

                    if (enumFacing.getAxis() != facing.getAxis()) { //Interpolate between the 2 axis
                        EnumFacing from = rotP;
                        EnumFacing to = rotP.rotateY().getOpposite();

                        if (teRot >= 270) {
                            from = rotP.rotateY();
                            to = rotP;
                        } else if (teRot >= 180) {
                            from = rotP.getOpposite();
                            to = rotP.rotateY();
                        } else if (teRot >= 90) {
                            from = rotP.rotateY().getOpposite();
                            to = rotP.getOpposite();
                        }

                        float fromRot = this.getRotation(from, facing, cube, teRot);
                        float toRot = this.getRotation(to, facing, cube, teRot);

                        rotation = fromRot + (toRot - fromRot) * (teRot % 90F) / 90F;
                    } else {
                        rotation = this.getRotation(rotP, facing, cube, teRot);
                    }

                    switch (enumFacing.getAxis()) {
                        case X:
                            GlStateManager.rotate(enumFacing == EnumFacing.EAST ? 90F : 270F, 0, 0, 1);
                            break;
                        case Y:
                            GlStateManager.rotate(enumFacing == EnumFacing.UP ? 180F : 0F, 1, 0, 0);
                            break;
                        case Z:
                            GlStateManager.rotate(enumFacing == EnumFacing.NORTH ? 90F : 270F, 1, 0, 0);
                            break;
                    }
                    GlStateManager.rotate(rotation, 0, 1, 0);
                    GlStateManager.translate(-poleWidth / 2F, 0, -poleWidth / 2F);

                    double renderPosition;
                    switch (enumFacing.getAxis()) {
                        case X:
                            renderPosition = rendererPos.x;
                            break;
                        case Y:
                            renderPosition = rendererPos.y;
                            break;
                        case Z:
                            renderPosition = rendererPos.z;
                            break;
                        default:
                            throw new IllegalArgumentException("Unacceptable facingdir " + facing.getAxis()); //Impossible ?
                    }

                    double poleLength = (cubeEndPos - renderPosition) * -poleFacing.getFacing().getAxisDirection().getOffset();
                    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    //Render pole
                    buff.pos(0, 0, poleWidth).tex(0, 0).normal(0, 0, 1).endVertex();
                    buff.pos(0, poleLength, poleWidth).tex(0, poleLength).normal(0, 0, 1).endVertex();
                    buff.pos(poleWidth, poleLength, poleWidth).tex(1 / textureWidth, poleLength).normal(0, 0, 1).endVertex();
                    buff.pos(poleWidth, 0, poleWidth).tex(1 / textureWidth, 0).normal(0, 0, 1).endVertex();

                    buff.pos(0, 0, 0).tex(0, 0).normal(-1, 0, 0).endVertex();
                    buff.pos(0, poleLength, 0).tex(0, poleLength).normal(-1, 0, 0).endVertex();
                    buff.pos(0, poleLength, poleWidth).tex(1 / textureWidth, poleLength).normal(-1, 0, 0).endVertex();
                    buff.pos(0, 0, poleWidth).tex(1 / textureWidth, 0).normal(-1, 0, -1).endVertex();

                    buff.pos(poleWidth, 0, 0).tex(0, 0).normal(0, 0, -1).endVertex();
                    buff.pos(poleWidth, poleLength, 0).tex(0, poleLength).normal(0, 0, -1).endVertex();
                    buff.pos(0, poleLength, 0).tex(1 / textureWidth, poleLength).normal(0, 0, -1).endVertex();
                    buff.pos(0, 0, 0).tex(1 / textureWidth, 0).normal(0, 0, -1).endVertex();

                    buff.pos(poleWidth, 0, poleWidth).tex(0, 0).normal(1, 0, 0).endVertex();
                    buff.pos(poleWidth, poleLength, poleWidth).tex(0, poleLength).normal(1, 0, 0).endVertex();
                    buff.pos(poleWidth, poleLength, 0).tex(1 / textureWidth, poleLength).normal(1, 0, 0).endVertex();
                    buff.pos(poleWidth, 0, 0).tex(1 / textureWidth, 0).normal(1, 0, 0).endVertex();

                    tes.draw();
                    GlStateManager.translate(poleWidth / 2F, poleLength, poleWidth / 2F);
                    this.renderCube(baseWidth, baseHeight, textureWidth, textureHeight, 1, 0);
                    GlStateManager.translate(0, baseHeight, 0);
                    this.renderCube(notchWidth, notchHeight, textureWidth, textureHeight, 1, 6);
                    GlStateManager.popMatrix();
                }
            }
        }
        GlStateManager.popMatrix();
    }

    private static void setVisability(DinosaurEntity entity, TabulaModel tabulaModel) {
        Dinosaur dinosaur = entity.getDinosaur();
        Map<String, List<ModelRenderer>> modelChildMap = Maps.newHashMap();
        List<String> modelList = Lists.newArrayList();
        for (String s : dinosaur.getSkeletalInformation().getIndividualBones()) {
            modelList.addAll(dinosaur.getSkeletalInformation().getBoneToModelMap().get(s));
        }
        SkeletalBuilderCompoent compoent = entity.getOrNull(EntityComponentTypes.SKELETAL_BUILDER);
        if(compoent != null) {
            int id = compoent.modelIndex % (modelList.size() + 1);
            List<ModelRenderer> nonHiddenCubes = Lists.newArrayList();
            if(id != 0) {
                String currentState = modelList.get(id - 1);
                List<String> activeStates = Lists.newArrayList();
                for (int i = 0; i < modelList.size(); i++) {
                    String model = modelList.get(i);
                    modelChildMap.put(model, MoreTabulaUtils.getAllChildren(tabulaModel.getCube(model), modelList));
                    if(i <= modelList.indexOf(currentState)) {
                        activeStates.add(model);
                    }
                }
                for (String activeState : activeStates) {
                    nonHiddenCubes.addAll(modelChildMap.get(activeState));
                }
                for (ModelRenderer modelRenderer : tabulaModel.boxList) {
                    AdvancedModelRenderer box = (AdvancedModelRenderer) modelRenderer;
                    if(nonHiddenCubes.contains(modelRenderer)) {
                        box.scaleX = 1;
                        box.scaleY = 1;
                        box.scaleZ = 1;
                    } else {
                        box.scaleX = 0;
                        box.scaleY = 0;
                        box.scaleZ = 0;
                    }
                }
            } else {
                for (ModelRenderer modelRenderer : tabulaModel.boxList) {
                    AdvancedModelRenderer box = (AdvancedModelRenderer) modelRenderer;
                    box.scaleX = 1;
                    box.scaleY = 1;
                    box.scaleZ = 1;
                }
            }
        }
    }

    private static void resetVisability(TabulaModel tabulaModel) {
        for (ModelRenderer modelRenderer : tabulaModel.boxList) {
            AdvancedModelRenderer box = (AdvancedModelRenderer) modelRenderer;
            box.scaleX = 1;
            box.scaleY = 1;
            box.scaleZ = 1;
        }
    }

    private void renderCube(double width, double height, float textureWidth, float textureHeight, int uOffset, int vOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-width / 2F, 0, -width / 2F);

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        //Chunks of code are in U-D-N-E-S-W order

        double uFrom = uOffset/textureWidth;
        double vFrom = vOffset/textureHeight;
        double uTo = uFrom+width*16F/textureWidth;
        double vTo = vFrom+width*16F/textureHeight;

        buff.pos(0, height, 0).tex(uFrom, vFrom).normal(0, 1, 0).endVertex();
        buff.pos(0, height, width).tex(uFrom, vTo).normal(0, 1, 0).endVertex();
        buff.pos(width, height, width).tex(uTo, vTo).normal(0, 1, 0).endVertex();
        buff.pos(width, height, 0).tex(uTo, vFrom).normal(0, 1, 0).endVertex();

        buff.pos(width, 0, 0).tex(uFrom, vFrom).normal(0, -1, 0).endVertex();
        buff.pos(width, 0, width).tex(uFrom, vTo).normal(0, -1, 0).endVertex();
        buff.pos(0, 0, width).tex(uTo, vTo).normal(0, -1, 0).endVertex();
        buff.pos(0, 0, 0).tex(uTo, vFrom).normal(0, -1, 0).endVertex();

        vTo = vFrom+height*16F/textureHeight;

        buff.pos(width, height, 0).tex(uFrom, vFrom).normal(0, 0, -1).endVertex();
        buff.pos(width, 0, 0).tex(uFrom, vTo).normal(0, 0, -1).endVertex();
        buff.pos(0, 0, 0).tex(uTo, vTo).normal(0, 0, -1).endVertex();
        buff.pos(0, height, 0).tex(uTo, vFrom).normal(0, 0, -1).endVertex();

        buff.pos(width, height, width).tex(uFrom, vFrom).normal(1, 0, 0).endVertex();
        buff.pos(width, 0, width).tex(uFrom, vTo).normal(1, 0, 0).endVertex();
        buff.pos(width, 0, 0).tex(uTo, vTo).normal(1, 0, 0).endVertex();
        buff.pos(width, height, 0).tex(uTo, vFrom).normal(1, 0, 0).endVertex();

        buff.pos(0, height, width).tex(uFrom, vFrom).normal(0, 0, 1).endVertex();
        buff.pos(0, 0, width).tex(uFrom, vTo).normal(0, 0, 1).endVertex();
        buff.pos(width, 0, width).tex(uTo, vTo).normal(0, 0, 1).endVertex();
        buff.pos(width, height, width).tex(uTo, vFrom).normal(0, 0, 1).endVertex();

        buff.pos(0, height, 0).tex(uFrom, vFrom).normal(-1, 0, 0).endVertex();
        buff.pos(0, 0, 0).tex(uFrom, vTo).normal(-1, 0, 0).endVertex();
        buff.pos(0, 0, width).tex(uTo, vTo).normal(-1, 0, 0).endVertex();
        buff.pos(0, height, width).tex(uTo, vFrom).normal(-1, 0, 0).endVertex();

        tes.draw();
        GlStateManager.popMatrix();
    }

    private float getRotation(EnumFacing poleFacing, EnumFacing teFacing, AdvancedModelRenderer cube, float rot) {
        float rotation = 0;
        if(poleFacing.getAxis() == EnumFacing.Axis.Y) {
            rotation = rot * poleFacing.getAxisDirection().getOffset() * teFacing.getAxisDirection().getOffset();
        }
        AdvancedModelRenderer reference = cube;
        while(reference != null) {
            double axisRotation;
            switch (poleFacing.getAxis()) {
                case X: axisRotation = reference.rotateAngleX; break;
                case Y: axisRotation = reference.rotateAngleY; break;
                case Z: axisRotation = reference.rotateAngleZ; break;
                default: throw new IllegalArgumentException("Unacceptable facingdir " + teFacing.getAxis());
            }
            rotation += teFacing.getAxisDirection().getOffset() * poleFacing.getAxisDirection().getOffset() * Math.toDegrees(axisRotation);
            reference = reference.getParent();
        }
        return rotation;
    }

    @Override
    public boolean isGlobalRenderer(SkeletalBuilderBlockEntity te) {
        return true;
    }
}
