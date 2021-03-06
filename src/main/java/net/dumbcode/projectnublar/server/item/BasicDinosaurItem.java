package net.dumbcode.projectnublar.server.item;

import lombok.Getter;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.item.Item;

public class BasicDinosaurItem extends Item implements DinosaurProvider {
    @Getter private final Dinosaur dinosaur;

    public BasicDinosaurItem(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
    }
}
