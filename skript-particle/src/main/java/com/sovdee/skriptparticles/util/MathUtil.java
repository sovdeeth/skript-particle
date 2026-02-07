package com.sovdee.skriptparticles.util;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MathUtil {
    public static final double PHI = Math.PI * (3.0 - Math.sqrt(5.0));
    public static final double PHI_RECIPROCAL = 1.0 / PHI;
    public static final double PHI_SQUARED = PHI * PHI;
    public static final double[] SPHERE_THETA_COS = new double[4096];
    public static final double[] SPHERE_THETA_SIN = new double[4096];
    public static final double EPSILON = 0.0001;

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
        Set<Vector> points = new LinkedHashSet<>();
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

    public static Set<Vector> calculateCircle(double radius, double particleDensity, double cutoffAngle) {
        Set<Vector> points = new LinkedHashSet<>();
        double stepSize = particleDensity / radius;
        for (double theta = 0; theta < cutoffAngle; theta += stepSize) {
            points.add(new Vector(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
        return points;
    }

    public static Set<Vector> calculateDisc(double radius, double particleDensity, double cutoffAngle) {
        Set<Vector> points = new LinkedHashSet<>();
        for (double subRadius = particleDensity; subRadius < radius; subRadius += particleDensity) {
            points.addAll(calculateCircle(subRadius, particleDensity, cutoffAngle));
        }
        points.addAll(calculateCircle(radius, particleDensity, cutoffAngle));
        return points;
    }

    public static Set<Vector> calculateHelix(double radius, double height, double slope, int direction, double particleDensity) {
        Set<Vector> points = new LinkedHashSet<>();
        if (radius <= 0 || height <= 0) {
            return points;
        }
        double loops = Math.abs(height / slope);
        double length = slope * slope + radius * radius;
        double stepSize = particleDensity / length;
        for (double t = 0; t < loops; t += stepSize) {
            double x = radius * Math.cos(direction * t);
            double z = radius * Math.sin(direction * t);
            points.add(new Vector(x, t * slope, z));
        }
        return points;
    }

    public static Set<Vector> calculateLine(Vector start, Vector end, double particleDensity) {
        Set<Vector> points = new LinkedHashSet<>();
        Vector direction = end.clone().subtract(start);
        double length = direction.length();
        double step = length / Math.round(length / particleDensity);
        direction.normalize().multiply(step);

        for (double i = 0; i <= (length / step); i++) {
            points.add(start.clone().add(direction.clone().multiply(i)));
        }
        return points;
    }

    public static Set<Vector> calculateRegularPolygon(double radius, double angle, double particleDensity, boolean wireframe) {
        angle = Math.max(angle, MathUtil.EPSILON);

        Set<Vector> points = new LinkedHashSet<>();
        double apothem = radius * Math.cos(angle / 2);
        double radiusStep = radius / Math.round(apothem / particleDensity);
        if (wireframe) {
            radiusStep = 2 * radius;
        } else {
            points.add(new Vector(0, 0, 0));
        }
        for (double subRadius = radius; subRadius >= 0; subRadius -= radiusStep) {
            Vector vertex = new Vector(subRadius, 0, 0);
            for (double i = 0; i < 2 * Math.PI; i += angle) {
                points.addAll(calculateLine(vertex.clone().rotateAroundY(i), vertex.clone().rotateAroundY(i + angle), particleDensity));
            }
        }
        return points;
    }

    public static Set<Vector> calculateRegularPrism(double radius, double angle, double height, double particleDensity, boolean wireframe) {
        Set<Vector> points = new LinkedHashSet<>();
        Vector vertex = new Vector(radius, 0, 0);
        for (double i = 0; i < 2 * Math.PI; i += angle) {
            Vector currentVertex = vertex.clone().rotateAroundY(i);
            for (Vector vector : calculateLine(currentVertex, vertex.clone().rotateAroundY(i + angle), particleDensity)) {
                points.add(vector);
                if (wireframe) {
                    points.add(vector.clone().setY(height));
                } else {
                    points.addAll(calculateLine(vector, vector.clone().setY(height), particleDensity));
                }
            }
            if (wireframe)
                points.addAll(calculateLine(currentVertex, currentVertex.clone().setY(height), particleDensity));
        }
        return points;
    }

    public static Set<Vector> connectPoints(List<Vector> points, double particleDensity) {
        Set<Vector> connectedPoints = new LinkedHashSet<>();
        for (int i = 0; i < points.size() - 1; i++) {
            connectedPoints.addAll(calculateLine(points.get(i), points.get(i + 1), particleDensity));
        }
        return connectedPoints;
    }

    private static double ellipseCircumference(double r1, double r2) {
        double a = Math.max(r1, r2);
        double b = Math.min(r1, r2);
        double h = Math.pow(a - b, 2) / Math.pow(a + b, 2);
        return Math.PI * (a + b) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
    }

    public static List<Vector> calculateEllipse(double r1, double r2, double particleDensity, double cutoffAngle) {
        List<Vector> points = new ArrayList<>();
        double circumference = ellipseCircumference(r1, r2);

        int steps = (int) Math.round(circumference / particleDensity);
        double theta = 0;
        double angleStep = 0;
        for (int i = 0; i < steps; i++) {
            if (theta > cutoffAngle) {
                break;
            }
            points.add(new Vector(r1 * Math.cos(theta), 0, r2 * Math.sin(theta)));
            double dx = r1 * Math.sin(theta + 0.5 * angleStep);
            double dy = r2 * Math.cos(theta + 0.5 * angleStep);
            angleStep = particleDensity / Math.sqrt(dx * dx + dy * dy);
            theta += angleStep;
        }
        return points;
    }

    public static Set<Vector> calculateEllipticalDisc(double r1, double r2, double particleDensity, double cutoffAngle) {
        Set<Vector> points = new LinkedHashSet<>();
        int steps = (int) Math.round(Math.max(r1, r2) / particleDensity);
        double r;
        for (double i = 1; i <= steps; i += 1) {
            r = i / steps;
            points.addAll(calculateEllipse(r1 * r, r2 * r, particleDensity, cutoffAngle));
        }
        return points;
    }

    public static Set<Vector> calculateCylinder(double r1, double height, double particleDensity, double cutoffAngle) {
        Set<Vector> points = calculateDisc(r1, particleDensity, cutoffAngle);
        points.addAll(points.stream().map(v -> v.clone().setY(height)).collect(Collectors.toSet()));
        // wall
        Set<Vector> wall = calculateCircle(r1, particleDensity, cutoffAngle);
        points.addAll(fillVertically(wall, height, particleDensity));
        return points;
    }

    public static Set<Vector> calculateCylinder(double r1, double r2, double height, double particleDensity, double cutoffAngle) {
        Set<Vector> points = calculateEllipticalDisc(r1, r2, particleDensity, cutoffAngle);
        points.addAll(points.stream().map(v -> v.clone().setY(height)).collect(Collectors.toSet()));
        // wall
        Set<Vector> wall = new LinkedHashSet<>(calculateEllipse(r1, r2, particleDensity, cutoffAngle));
        points.addAll(fillVertically(wall, height, particleDensity));
        return points;
    }

    public static Set<Vector> fillVertically(Set<Vector> vectors, double height, double particleDensity) {
        Set<Vector> points = new LinkedHashSet<>(vectors);
        double heightStep = height / Math.round(height / particleDensity);
        for (double i = 0; i < height; i += heightStep) {
            for (Vector vector : vectors) {
                points.add(vector.clone().setY(i));
            }
        }
        return points;
    }

    public static Set<Vector> calculateHeart(double length, double width, double eccentricity, double particleDensity) {
        Set<Vector> points = new LinkedHashSet<>();
        double angleStep = 4 / 3.0 * particleDensity / (width + length);
        for (double theta = 0; theta < Math.PI * 2; theta += angleStep) {
            double x = width * Math.pow(Math.sin(theta), 3);
            double y = length * (Math.cos(theta) - 1 / eccentricity * Math.cos(2 * theta) - 1.0 / 6 * Math.cos(3 * theta) - 1.0 / 16 * Math.cos(4 * theta));
            points.add(new Vector(x, 0, y));
        }
        return points;
    }

    public static Set<Vector> calculateStar(double innerRadius, double outerRadius, double angle, double particleDensity) {
        Set<Vector> points = new LinkedHashSet<>();
        Vector outerVertex = new Vector(outerRadius, 0, 0);
        Vector innerVertex = new Vector(innerRadius, 0, 0);
        for (double theta = 0; theta < 2 * Math.PI; theta += angle) {
            Vector currentVertex = outerVertex.clone().rotateAroundY(theta);
            points.addAll(calculateLine(currentVertex, innerVertex.clone().rotateAroundY(theta + angle / 2), particleDensity));
            points.addAll(calculateLine(currentVertex, innerVertex.clone().rotateAroundY(theta - angle / 2), particleDensity));
        }
        return points;
    }

    public static List<List<Vector>> batch(Collection<Vector> toDraw, double millisecondsPerPoint) {
        List<List<Vector>> batches = new ArrayList<>();
        double totalDuration = 0;
        Iterator<Vector> pointsIterator = toDraw.iterator();
        while (pointsIterator.hasNext()) {
            List<Vector> batch = new ArrayList<>();
            while (totalDuration < 50 && pointsIterator.hasNext()) {
                totalDuration += millisecondsPerPoint;
                batch.add(pointsIterator.next());
            }
            totalDuration -= 50;
            batches.add(batch);
        }
        return batches;
    }
}
