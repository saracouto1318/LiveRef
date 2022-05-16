package com.utils;

public class ThresholdsCandidates {
    public static int minNumExtractedMethods = 2;
    public static int maxOrigMethodPercentage = 80;
    public static int minNumStatements = 3;
    public static int minLengthExtraction = 12;
    public static int minValueParameters = 5;
    public static String username = "username";
    public static double lowerValue = 0.10;
    public static double upperValue = 0.40;
    public static boolean colorBlind = false;

    public ThresholdsCandidates(int minNumExtractedMethods, int maxOrigMethodPercentage, int minNumStatements,
                                int minLengthExtraction, int minValueParameters, String username, boolean colorBlind) {
        ThresholdsCandidates.minNumExtractedMethods = minNumExtractedMethods;
        ThresholdsCandidates.maxOrigMethodPercentage = maxOrigMethodPercentage;
        ThresholdsCandidates.minNumStatements = minNumStatements;
        ThresholdsCandidates.minLengthExtraction = minLengthExtraction;
        ThresholdsCandidates.minValueParameters = minValueParameters;
        ThresholdsCandidates.username = username;
        ThresholdsCandidates.colorBlind = colorBlind;
    }
}
