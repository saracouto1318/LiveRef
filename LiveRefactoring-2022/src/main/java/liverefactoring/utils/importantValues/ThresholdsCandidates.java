package liverefactoring.utils.importantValues;

public class ThresholdsCandidates {
    //Extract Class
    public static double targetSize = 2;
    public static double minNumExtractedMethods = 2;
    public static double numMethodsEC = 4;
    public static double extractClassLackCohesion = 0.33;
    public static double foreignData = 2;
    public static double minOrigMethodPercentageEC = 70;
    public static double lowerValue = 0.10;
    public static double upperValue = 0.25;

    //Extract Method
    public static double minOrigMethodPercentageEM = 70;
    public static double minNumStatements = 3;
    public static double extractMethodLines = 60;
    public static double extractMethodLinesCode = 30;
    public static double extractMethodComplexity = 15;
    public static double extractMethodEffort = 300;

    //Extract Variable
    public static double minLengthExtraction = 12;

    //Introduce Parameter Object
    public static double minValueParameters = 5;

    //Inheritance to Delegation
    public static double inheriteMethods = 33;
    public static double overrideMethods = 33;
    public static double protectedFields = 1;
    public static double protectedMethods = 1;

    //All
    public static int maxNumberRefactorings = Integer.MAX_VALUE;
}
