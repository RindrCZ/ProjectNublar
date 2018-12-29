package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Vector3f;

import java.util.Map;

import static lombok.EqualsAndHashCode.Include;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Connection {
    @Include private final ConnectionType type;
    @Include private final BlockPos from;
    @Include private final BlockPos to;

    @Include private final BlockPos previous;
    @Include private final BlockPos next;

    @Include private Map<Integer, Boolean> signMap = Maps.newHashMap();

    private final BlockPos position;
    private final int compared;
    private Cache[] rendercache;
    private final boolean[] indexs;

    public Connection(ConnectionType type, BlockPos from, BlockPos to, BlockPos previous, BlockPos next, BlockPos position) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.previous = previous;
        this.next = next;
        this.position = position;

        this.compared = this.from.getX() == this.to.getX() ? this.to.getZ() - this.from.getZ() : this.from.getX() - this.to.getX();

        this.rendercache = new Cache[this.type.getOffsets().length];

        this.indexs = new boolean[type.getOffsets().length];
        for (int i = 0; i < type.getOffsets().length; i++) {
            this.indexs[i] = true;
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("type", this.type.ordinal());
        nbt.setLong("from", this.getFrom().toLong());
        nbt.setLong("to", this.getTo().toLong());
        for (int i = 0; i < this.type.getOffsets().length; i++) {
            nbt.setBoolean("sign_" + i, this.signMap.getOrDefault(i, false));
        }
        NBTTagCompound ind = new NBTTagCompound();
        ind.setInteger("size", this.indexs.length);
        for (int i = 0; i < this.indexs.length; i++) {
            ind.setBoolean(String.valueOf(i), this.indexs[i]);
        }
        nbt.setTag("indexes", ind);
        nbt.setLong("previous", this.previous.toLong());
        nbt.setLong("next", this.next.toLong());
        return nbt;
    }

    public static Connection fromNBT(NBTTagCompound nbt, TileEntity tileEntity) {
        Connection connection =  new Connection(ConnectionType.getType(nbt.getInteger("type")), BlockPos.fromLong(nbt.getLong("from")), BlockPos.fromLong(nbt.getLong("to")), BlockPos.fromLong(nbt.getLong("previous")), BlockPos.fromLong(nbt.getLong("next")), tileEntity.getPos());
        for (int i = 0; i < connection.type.getOffsets().length; i++) {
            connection.getSignMap().put(i, nbt.getBoolean("sign_" + i));
        }
        NBTTagCompound ind = nbt.getCompoundTag("indexes");
        int size = ind.getInteger("size");
        for (int i = 0; i < size; i++) {
            connection.indexs[i] = ind.getBoolean(String.valueOf(i));
        }
        return connection;
    }


    public BlockPos getMin() {
        return this.compared < 0 ? this.to : this.from;
    }

    public BlockPos getMax() {
        return this.compared >= 0 ? this.to : this.from;
    }

    public void breakIndex(int index) {
        this.indexs[index] = false;
    }

    public Cache getCache(int index) {
        return this.rendercache[index] = this.getOrGenCache(this.rendercache[index], index);
    }

    private Cache getOrGenCache(Cache cache, int index) {
        if(cache != null) {
            return cache;
        }
        double halfthick = this.type.getCableWidth() / 2F;

        BlockPos to = this.getMax();
        BlockPos from = this.getMin();

        double posdist = this.distance(from, to.getX()+0.5F, to.getZ()+0.5F);
        double yrange = (to.getY() - from.getY()) / posdist;
        double[] in = LineUtils.intersect(this.position, from, to, this.type.getOffsets()[index]);
        if(in != null) {
            double tangrad = in[1] == in[0] ? Math.PI/2D : Math.atan((in[2] - in[3]) / (in[1] - in[0]));
            double xcomp = halfthick * Math.sin(tangrad);
            double zcomp = halfthick * Math.cos(tangrad);
            double tangrady = posdist == 0 ? Math.PI/2D : Math.atan((to.getY() - from.getY()) / posdist);
            double yxzcomp = Math.sin(tangrady) ;
            double[] ct = new double[] {
                    in[0] - xcomp + yxzcomp*zcomp, in[2] - zcomp - yxzcomp*xcomp,
                    in[1] - xcomp + yxzcomp*zcomp, in[3] - zcomp - yxzcomp*xcomp,
                    in[1] + xcomp + yxzcomp*zcomp, in[3] + zcomp - yxzcomp*xcomp,
                    in[0] + xcomp + yxzcomp*zcomp, in[2] + zcomp - yxzcomp*xcomp
            };
            double[] cb = new double[] {
                    in[0] - xcomp - yxzcomp*zcomp, in[2] - zcomp + yxzcomp*xcomp,
                    in[1] - xcomp - yxzcomp*zcomp, in[3] - zcomp + yxzcomp*xcomp,
                    in[1] + xcomp - yxzcomp*zcomp, in[3] + zcomp + yxzcomp*xcomp,
                    in[0] + xcomp - yxzcomp*zcomp, in[2] + zcomp + yxzcomp*xcomp
            };
            double ytop = yrange * this.distance(from, in[0], in[2]) - this.position.getY() + from.getY();
            double ybot = yrange * this.distance(from, in[1], in[3]) - this.position.getY() + from.getY();
            double len = Math.sqrt(Math.pow(ct[0] == ct[2] ? ct[1]-ct[3] : ct[0]-ct[2], 2) + (ytop-ybot)*(ytop-ybot));
            double yThick = halfthick *  Math.cos(tangrady);
            Vector3f xNorm = MathUtils.calcualeNormalF(
                    ct[2], ybot+yThick, ct[3],
                    cb[2], ybot-yThick, cb[3],
                    cb[4], ybot-yThick, cb[5]
            );
            Vector3f zNorm = MathUtils.calcualeNormalF( //outwards
                    ct[0], ytop+yThick, ct[1],
                    cb[0], ytop-yThick, cb[1],
                    cb[2], ybot-yThick, cb[3]
            );
            double sqxz = (in[1]-in[0])*(in[1]-in[0]) + (in[3]-in[2])*(in[3]-in[2]);
            return new Cache(ct, cb, in, xNorm, zNorm, ybot, ytop, len, Math.sqrt(sqxz), Math.sqrt(sqxz + (in[5]-in[4])*(in[5]-in[4])), yThick, halfthick*2);
        }
        return null;
    }

    private double distance(BlockPos from, double x, double z) {
        return Math.sqrt((from.getX()+0.5F-x)*(from.getX()+0.5F-x) + (from.getZ()+0.5F-z)*(from.getZ()+0.5F-z));
    }

    @Value public class Cache { double[] ct, cb, in; Vector3f xNorm, zNorm; double ybot, ytop, texLen, XZlen, fullLen, yThick, fullThick; }
}
