package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.client.gui.tablet.BackgroundTabletScreen;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages.PhotoBackgroundSetup;
import net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages.SetupPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class S36SyncBackgroundIcons implements IMessage {

    private boolean global;
    private List<TabletBGImageHandler.IconEntry> entryList;

    public S36SyncBackgroundIcons() {
    }


    public S36SyncBackgroundIcons(boolean global, List<TabletBGImageHandler.IconEntry> entryList) {
        this.global = global;
        this.entryList = entryList;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.global = buf.readBoolean();
        this.entryList = new ArrayList<>();

        short len = buf.readShort();
        for (int i = 0; i < len; i++) {
            String playerUUID = ByteBufUtils.readUTF8String(buf);
            String imageHash = ByteBufUtils.readUTF8String(buf);
            byte[] image = new byte[buf.readInt()];
            buf.readBytes(image);

            this.entryList.add(new TabletBGImageHandler.IconEntry(playerUUID, imageHash, image));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.global);

        buf.writeShort(this.entryList.size());
        for (TabletBGImageHandler.IconEntry entry : this.entryList) {
            ByteBufUtils.writeUTF8String(buf, entry.getUploaderUUID());
            ByteBufUtils.writeUTF8String(buf, entry.getImageHash());
            buf.writeInt(entry.getImageData().length);
            buf.writeBytes(entry.getImageData());
        }
    }

    public static class Handler extends WorldModificationsMessageHandler<S36SyncBackgroundIcons, S36SyncBackgroundIcons> {

        @Override
        protected void handleMessage(S36SyncBackgroundIcons message, MessageContext ctx, World world, EntityPlayer player) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if(screen instanceof BackgroundTabletScreen) {
                SetupPage page = ((BackgroundTabletScreen) screen).getSetupPage();
                if(page instanceof PhotoBackgroundSetup) {
                    ((PhotoBackgroundSetup) page).loadEntries(message.global, message.entryList);
                }
            }
        }
    }
}
