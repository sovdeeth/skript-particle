package com.sovdee.skriptparticle.particles;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.List;

public class CustomParticle {

    private Particle particle;
    private int count;
    private Object data = null;
    private Vector offset = new Vector(0,0,0);
    private double extra = 0;
    private boolean force = false;

    public CustomParticle(Particle particle, int count) {
        this.particle = particle;
        this.count = count;
    }

    public CustomParticle(Particle particle, int count, @Nullable Vector offset, @Nullable Number extra,  @Nullable Object data) {
        this.particle = particle;
        this.count = count;
        this.offset = offset == null ? new Vector(0,0,0) : offset;
        this.extra = extra == null ? 0 : extra.doubleValue();
        this.data = data;
    }

    public CustomParticle(Particle particle, int count, @Nullable Vector offset, @Nullable Number extra,  @Nullable Object data, boolean force) {
        this.particle = particle;
        this.count = count;
        this.offset = offset == null ? new Vector(0,0,0) : offset;
        this.extra = extra == null ? 0 : extra.doubleValue();
        this.force = force;
        this.data = data;
    }

    public void drawParticle(Location location) {
        drawParticle(location, (Player[]) null);
    }

    public void drawParticle(Location location, List<Player> players){
        drawParticle(location, players.toArray(new Player[0]));
    }

    public void drawParticle(Location location, @Nullable Player[] players){
        if (players == null) {
            World world = location.getWorld();
            if (world == null) return;
            world.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra, data, force);
        } else {
            for (Player player : players) {
                assert player != null;
                player.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra, data);
            }
        }
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Vector getOffset() {
        return offset;
    }

    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    public double getExtra() {
        return extra;
    }

    public void setExtra(double extra) {
        this.extra = extra;
    }

    public boolean isForced() {
        return force;
    }

    public void setForced(boolean force) {
        this.force = force;
    }

    static {
        Classes.registerClass(new ClassInfo<>(CustomParticle.class, "customparticle")
                .user("customparticles?")
                .name("Custom Particle Data")
                .description("Represents a fully defined particle, including count, speed, extra, optionData, and offset.")
                .examples("on load:", "\tset {_particle} to 5 flame particles with offset vector(1,1,1) and extra 0")
//                .defaultExpression(new EventValueExpression<>(Circle.class))
                .parser(new Parser<>() {
                    @Override
                    public CustomParticle parse(String input, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(CustomParticle o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(CustomParticle particle) {
                        return "customParticle:" + particle.particle;
                    }
                })
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(CustomParticle particle) {
                        Fields fields = new Fields();
                        fields.putObject("particle", particle.getParticle());
                        fields.putPrimitive("count", particle.getCount());
                        fields.putObject("offset", particle.getOffset());
                        fields.putPrimitive("extra", particle.getExtra());
                        fields.putObject("data", particle.getData());
                        fields.putPrimitive("force", particle.isForced());
                        return fields;
                    }

                    @Override
                    public CustomParticle deserialize(Fields fields) throws StreamCorruptedException {
                        Particle particle = fields.getObject("particle", Particle.class);
                        int count = fields.getPrimitive("count", int.class);
                        Vector offset = fields.getObject("offset", Vector.class);
                        double extra = fields.getPrimitive("extra", double.class);
                        Object data = fields.getObject("data", Object.class);
                        boolean force = fields.getPrimitive("force", boolean.class);
                        return new CustomParticle(particle, count, offset, extra, data, force);
                    }

                    @Override
                    public void deserialize(CustomParticle particle, Fields fields) throws StreamCorruptedException, NotSerializableException {
                        assert false;
                    }

                    @Override
                    public boolean mustSyncDeserialization() {
                        return false;
                    }

                    @Override
                    protected boolean canBeInstantiated() {
                        return false;
                    }

                }));

        Converters.registerConverter(Particle.class, CustomParticle.class, particle -> new CustomParticle(particle, 1));
    }
}
