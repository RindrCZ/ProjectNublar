package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.EggPrinterGui;
import net.dumbcode.projectnublar.client.gui.tab.TabListInformation;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.recipes.EggPrinterRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import java.util.List;

public class EggPrinterBlockEntity extends MachineModuleBlockEntity<EggPrinterBlockEntity> {
    @Override
    protected int getInventorySize() {
        return 5;
    }

    @Override
    protected List<MachineRecipe<EggPrinterBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(EggPrinterRecipe.INSTANCE);
    }

    @Override
    protected EggPrinterBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<EggPrinterBlockEntity>> createProcessList() {
        return Lists.newArrayList(
                new MachineProcess<>(new int[]{0, 1, 2}, new int[]{3, 4})
        );
    }

    @Override
    public GuiScreen createScreen(EntityPlayer player, TabListInformation info, int tab) {
        return new EggPrinterGui(player, this, info, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        return new MachineModuleContainer(player, 84, 176,
                new MachineModuleSlot(this, 0, 29, 15),
                new MachineModuleSlot(this, 1, 29, 33),
                new MachineModuleSlot(this, 2, 29, 51),
                new MachineModuleSlot(this, 3, 129, 24),
                new MachineModuleSlot(this, 4, 129, 42)
        );
    }
}