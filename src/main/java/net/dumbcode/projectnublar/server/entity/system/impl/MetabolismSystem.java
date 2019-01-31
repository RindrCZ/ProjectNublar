package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.EntityProperties;
import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;

public class MetabolismSystem implements EntitySystem
{

    private MetabolismComponent[] metabolism = new MetabolismComponent[0];
    private DinosaurComponent[] dinosaurs = new DinosaurComponent[0];
    private Entity[] entities = new Entity[0];

    @Override
    public void populateBuffers(EntityManager manager)
    {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.METABOLISM, EntityComponentTypes.DINOSAUR);
        metabolism = family.populateBuffer(EntityComponentTypes.METABOLISM, metabolism);
        dinosaurs = family.populateBuffer(EntityComponentTypes.DINOSAUR);
        entities = family.getEntities();
    }

    @Override
    public void update()
    {
        for (int i = 0; i < metabolism.length; i++)
        {
            if(entities[i].ticksExisted % 20 == 0)
            {
                Dinosaur dinosaur = dinosaurs[i].dinosaur;
                MetabolismComponent meta = metabolism[i];
                EntityProperties properties = dinosaur.getEntityProperties();

                meta.food -= properties.getFoodRate();
                meta.water -= properties.getWaterRate();

                // TODO: Hurt the entity when it has no more food / water left.
                // Waiting to do this after eating and drinking ai are done.
            }
        }
    }
}