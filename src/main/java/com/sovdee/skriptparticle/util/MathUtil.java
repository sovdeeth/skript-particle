package com.sovdee.skriptparticle.util;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MathUtil {
    public static final double PHI = Math.PI * (3.0 - Math.sqrt(5.0));
    public static final double PHI_RECIPROCAL = 1.0 / PHI;
    public static final double PHI_SQUARED = PHI * PHI;
    public static final double[] SPHERE_THETA_COS = new double[4096];
    public static final double[] SPHERE_THETA_SIN = new double[4096];
    static {
        for (int i = 0; i < SPHERE_THETA_COS.length; i++) {
            SPHERE_THETA_COS[i] = Math.cos(MathUtil.PHI * i);
            SPHERE_THETA_SIN[i] = Math.sin(MathUtil.PHI * i);
        }
    }

    public static List<Vector> calculateFibonacciSphere(int pointCount, double radius) {
        return calculateFibonacciSphere(pointCount, radius, Math.PI);
    }
    public static List<Vector> calculateFibonacciSphere(int pointCount, double radius, double angleCutoff) {
        List<Vector> points = new ArrayList<>();
        double y = 1;
        if (angleCutoff > Math.PI) angleCutoff = Math.PI;
        double yLimit = Math.cos(angleCutoff);

        double yStep = 2.0 / pointCount;
        int preCompPoints = Math.min(pointCount, MathUtil.SPHERE_THETA_COS.length);
        // Use precomputed points if possible
        for (int i = 0; i < preCompPoints; i++) {
            double r = Math.sqrt(1 - y * y) * radius;
            points.add(new Vector(r * MathUtil.SPHERE_THETA_COS[i], y * radius, r * MathUtil.SPHERE_THETA_SIN[i]));
            y -= yStep;
            if (y <= yLimit) {
                return points;
            }
        }
        // If we have more points than we precomputed, we need to calculate the rest
        if (pointCount > preCompPoints) {
            for (int i = preCompPoints; i < pointCount; i++) {
                double r = Math.sqrt(1 - y * y) * radius;
                double theta = MathUtil.PHI * i;
                points.add(new Vector(r * Math.cos(theta), y * radius, r * Math.sin(theta)));
                y -= yStep;
                if (y <= yLimit) {
                    return points;
                }
            }
        }
        return points;
    }

    public static List<Vector> calculateCircle(double radius, double particleDensity, double cutoffAngle){
        List<Vector> points = new ArrayList<>();
        double stepSize = particleDensity / radius;
        for (double theta = 0; theta < cutoffAngle; theta += stepSize) {
            points.add(new Vector(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
        return points;
    }

    public static List<Vector> calculateDisc(double radius, double particleDensity, double cutoffAngle){
        List<Vector> points = new ArrayList<>();
        for (double subRadius = particleDensity; subRadius < radius; subRadius += particleDensity) {
            points.addAll(calculateCircle(subRadius, particleDensity, cutoffAngle));
        }
        points.addAll(calculateCircle(radius, particleDensity, cutoffAngle));
        return points;
    }

    public static List<Vector> calculateLine(Vector start, Vector end, double particleDensity){
        List<Vector> points = new ArrayList<>();
        Vector direction = end.clone().subtract(start);
        double length = direction.length();
        direction.normalize().multiply(particleDensity);

        for (double i = 0; i < (length / particleDensity); i++) {
            points.add(start.clone().add(direction.clone().multiply(i)));
        }
        return points;
    }

    public static List<Vector> calculateRegularPolygon(double radius, double angle, double particleDensity) {
        List<Vector> points = new ArrayList<>();
        Vector vertex = new Vector(radius, 0, 0);
        for (double i = 0; i < 2*Math.PI; i += angle) {
            points.addAll(calculateLine(vertex.clone().rotateAroundY(i), vertex.clone().rotateAroundY(i + angle), particleDensity));
        }
        return points;
    }

    public static List<Vector> connectPoints(List<Vector> points, double particleDensity) {
        List<Vector> connectedPoints = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            connectedPoints.addAll(calculateLine(points.get(i), points.get(i + 1), particleDensity));
        }
        return connectedPoints;
    }

    public static List<Vector> calculateRectSurface(double halfLength, double halfWidth, double particleDensity) {
        List<Vector> points = new ArrayList<>();
        for (double x = -halfLength; x < halfLength; x += particleDensity) {
            for (double z = -halfWidth; z < halfWidth; z += particleDensity) {
                points.add(new Vector(x, 0, z));
            }
        }
        return points;
    }

    public static List<Vector> calculateRectOutline(double halfLength, double halfWidth, double particleDensity) {
        List<Vector> points = new ArrayList<>();
        for (double x = -halfLength; x < halfLength; x += particleDensity) {
            points.add(new Vector(x, 0, -halfWidth));
            points.add(new Vector(x, 0, halfWidth));
        }
        for (double z = -halfWidth; z < halfWidth; z += particleDensity) {
            points.add(new Vector(-halfLength, 0, z));
            points.add(new Vector(halfLength, 0, z));
        }
        return points;
    }
}
