package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.DrillExtractorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Function;
import java.util.function.Predicate;

public class DrillExtractorRecipe implements MachineRecipe<DrillExtractorBlockEntity> {

    private final ResourceLocation registryName;
    private final Predicate<ItemStack> inputPredicate;
    private final Function<ItemStack, ItemStack> outputStack;
    private final int recipeTime;

    public DrillExtractorRecipe(ResourceLocation registryName, Predicate<ItemStack> inputPredicate, Function<ItemStack, ItemStack> outputStack, int recipeTime) {
        this.registryName = registryName;
        this.inputPredicate = inputPredicate;
        this.outputStack = outputStack;
        this.recipeTime = recipeTime;
    }

    @Override
    public boolean accpets(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlots()[0]);
        if(this.inputPredicate.test(inSlot)) {
            for (int i = 0; i < process.getOutputSlots().length; i++) {
                if(handler.getStackInSlot(i).getItem() == ItemHandler.EMPTY_TEST_TUBE) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getRecipeTime(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return this.recipeTime;
    }

    @Override
    public void onRecipeFinished(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlots()[0]);
        for (int i = 0; i < process.getOutputSlots().length; i++) {
            if(handler.getStackInSlot(i).getItem() == ItemHandler.EMPTY_TEST_TUBE) {
                handler.setStackInSlot(i, this.outputStack.apply(inSlot));
                inSlot.shrink(1);
                return;
            }
        }
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }
}