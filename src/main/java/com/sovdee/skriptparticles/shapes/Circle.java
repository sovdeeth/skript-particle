package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Circle extends RadialShape {

    protected double radius;

    public Circle (double radius){
        super();
        this.style = Style.OUTLINE;
        this.radius = radius;
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateCircle(radius, particleDensity, 2*Math.PI);
    }


    @Override
    public Set<Vector> generateSurface() {
        return MathUtil.calculateDisc(radius, particleDensity, 2*Math.PI);
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleDensity = switch (style) {
            case OUTLINE -> 2 * Math.PI * radius / particleCount;
            case SURFACE, FILL -> Math.sqrt(Math.PI * radius * radius / particleCount);
        };
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        Sphere sphere = new Sphere(radius);
        this.copyTo(sphere);
        return sphere;
    }

    public String toString(){
        return style.toString() + " circle with radius " + this.radius + " and density " + this.particleDensity;
    }

}
