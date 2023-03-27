package com.sovdee.skriptparticle.elements.particles;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ParticleGradient extends ParticleBuilder {

    private final Quaternion orientation = new Quaternion(1, 0, 0, 0);
    private Vector origin = new Vector(0, 0, 0);
    private final List<ParticleGradientPoint> points = new ArrayList<>();
    private boolean local = false;

    public ParticleGradient() {
        super(Particle.REDSTONE);
    }

    public Vector origin() {
        return origin;
    }

    public ParticleGradient origin(Vector origin) {
        this.origin = origin;
        return this;
    }

    public ParticleGradient origin(Location origin) {
        this.origin = origin.toVector();
        return this;
    }

    public Quaternion orientation() {
        return orientation;
    }

    public ParticleGradient orientation(Quaternion orientation) {
        this.orientation.set(orientation);
        return this;
    }

    public Vector delta = new Vector(0, 0, 0);
    @Override
    public @NotNull ParticleBuilder spawn() {
        delta = this.location().clone().toVector().subtract(origin);
        if (local) {
            orientation.transform(delta);
        }
        this.color(calculateColor(delta));
        super.spawn();
        return this;
    }

    private Color calculateColor(Vector delta) {
        double weightTotal = 0;
        double[] rgb = new double[]{0,0,0};
        double weight;
        for (ParticleGradientPoint point : points) {
            weight = (1 / (point.position().clone().subtract(delta).length()));
            weightTotal += weight;
            rgb[0] += weight * point.color().getRed();
            rgb[1] += weight * point.color().getGreen();
            rgb[2] += weight * point.color().getBlue();
        }
        return Color.fromRGB((int) (rgb[0] / weightTotal), (int) (rgb[1] / weightTotal), (int) (rgb[2] / weightTotal));
    }

    public void isLocal(boolean b) {
        this.local = b;
    }

    public boolean isLocal() {
        return local;
    }

    public void addPoints(ParticleGradientPoint... point) {
        points.addAll(List.of(point));
    }

    public void removePoints(ParticleGradientPoint... point) {
        points.removeAll(List.of(point));
    }

    public void setPoints(ParticleGradientPoint... point) {
        points.clear();
        points.addAll(List.of(point));
    }

    public List<ParticleGradientPoint> points() {
        return points;
    }



    public static class ParticleGradientPoint {

        private Vector position;
        private Color color;

        public ParticleGradientPoint(Vector position, Color color) {
            this.position = position;
            this.color = color;
        }

        public ParticleGradientPoint(Vector position, ColorRGB color) {
            this.position = position;
            this.color = color.asBukkitColor();
        }

        public Vector position() {
            return position;
        }

        public Color color() {
            return color;
        }

        public ParticleGradientPoint position(Vector position) {
            this.position = position;
            return this;
        }

        public ParticleGradientPoint color(Color color) {
            this.color = color;
            return this;
        }
    }

    static {

        Classes.registerClass(new ClassInfo<>(ParticleGradient.class, "particlegradient")
                .user("particlegradients?")
                .name("Particle Gradient")
                .description("Represents a 3-dimensional gradient, consisting of 1 or more colours at certain points.")
                .parser(new Parser<>() {

                    @SuppressWarnings("NullableProblems")
                    @Nullable
                    @Override
                    public ParticleGradient parse(String s, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public @NotNull String toString(ParticleGradient particle, int flags) {
                        return "particle gradient";
                    }

                    @Override
                    public @NotNull String toVariableNameString(ParticleGradient particle) {
                        return "particlegradient:" + particle.toString();
                    }
                }));

        Classes.registerClass(new ClassInfo<>(ParticleGradientPoint.class, "particlegradientpoint")
                .user("particlegradientpoints?")
                        .name("Particle Gradient Point")
                        .description("Represents a single point in a gradient, consisting of a vector position and a colour.")
                        .parser(new Parser<>() {

            @SuppressWarnings("NullableProblems")
            @Nullable
            @Override
            public ParticleGradientPoint parse(String s, ParseContext context) {
                return null;
            }

            @Override
            public boolean canParse(ParseContext context) {
                return false;
            }

            @Override
            public @NotNull String toString(ParticleGradientPoint particle, int flags) {
                return "particle gradient point";
            }

            @Override
            public @NotNull String toVariableNameString(ParticleGradientPoint particle) {
                return "particlegradientpoint:" + particle.toString();
            }
        }));
}

}
