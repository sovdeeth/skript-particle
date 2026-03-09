package com.sovdee.skriptparticles.util;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MathUtil {
    public static final double EPSILON = 0.0001;

    public static double clamp(double value, double min, double max) {
        return Math.clamp(value, min, max);
    }

    public static List<Vector> calculateLine(Vector start, Vector end, double particleDensity) {
        List<Vector> points = new ArrayList<>();
        Vector direction = end.clone().subtract(start);
        double length = direction.length();
        double step = length / Math.round(length / particleDensity);
        direction.normalize().multiply(step);

        for (double i = 0; i <= (length / step); i++) {
            points.add(start.clone().add(direction.clone().multiply(i)));
        }
        return points;
    }

    public static <T> List<List<T>> batch(Collection<T> toDraw, double millisecondsPerPoint) {
        List<List<T>> batches = new ArrayList<>();
        double totalDuration = 0;
        Iterator<T> pointsIterator = toDraw.iterator();
        while (pointsIterator.hasNext()) {
            List<T> batch = new ArrayList<>();
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
