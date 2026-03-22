package com.zzhalex233.ae2cellrender.client.drive;

import java.util.List;

final class CellSpriteMainColorExtractor {
    private static final float FAMILY_SCORE_TIE_MARGIN = 1.0F;

    private CellSpriteMainColorExtractor() {
    }

    static int mainColor(int[] pixels, int width, int height, int fallback) {
        return mainColorCandidate(pixels, width, height, fallback).argb();
    }

    static CellSpriteColorCandidate mainColorCandidate(int[] pixels, int width, int height, int fallback) {
        List<CellSpriteWeightedSamples.WeightedSample> samples = CellSpriteWeightedSamples.build(pixels, width, height);
        if (samples.isEmpty()) {
            return fallbackCandidate(fallback);
        }
        CellColorMath.LabColor globalLab = weightedAverageLab(samples);

        List<LabClusterReducer.Cluster> clusters = LabClusterReducer.reduce(samples);
        if (clusters.isEmpty()) {
            return weightedAverageCandidate(samples, fallback);
        }

        LabClusterReducer.Cluster familyAnchor = clusters.get(0);
        float bestFamilyScore = familyScore(familyAnchor, clusters, globalLab);
        float bestFamilyRepresentativeScore = familyRepresentativeScore(familyAnchor, clusters);
        for (int i = 1; i < clusters.size(); i++) {
            LabClusterReducer.Cluster candidate = clusters.get(i);
            float candidateFamilyScore = familyScore(candidate, clusters, globalLab);
            float candidateRepresentativeScore = familyRepresentativeScore(candidate, clusters);
            if (candidateFamilyScore > bestFamilyScore + FAMILY_SCORE_TIE_MARGIN
                    || (Math.abs(candidateFamilyScore - bestFamilyScore) <= FAMILY_SCORE_TIE_MARGIN
                    && candidateRepresentativeScore > bestFamilyRepresentativeScore)) {
                familyAnchor = candidate;
                bestFamilyScore = candidateFamilyScore;
                bestFamilyRepresentativeScore = candidateRepresentativeScore;
            }
        }

        LabClusterReducer.Cluster representative = familyAnchor;
        float bestRepresentativeScore = representativeScore(representative);
        for (LabClusterReducer.Cluster candidate : clusters) {
            if (!sameFamily(familyAnchor, candidate)) {
                continue;
            }
            float candidateScore = representativeScore(candidate);
            if (candidateScore > bestRepresentativeScore) {
                representative = candidate;
                bestRepresentativeScore = candidateScore;
            }
        }
        return new CellSpriteColorCandidate(
                representative.argb(),
                samples.size(),
                representative.totalWeight(),
                familyWeight(familyAnchor, clusters),
                innerWeightRatio(representative),
                clusters.size(),
                representative.averageSaturation(),
                representative.lab().lightness()
        );
    }

    private static float familyScore(LabClusterReducer.Cluster anchor, List<LabClusterReducer.Cluster> clusters, CellColorMath.LabColor globalLab) {
        float score = 0.0F;
        for (LabClusterReducer.Cluster cluster : clusters) {
            if (sameFamily(anchor, cluster)) {
                score += familySupport(cluster, globalLab);
            }
        }
        return score;
    }

    private static float familyWeight(LabClusterReducer.Cluster anchor, List<LabClusterReducer.Cluster> clusters) {
        float totalWeight = 0.0F;
        for (LabClusterReducer.Cluster cluster : clusters) {
            if (sameFamily(anchor, cluster)) {
                totalWeight += cluster.totalWeight();
            }
        }
        return totalWeight;
    }

    private static float representativeScore(LabClusterReducer.Cluster cluster) {
        float lightness = cluster.lab().lightness() / 100.0F;
        float structuralCredibility = structuralCredibility(cluster);
        return (cluster.totalWeight() * 0.08F)
                + (cluster.innerWeight() * 0.20F)
                + (cluster.secondRingWeight() * 0.08F)
                + (lightness * 18.0F)
                + (cluster.averageSaturation() * 4.0F)
                + (structuralCredibility * 6.0F)
                - (cluster.outerWeight() * 0.05F)
                - (cluster.outlineWeight() * 0.40F)
                - (cluster.highlightWeight() * 0.20F);
    }

    private static float familyRepresentativeScore(LabClusterReducer.Cluster anchor, List<LabClusterReducer.Cluster> clusters) {
        float bestScore = Float.NEGATIVE_INFINITY;
        for (LabClusterReducer.Cluster cluster : clusters) {
            if (!sameFamily(anchor, cluster)) {
                continue;
            }
            bestScore = Math.max(bestScore, representativeScore(cluster));
        }
        return bestScore;
    }

    private static boolean sameFamily(LabClusterReducer.Cluster left, LabClusterReducer.Cluster right) {
        CellColorMath.HsvColor leftHsv = CellColorMath.hsv(left.argb());
        CellColorMath.HsvColor rightHsv = CellColorMath.hsv(right.argb());
        return CellColorMath.hueDistance(leftHsv.hue(), rightHsv.hue()) <= 18.0F
                && Math.abs(leftHsv.saturation() - rightHsv.saturation()) <= 0.22F;
    }

    private static int weightedAverage(List<CellSpriteWeightedSamples.WeightedSample> samples, int fallback) {
        float totalWeight = 0.0F;
        float red = 0.0F;
        float green = 0.0F;
        float blue = 0.0F;
        for (CellSpriteWeightedSamples.WeightedSample sample : samples) {
            float weight = sample.weight();
            totalWeight += weight;
            red += sample.red() * weight;
            green += sample.green() * weight;
            blue += sample.blue() * weight;
        }
        if (totalWeight == 0.0F) {
            return fallback;
        }
        return CellColorMath.argb(0xFF, Math.round(red / totalWeight), Math.round(green / totalWeight), Math.round(blue / totalWeight));
    }

    private static CellColorMath.LabColor weightedAverageLab(List<CellSpriteWeightedSamples.WeightedSample> samples) {
        float totalWeight = 0.0F;
        float lightness = 0.0F;
        float a = 0.0F;
        float b = 0.0F;
        for (CellSpriteWeightedSamples.WeightedSample sample : samples) {
            float weight = sample.weight();
            totalWeight += weight;
            lightness += sample.lab().lightness() * weight;
            a += sample.lab().a() * weight;
            b += sample.lab().b() * weight;
        }
        if (totalWeight == 0.0F) {
            return CellColorMath.lab(0.0F, 0.0F, 0.0F);
        }
        return CellColorMath.lab(lightness / totalWeight, a / totalWeight, b / totalWeight);
    }

    private static float structuralCredibility(LabClusterReducer.Cluster cluster) {
        float solidity = cluster.solidity();
        float innerRatio = cluster.totalWeight() <= 0.0F ? 0.0F : cluster.innerWeight() / cluster.totalWeight();
        return (solidity * 0.75F) + (innerRatio * 0.25F);
    }

    private static float familySupport(LabClusterReducer.Cluster cluster, CellColorMath.LabColor globalLab) {
        // Broad shell regions should outrank small bright inserts when both are otherwise plausible body colors.
        float areaWeight = cluster.totalWeight() + (cluster.sampleCount() * 0.20F);
        // Keep the global average as a nudge instead of letting it dominate multi-tone sprites.
        float globalBias = 0.55F + (0.45F * globalCloseness(cluster, globalLab));
        return areaWeight * structuralCredibility(cluster) * globalBias;
    }

    private static float globalCloseness(LabClusterReducer.Cluster cluster, CellColorMath.LabColor globalLab) {
        float delta = CellColorMath.deltaE(cluster.lab(), globalLab);
        return Math.max(0.15F, 1.0F - (delta / 40.0F));
    }

    private static CellSpriteColorCandidate fallbackCandidate(int fallback) {
        CellColorMath.HsvColor hsv = CellColorMath.hsv(fallback);
        return new CellSpriteColorCandidate(fallback, 0, 0.0F, 0.0F, 0.0F, 0, hsv.saturation(), CellColorMath.lab(fallback).lightness());
    }

    private static CellSpriteColorCandidate weightedAverageCandidate(List<CellSpriteWeightedSamples.WeightedSample> samples, int fallback) {
        int argb = weightedAverage(samples, fallback);
        CellColorMath.HsvColor hsv = CellColorMath.hsv(argb);
        return new CellSpriteColorCandidate(argb, samples.size(), 0.0F, 0.0F, 0.0F, 0, hsv.saturation(), CellColorMath.lab(argb).lightness());
    }

    private static float innerWeightRatio(LabClusterReducer.Cluster cluster) {
        if (cluster.totalWeight() <= 0.0F) {
            return 0.0F;
        }
        return cluster.innerWeight() / cluster.totalWeight();
    }
}
