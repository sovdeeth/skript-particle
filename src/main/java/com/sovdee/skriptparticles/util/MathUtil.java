package com.sovdee.skriptparticles.util;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Set<Vector> calculateFibonacciSphere(int pointCount, double radius) {
        return calculateFibonacciSphere(pointCount, radius, Math.PI);
    }
    public static Set<Vector> calculateFibonacciSphere(int pointCount, double radius, double angleCutoff) {
        Set<Vector> points = new HashSet<>();
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

    public static Set<Vector> calculateCircle(double radius, double particleDensity, double cutoffAngle){
        Set<Vector> points = new HashSet<>();
        double stepSize = particleDensity / radius;
        for (double theta = 0; theta < cutoffAngle; theta += stepSize) {
            points.add(new Vector(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
        return points;
    }

    public static Set<Vector> calculateDisc(double radius, double particleDensity, double cutoffAngle){
        Set<Vector> points = new HashSet<>();
        for (double subRadius = particleDensity; subRadius < radius; subRadius += particleDensity) {
            points.addAll(calculateCircle(subRadius, particleDensity, cutoffAngle));
        }
        points.addAll(calculateCircle(radius, particleDensity, cutoffAngle));
        return points;
    }

    public static Set<Vector> calculateLine(Vector start, Vector end, double particleDensity){
        Set<Vector> points = new HashSet<>();
        Vector direction = end.clone().subtract(start);
        double length = direction.length();
        double step = length / Math.round(length / particleDensity);
        direction.normalize().multiply(step);

        for (double i = 0; i <= (length / step); i++) {
            points.add(start.clone().add(direction.clone().multiply(i)));
        }
        return points;
    }

    public static Set<Vector> calculateRegularPolygon(double radius, double angle, double particleDensity) {
        Set<Vector> points = new HashSet<>();
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

    public static List<Vector> calculateEllipse(double r1, double r2, double particleDensity, double cutoffAngle) {
        List<Vector> points = new ArrayList<>();
        double theta = 0.0;
        double twoPi = Math.PI*2.0;
        double deltaTheta = 0.0001;
        double numIntegrals = Math.round(twoPi/deltaTheta);
        double circ = 0.0;
        double dpt;

        /* integrate over the elipse to get the circumference */
        for( int i=0; i < numIntegrals; i++ ) {
            theta += i*deltaTheta;
            dpt = computeDpt( r1, r2, theta);
            circ += dpt;
        }

        int n = (int) Math.round(circ/particleDensity/10000);
        int nextPoint = 0;
        double run = 0, x, z, subIntegral;
        theta = 0.0;

        for( int i=0; i < numIntegrals; i++ ) {
            theta += deltaTheta;
            if (theta > cutoffAngle) break;
            subIntegral = n*run/circ;
            if( (int) subIntegral >= nextPoint ) {
                x = r1 * Math.cos(theta);
                z = r2 * Math.sin(theta);
                points.add(new Vector(x, 0, z));
                nextPoint++;
            }
            run += computeDpt(r1, r2, theta);
        }
        return points;
    }

    public static double computeDpt( double r1, double r2, double theta ) {
        double dpt_sin = Math.pow(r1*Math.sin(theta), 2.0);
        double dpt_cos = Math.pow( r2*Math.cos(theta), 2.0);
        return Math.sqrt(dpt_sin + dpt_cos);
    }
}
