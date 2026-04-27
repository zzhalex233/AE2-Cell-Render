package com.zzhalex233.ae2cellrender.client.drive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class LabClusterReducer {

    static final int K_CLUSTERS = 6;
    static final int MAX_ITERATIONS = 12;
    static final float MIN_CLUSTER_RATIO = 0.03F;
    static final float MERGE_DELTA_E = 12.0F;
    private static final float MIN_CENTER_SHIFT = 0.05F;

    private LabClusterReducer() {
    }

    static List<Cluster> reduce(List<CellSpriteWeightedSamples.WeightedSample> samples) {
        List<CellSpriteWeightedSamples.WeightedSample> weightedSamples = new ArrayList<>();
        for (CellSpriteWeightedSamples.WeightedSample sample : samples) {
            if (sample.weight() > 0.0F) {
                weightedSamples.add(sample);
            }
        }
        if (weightedSamples.isEmpty()) {
            return Collections.emptyList();
        }

        int clusterCount = Math.min(K_CLUSTERS, weightedSamples.size());
        List<CellColorMath.LabColor> centers = seedCenters(weightedSamples, clusterCount);
        int[] assignments = new int[weightedSamples.size()];

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            assignSamples(weightedSamples, centers, assignments);
            List<CellColorMath.LabColor> nextCenters = recomputeCenters(weightedSamples, centers, assignments);
            if (centerShift(centers, nextCenters) < MIN_CENTER_SHIFT) {
                centers = nextCenters;
                break;
            }
            centers = nextCenters;
        }

        return filterAndMerge(buildClusters(weightedSamples, centers, assignments));
    }

    static List<Cluster> filterAndMerge(List<Cluster> clusters) {
        if (clusters.isEmpty()) {
            return Collections.emptyList();
        }

        float totalWeight = 0.0F;
        for (Cluster cluster : clusters) {
            totalWeight += cluster.totalWeight();
        }

        List<Cluster> filtered = new ArrayList<>();
        for (Cluster cluster : clusters) {
            if (cluster.totalWeight() / totalWeight >= MIN_CLUSTER_RATIO) {
                filtered.add(cluster);
            }
        }
        if (filtered.isEmpty()) {
            return Collections.emptyList();
        }

        boolean mergedAny;
        do {
            mergedAny = false;
            for (int leftIndex = 0; leftIndex < filtered.size() && !mergedAny; leftIndex++) {
                for (int rightIndex = leftIndex + 1; rightIndex < filtered.size(); rightIndex++) {
                    if (CellColorMath.deltaE(filtered.get(leftIndex).lab(), filtered.get(rightIndex).lab()) < MERGE_DELTA_E) {
                        Cluster merged = filtered.get(leftIndex).merge(filtered.get(rightIndex));
                        filtered.set(leftIndex, merged);
                        filtered.remove(rightIndex);
                        mergedAny = true;
                        break;
                    }
                }
            }
        } while (mergedAny);

        filtered.sort(Comparator.comparing(Cluster::totalWeight).reversed());
        return Collections.unmodifiableList(filtered);
    }

    private static List<CellColorMath.LabColor> seedCenters(List<CellSpriteWeightedSamples.WeightedSample> samples, int clusterCount) {
        List<CellSpriteWeightedSamples.WeightedSample> sorted = new ArrayList<>(samples);
        sorted.sort(Comparator.comparing(CellSpriteWeightedSamples.WeightedSample::weight).reversed());

        List<CellColorMath.LabColor> centers = new ArrayList<>(clusterCount);
        centers.add(sorted.get(0).lab());
        while (centers.size() < clusterCount) {
            CellSpriteWeightedSamples.WeightedSample next = null;
            float bestScore = -1.0F;
            for (CellSpriteWeightedSamples.WeightedSample sample : sorted) {
                float nearestDistance = Float.MAX_VALUE;
                for (CellColorMath.LabColor center : centers) {
                    nearestDistance = Math.min(nearestDistance, CellColorMath.deltaE(sample.lab(), center));
                }
                float score = nearestDistance * sample.weight();
                if (score > bestScore) {
                    bestScore = score;
                    next = sample;
                }
            }
            centers.add(next == null ? sorted.get(0).lab() : next.lab());
        }
        return centers;
    }

    private static void assignSamples(List<CellSpriteWeightedSamples.WeightedSample> samples, List<CellColorMath.LabColor> centers, int[] assignments) {
        for (int sampleIndex = 0; sampleIndex < samples.size(); sampleIndex++) {
            CellSpriteWeightedSamples.WeightedSample sample = samples.get(sampleIndex);
            int bestIndex = 0;
            float bestDistance = Float.MAX_VALUE;
            for (int centerIndex = 0; centerIndex < centers.size(); centerIndex++) {
                float distance = CellColorMath.deltaE(sample.lab(), centers.get(centerIndex));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = centerIndex;
                }
            }
            assignments[sampleIndex] = bestIndex;
        }
    }

    private static List<CellColorMath.LabColor> recomputeCenters(List<CellSpriteWeightedSamples.WeightedSample> samples,
                                                                 List<CellColorMath.LabColor> centers,
                                                                 int[] assignments) {
        List<CellColorMath.LabColor> nextCenters = new ArrayList<>(centers.size());
        for (int centerIndex = 0; centerIndex < centers.size(); centerIndex++) {
            float totalWeight = 0.0F;
            float lightness = 0.0F;
            float a = 0.0F;
            float b = 0.0F;
            for (int sampleIndex = 0; sampleIndex < samples.size(); sampleIndex++) {
                if (assignments[sampleIndex] != centerIndex) {
                    continue;
                }
                CellSpriteWeightedSamples.WeightedSample sample = samples.get(sampleIndex);
                float weight = sample.weight();
                totalWeight += weight;
                lightness += sample.lab().lightness() * weight;
                a += sample.lab().a() * weight;
                b += sample.lab().b() * weight;
            }
            if (totalWeight == 0.0F) {
                nextCenters.add(centers.get(centerIndex));
            } else {
                nextCenters.add(CellColorMath.lab(lightness / totalWeight, a / totalWeight, b / totalWeight));
            }
        }
        return nextCenters;
    }

    private static List<Cluster> buildClusters(List<CellSpriteWeightedSamples.WeightedSample> samples,
                                               List<CellColorMath.LabColor> centers,
                                               int[] assignments) {
        List<ClusterAccumulator> accumulators = new ArrayList<>(centers.size());
        for (int centerIndex = 0; centerIndex < centers.size(); centerIndex++) {
            accumulators.add(new ClusterAccumulator());
        }

        for (int sampleIndex = 0; sampleIndex < samples.size(); sampleIndex++) {
            accumulators.get(assignments[sampleIndex]).add(samples.get(sampleIndex));
        }

        List<Cluster> clusters = new ArrayList<>();
        for (ClusterAccumulator accumulator : accumulators) {
            Cluster cluster = accumulator.finish();
            if (cluster != null) {
                clusters.add(cluster);
            }
        }
        return clusters;
    }

    private static float centerShift(List<CellColorMath.LabColor> centers, List<CellColorMath.LabColor> nextCenters) {
        float maxShift = 0.0F;
        for (int index = 0; index < centers.size(); index++) {
            maxShift = Math.max(maxShift, CellColorMath.deltaE(centers.get(index), nextCenters.get(index)));
        }
        return maxShift;
    }

    static final class Cluster {
        private final CellColorMath.LabColor lab;
        private final int argb;
        private final float totalWeight;
        private final int sampleCount;
        private final float outerWeight;
        private final float secondRingWeight;
        private final float innerWeight;
        private final float outlineWeight;
        private final float highlightWeight;
        private final float saturationWeight;
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        Cluster(CellColorMath.LabColor lab, int argb, float totalWeight, int sampleCount) {
            this(lab, argb, totalWeight, sampleCount, 0.0F, 0.0F, totalWeight, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0);
        }

        Cluster(CellColorMath.LabColor lab, int argb, float totalWeight, int sampleCount, float outerWeight,
                float secondRingWeight, float innerWeight, float outlineWeight, float highlightWeight,
                float saturationWeight, int minX, int minY, int maxX, int maxY) {
            this.lab = lab;
            this.argb = argb;
            this.totalWeight = totalWeight;
            this.sampleCount = sampleCount;
            this.outerWeight = outerWeight;
            this.secondRingWeight = secondRingWeight;
            this.innerWeight = innerWeight;
            this.outlineWeight = outlineWeight;
            this.highlightWeight = highlightWeight;
            this.saturationWeight = saturationWeight;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        CellColorMath.LabColor lab() {
            return lab;
        }

        int argb() {
            return argb;
        }

        float totalWeight() {
            return totalWeight;
        }

        int sampleCount() {
            return sampleCount;
        }

        float outerWeight() {
            return outerWeight;
        }

        float secondRingWeight() {
            return secondRingWeight;
        }

        float innerWeight() {
            return innerWeight;
        }

        float outlineWeight() {
            return outlineWeight;
        }

        float highlightWeight() {
            return highlightWeight;
        }

        float averageSaturation() {
            if (totalWeight == 0.0F) {
                return 0.0F;
            }
            return saturationWeight / totalWeight;
        }

        float solidity() {
            int width = Math.max(1, maxX - minX + 1);
            int height = Math.max(1, maxY - minY + 1);
            return sampleCount / (float) (width * height);
        }

        Cluster merge(Cluster other) {
            float mergedWeight = totalWeight + other.totalWeight;
            float leftShare = totalWeight / mergedWeight;
            float rightShare = other.totalWeight / mergedWeight;
            int mergedArgb = CellColorMath.argb(
                    0xFF,
                    Math.round((((argb >>> 16) & 0xFF) * leftShare) + (((other.argb >>> 16) & 0xFF) * rightShare)),
                    Math.round((((argb >>> 8) & 0xFF) * leftShare) + (((other.argb >>> 8) & 0xFF) * rightShare)),
                    Math.round(((argb & 0xFF) * leftShare) + ((other.argb & 0xFF) * rightShare))
            );
            CellColorMath.LabColor mergedLab = CellColorMath.lab(
                    (lab.lightness() * leftShare) + (other.lab.lightness() * rightShare),
                    (lab.a() * leftShare) + (other.lab.a() * rightShare),
                    (lab.b() * leftShare) + (other.lab.b() * rightShare)
            );
            return new Cluster(
                    mergedLab,
                    mergedArgb,
                    mergedWeight,
                    sampleCount + other.sampleCount,
                    outerWeight + other.outerWeight,
                    secondRingWeight + other.secondRingWeight,
                    innerWeight + other.innerWeight,
                    outlineWeight + other.outlineWeight,
                    highlightWeight + other.highlightWeight,
                    saturationWeight + other.saturationWeight,
                    Math.min(minX, other.minX),
                    Math.min(minY, other.minY),
                    Math.max(maxX, other.maxX),
                    Math.max(maxY, other.maxY)
            );
        }
    }

    private static final class ClusterAccumulator {
        private float totalWeight;
        private int sampleCount;
        private float lightness;
        private float a;
        private float b;
        private float red;
        private float green;
        private float blue;
        private float outerWeight;
        private float secondRingWeight;
        private float innerWeight;
        private float outlineWeight;
        private float highlightWeight;
        private float saturationWeight;
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;

        private ClusterAccumulator() {
        }

        private void add(CellSpriteWeightedSamples.WeightedSample sample) {
            float weight = sample.weight();
            totalWeight += weight;
            sampleCount++;
            lightness += sample.lab().lightness() * weight;
            a += sample.lab().a() * weight;
            b += sample.lab().b() * weight;
            red += sample.red() * weight;
            green += sample.green() * weight;
            blue += sample.blue() * weight;
            saturationWeight += sample.saturation() * weight;
            minX = Math.min(minX, sample.x());
            minY = Math.min(minY, sample.y());
            maxX = Math.max(maxX, sample.x());
            maxY = Math.max(maxY, sample.y());
            if (sample.isOutlineCandidate()) {
                outlineWeight += weight;
            }
            if (sample.isHighlightCandidate()) {
                highlightWeight += weight;
            }
            switch (sample.ring()) {
                case OUTER:
                    outerWeight += weight;
                    break;
                case SECOND:
                    secondRingWeight += weight;
                    break;
                case INNER:
                    innerWeight += weight;
                    break;
                default:
                    break;
            }
        }

        private Cluster finish() {
            if (sampleCount == 0 || totalWeight == 0.0F) {
                return null;
            }
            CellColorMath.LabColor lab = CellColorMath.lab(lightness / totalWeight, a / totalWeight, b / totalWeight);
            int argb = CellColorMath.argb(
                    0xFF,
                    Math.round(red / totalWeight),
                    Math.round(green / totalWeight),
                    Math.round(blue / totalWeight)
            );
            return new Cluster(lab, argb, totalWeight, sampleCount, outerWeight, secondRingWeight, innerWeight,
                    outlineWeight, highlightWeight, saturationWeight, minX, minY, maxX, maxY);
        }
    }
}
