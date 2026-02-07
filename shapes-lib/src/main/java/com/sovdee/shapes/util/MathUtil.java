package com.sovdee.shapes.util;

import org.joml.Vector3d;

import java.util.ArrayList;
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

    public static Set<Vector3d> calculateFibonacciSphere(int pointCount, double radius) {
        return calculateFibonacciSphere(pointCount, radius, Math.PI);
    }

    public static Set<Vector3d> calculateFibonacciSphere(int pointCount, double radius, double angleCutoff) {
        Set<Vector3d> points = new LinkedHashSet<>();
        double y = 1;
        if (angleCutoff > Math.PI) angleCutoff = Math.PI;
        double yLimit = Math.cos(angleCutoff);

        double yStep = 2.0 / pointCount;
        int preCompPoints = Math.min(pointCount, MathUtil.SPHERE_THETA_COS.length);
        for (int i = 0; i < preCompPoints; i++) {
            double r = Math.sqrt(1 - y * y) * radius;
            points.add(new Vector3d(r * MathUtil.SPHERE_THETA_COS[i], y * radius, r * MathUtil.SPHERE_THETA_SIN[i]));
            y -= yStep;
            if (y <= yLimit) {
                return points;
            }
        }
        if (pointCount > preCompPoints) {
            for (int i = preCompPoints; i < pointCount; i++) {
                double r = Math.sqrt(1 - y * y) * radius;
                double theta = MathUtil.PHI * i;
                points.add(new Vector3d(r * Math.cos(theta), y * radius, r * Math.sin(theta)));
                y -= yStep;
                if (y <= yLimit) {
                    return points;
                }
            }
        }
        return points;
    }

    public static Set<Vector3d> calculateCircle(double radius, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        double stepSize = particleDensity / radius;
        for (double theta = 0; theta < cutoffAngle; theta += stepSize) {
            points.add(new Vector3d(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
        return points;
    }

    public static Set<Vector3d> calculateDisc(double radius, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        for (double subRadius = particleDensity; subRadius < radius; subRadius += particleDensity) {
            points.addAll(calculateCircle(subRadius, particleDensity, cutoffAngle));
        }
        points.addAll(calculateCircle(radius, particleDensity, cutoffAngle));
        return points;
    }

    public static Set<Vector3d> calculateHelix(double radius, double height, double slope, int direction, double particleDensity) {
        Set<Vector3d> points = new LinkedHashSet<>();
        if (radius <= 0 || height <= 0) {
            return points;
        }
        double loops = Math.abs(height / slope);
        double length = slope * slope + radius * radius;
        double stepSize = particleDensity / length;
        for (double t = 0; t < loops; t += stepSize) {
            double x = radius * Math.cos(direction * t);
            double z = radius * Math.sin(direction * t);
            points.add(new Vector3d(x, t * slope, z));
        }
        return points;
    }

    public static Set<Vector3d> calculateLine(Vector3d start, Vector3d end, double particleDensity) {
        Set<Vector3d> points = new LinkedHashSet<>();
        Vector3d direction = new Vector3d(end).sub(start);
        double length = direction.length();
        double step = length / Math.round(length / particleDensity);
        direction.normalize().mul(step);

        for (double i = 0; i <= (length / step); i++) {
            points.add(new Vector3d(start).add(new Vector3d(direction).mul(i)));
        }
        return points;
    }

    public static Set<Vector3d> calculateRegularPolygon(double radius, double angle, double particleDensity, boolean wireframe) {
        angle = Math.max(angle, MathUtil.EPSILON);

        Set<Vector3d> points = new LinkedHashSet<>();
        double apothem = radius * Math.cos(angle / 2);
        double radiusStep = radius / Math.round(apothem / particleDensity);
        if (wireframe) {
            radiusStep = 2 * radius;
        } else {
            points.add(new Vector3d(0, 0, 0));
        }
        for (double subRadius = radius; subRadius >= 0; subRadius -= radiusStep) {
            Vector3d vertex = new Vector3d(subRadius, 0, 0);
            for (double i = 0; i < 2 * Math.PI; i += angle) {
                points.addAll(calculateLine(
                        VectorUtil.rotateAroundY(new Vector3d(vertex), i),
                        VectorUtil.rotateAroundY(new Vector3d(vertex), i + angle),
                        particleDensity));
            }
        }
        return points;
    }

    public static Set<Vector3d> calculateRegularPrism(double radius, double angle, double height, double particleDensity, boolean wireframe) {
        Set<Vector3d> points = new LinkedHashSet<>();
        Vector3d vertex = new Vector3d(radius, 0, 0);
        for (double i = 0; i < 2 * Math.PI; i += angle) {
            Vector3d currentVertex = VectorUtil.rotateAroundY(new Vector3d(vertex), i);
            for (Vector3d vector : calculateLine(currentVertex, VectorUtil.rotateAroundY(new Vector3d(vertex), i + angle), particleDensity)) {
                points.add(vector);
                if (wireframe) {
                    points.add(new Vector3d(vector.x, height, vector.z));
                } else {
                    points.addAll(calculateLine(vector, new Vector3d(vector.x, height, vector.z), particleDensity));
                }
            }
            if (wireframe)
                points.addAll(calculateLine(currentVertex, new Vector3d(currentVertex.x, height, currentVertex.z), particleDensity));
        }
        return points;
    }

    public static Set<Vector3d> connectPoints(List<Vector3d> points, double particleDensity) {
        Set<Vector3d> connectedPoints = new LinkedHashSet<>();
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

    public static List<Vector3d> calculateEllipse(double r1, double r2, double particleDensity, double cutoffAngle) {
        List<Vector3d> points = new ArrayList<>();
        double circumference = ellipseCircumference(r1, r2);

        int steps = (int) Math.round(circumference / particleDensity);
        double theta = 0;
        double angleStep = 0;
        for (int i = 0; i < steps; i++) {
            if (theta > cutoffAngle) {
                break;
            }
            points.add(new Vector3d(r1 * Math.cos(theta), 0, r2 * Math.sin(theta)));
            double dx = r1 * Math.sin(theta + 0.5 * angleStep);
            double dy = r2 * Math.cos(theta + 0.5 * angleStep);
            angleStep = particleDensity / Math.sqrt(dx * dx + dy * dy);
            theta += angleStep;
        }
        return points;
    }

    public static Set<Vector3d> calculateEllipticalDisc(double r1, double r2, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        int steps = (int) Math.round(Math.max(r1, r2) / particleDensity);
        double r;
        for (double i = 1; i <= steps; i += 1) {
            r = i / steps;
            points.addAll(calculateEllipse(r1 * r, r2 * r, particleDensity, cutoffAngle));
        }
        return points;
    }

    public static Set<Vector3d> calculateCylinder(double r1, double height, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = calculateDisc(r1, particleDensity, cutoffAngle);
        points.addAll(points.stream().map(v -> new Vector3d(v.x, height, v.z)).collect(Collectors.toSet()));
        Set<Vector3d> wall = calculateCircle(r1, particleDensity, cutoffAngle);
        points.addAll(fillVertically(wall, height, particleDensity));
        return points;
    }

    public static Set<Vector3d> calculateCylinder(double r1, double r2, double height, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = calculateEllipticalDisc(r1, r2, particleDensity, cutoffAngle);
        points.addAll(points.stream().map(v -> new Vector3d(v.x, height, v.z)).collect(Collectors.toSet()));
        Set<Vector3d> wall = new LinkedHashSet<>(calculateEllipse(r1, r2, particleDensity, cutoffAngle));
        points.addAll(fillVertically(wall, height, particleDensity));
        return points;
    }

    public static Set<Vector3d> fillVertically(Set<Vector3d> vectors, double height, double particleDensity) {
        Set<Vector3d> points = new LinkedHashSet<>(vectors);
        double heightStep = height / Math.round(height / particleDensity);
        for (double i = 0; i < height; i += heightStep) {
            for (Vector3d vector : vectors) {
                points.add(new Vector3d(vector.x, i, vector.z));
            }
        }
        return points;
    }

    public static Set<Vector3d> calculateHeart(double length, double width, double eccentricity, double particleDensity) {
        Set<Vector3d> points = new LinkedHashSet<>();
        double angleStep = 4 / 3.0 * particleDensity / (width + length);
        for (double theta = 0; theta < Math.PI * 2; theta += angleStep) {
            double x = width * Math.pow(Math.sin(theta), 3);
            double y = length * (Math.cos(theta) - 1 / eccentricity * Math.cos(2 * theta) - 1.0 / 6 * Math.cos(3 * theta) - 1.0 / 16 * Math.cos(4 * theta));
            points.add(new Vector3d(x, 0, y));
        }
        return points;
    }

    public static Set<Vector3d> calculateStar(double innerRadius, double outerRadius, double angle, double particleDensity) {
        Set<Vector3d> points = new LinkedHashSet<>();
        Vector3d outerVertex = new Vector3d(outerRadius, 0, 0);
        Vector3d innerVertex = new Vector3d(innerRadius, 0, 0);
        for (double theta = 0; theta < 2 * Math.PI; theta += angle) {
            Vector3d currentVertex = VectorUtil.rotateAroundY(new Vector3d(outerVertex), theta);
            points.addAll(calculateLine(currentVertex, VectorUtil.rotateAroundY(new Vector3d(innerVertex), theta + angle / 2), particleDensity));
            points.addAll(calculateLine(currentVertex, VectorUtil.rotateAroundY(new Vector3d(innerVertex), theta - angle / 2), particleDensity));
        }
        return points;
    }
}
