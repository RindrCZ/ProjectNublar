package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.utils.TrackingTabletIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C23ConfirmTrackingTablet implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler extends WorldModificationsMessageHandler<C23ConfirmTrackingTablet, C23ConfirmTrackingTablet> {

        @Override
        protected void handleMessage(C23ConfirmTrackingTablet message, MessageContext ctx, World world, EntityPlayer player) {
            if (TrackingTabletIterator.PLAYER_TO_TABLET_MAP.containsKey(player.getUniqueID())) {
                TrackingTabletIterator.PLAYER_TO_TABLET_MAP.get(player.getUniqueID()).start();
            }
        }
    }
}
