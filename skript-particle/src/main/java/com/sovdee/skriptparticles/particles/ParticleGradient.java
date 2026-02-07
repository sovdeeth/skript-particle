package com.sovdee.skriptparticles.particles;

import ch.njol.skript.util.ColorRGB;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Color;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ParticleGradient {

    private final Quaternion orientation = new Quaternion(1, 0, 0, 0);
    private final List<Point> points = new ArrayList<>();
    private boolean local = false;

    public Color calculateColour(Vector delta) {
        if (local) {
            orientation.transform(delta);
        }

        double weightTotal = 0;
        double[] rgb = new double[]{0, 0, 0};
        double weight;
        Color colour;
        for (Point point : points) {
            weight = (1 / (point.getPosition().clone().subtract(delta).length()));
            weightTotal += weight;
            colour = point.getColor();
            rgb[0] += weight * colour.getRed();
            rgb[1] += weight * colour.getGreen();
            rgb[2] += weight * colour.getBlue();
        }
        return Color.fromRGB((int) (rgb[0] / weightTotal), (int) (rgb[1] / weightTotal), (int) (rgb[2] / weightTotal));
    }

    public Quaternion getOrientation() {
        return orientation;
    }

    public void setOrientation(Quaternion orientation) {
        this.orientation.set(orientation.clone());
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points.clear();
        this.points.addAll(points);
    }

    public void addPoint(Vector position, Color color) {
        points.add(new Point(position, color));
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    @Override
    public String toString() {
        return "ParticleGradient{" +
                "orientation=" + orientation +
                ", points=" + points +
                ", local=" + local +
                '}';
    }

    public static class Point {

        private Vector position;
        private Color color;

        public Point(Vector position, Color color) {
            this.position = position;
            this.color = color;
        }

        public Point(Vector position, ColorRGB color) {
            this.position = position;
            this.color = color.asBukkitColor();
        }

        public Vector getPosition() {
            return position;
        }

        public void setPosition(Vector position) {
            this.position = position;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "position=" + position +
                    ", color=" + color +
                    '}';
        }
    }
}
