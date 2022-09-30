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
}
