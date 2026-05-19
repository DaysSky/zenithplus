package com.dayssky.zenithplus.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

public class CircleMath {
    private static final double[] EXPECTED_RADII = new double[41];
    static {
        for (int t = 0; t <= 40; t++) {
            double r = -0.01 * t * (t - 40);
            EXPECTED_RADII[t] = Math.max(0.5, Math.min(2.0, r));
        }
    }

    public static int tickFromRadius(double radius) {
        for (int t = 1; t <= 40; t++) {
            if (Math.abs(radius - EXPECTED_RADII[t]) < 1e-4) {
                return t;
            }
        }
        return -1;
    }

    public static double @Nullable [] circleFrom3Points(double x1, double z1, double x2, double z2, double x3, double z3) {
        double a = x1 * (z2 - z3) - z1 * (x2 - x3) + x2 * z3 - x3 * z2;
        if (Math.abs(a) < 1e-10) return null;

        double b = (x1 * x1 + z1 * z1) * (z3 - z2) + (x2 * x2 + z2 * z2) * (z1 - z3) + (x3 * x3 + z3 * z3) * (z2 - z1);
        double c = (x1 * x1 + z1 * z1) * (x2 - x3) + (x2 * x2 + z2 * z2) * (x3 - x1) + (x3 * x3 + z3 * z3) * (x1 - x2);

        double cx = -b / (2 * a);
        double cz = -c / (2 * a);
        double r = Math.sqrt(Math.pow(x1 - cx, 2) + Math.pow(z1 - cz, 2));

        return new double[]{cx, cz, r};
    }

    public static List<List<double[]>> groupByYLevel(List<double[]> particles) {
        List<List<double[]>> groups = new ArrayList<>();

        for (double[] p : particles) {
            boolean foundGroup = false;
            for (List<double[]> group : groups) {
                if (!group.isEmpty() && Math.abs(group.get(0)[1] - p[1]) < 1e-6) {
                    group.add(p);
                    foundGroup = true;
                    break;
                }
            }
            if (!foundGroup) {
                List<double[]> newGroup = new ArrayList<>();
                newGroup.add(p);
                groups.add(newGroup);
            }
        }

        return groups;
    }

    public static List<double[]> fitCircles(List<double[]> particles, int maxCircles) {
        List<double[]> validCircles = new ArrayList<>();
        List<double[]> remaining = new ArrayList<>(particles);

        Random random = new Random();

        for (int circleIdx = 0; circleIdx < maxCircles && remaining.size() >= 3; circleIdx++) {
            double[] bestCircle = null;
            List<double[]> bestInliers = null;
            int bestInlierCount = 0;
            int bestTick = -1;

            for (int iter = 0; iter < 150 && remaining.size() >= 3; iter++) {
                int i1 = random.nextInt(remaining.size());
                int i2, i3;
                do { i2 = random.nextInt(remaining.size()); } while (i2 == i1);
                do { i3 = random.nextInt(remaining.size()); } while (i3 == i1 || i3 == i2);

                double[] p1 = remaining.get(i1);
                double[] p2 = remaining.get(i2);
                double[] p3 = remaining.get(i3);

                double[] circle = circleFrom3Points(p1[0], p1[2], p2[0], p2[2], p3[0], p3[2]);
                if (circle == null) continue;

                double cx = circle[0], cz = circle[1], r = circle[2];

                if (r < 0.5 || r > 2.0) continue;

                int tick = tickFromRadius(r);
                if (tick <= 0 || tick > 40) continue;

                List<double[]> inliers = new ArrayList<>();
                for (double[] p : remaining) {
                    double distFromCenter = Math.sqrt(Math.pow(p[0] - cx, 2) + Math.pow(p[2] - cz, 2));
                    double distFromCircumference = Math.abs(distFromCenter - r);
                    if (distFromCircumference < 1e-4) {
                        inliers.add(p);
                    }
                }

                if (inliers.size() > bestInlierCount) {
                    bestInlierCount = inliers.size();
                    bestCircle = circle;
                    bestInliers = inliers;
                    bestTick = tick;
                }
            }

            if (bestCircle != null && bestInlierCount >= 5) {
                validCircles.add(new double[]{bestCircle[0], bestCircle[1], bestTick});
                remaining.removeAll(bestInliers);
            } else {
                break;
            }
        }

        return validCircles;
    }

    public static boolean fitsTrajectory(double[] circle, double[] refCircle, double[] velocity) {
        double tickDiff = circle[2] - refCircle[2];
        double expectedX = refCircle[0] + velocity[0] * tickDiff;
        double expectedZ = refCircle[1] + velocity[1] * tickDiff;

        double distX = Math.abs(circle[0] - expectedX);
        double distZ = Math.abs(circle[1] - expectedZ);

        return distX < 1e-3 && distZ < 1e-3;
    }
}
