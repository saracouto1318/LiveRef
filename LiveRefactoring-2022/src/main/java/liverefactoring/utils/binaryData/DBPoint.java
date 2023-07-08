package com.utils.binaryData;

import com.intellij.psi.PsiStatement;

import java.util.*;

public class DBPoint {

    public static final int Undefined = -2;

    /**
     * Reachable Distance for point, Undefined by default
     */
    private static double reachabilityDistance = Undefined;

    /**
     * Used by OPTICS clustering algorithm
     */
    private static boolean processedPoint = false;

    /**
     * Contains the neighborhood of this DBPoint
     */
    private Set<DBPoint> neighborhood = new HashSet<>();

    /**
     * Raw data points
     */
    public ArrayList<Integer> values = new ArrayList<>();

    public PsiStatement statement = null;

    public Set<String> entities = new HashSet<>();

    /**
     * @return the reachabilityDistance
     */
    public double getReachabilityDistance() {
        return reachabilityDistance;
    }


    /**
     * ={ UNDEFINED if |Neps(p)|<MinPts
     * ={ max(core-dist eps, MinPts(p), dist(p,o) else
     * @param newReachDistance the reachabilityDistance to set
     */
    public void setReachabilityDistance(double newReachDistance) {
        DBPoint.reachabilityDistance = newReachDistance;
    }

    /**
     * @return the processedPoint
     */
    public boolean isProcessedPoint() {
        return processedPoint;
    }

    /**
     * @param processedPoint the processedPoint to set
     */
    public void setProcessedPoint(boolean processedPoint) {
        DBPoint.processedPoint = processedPoint;
    }

    /**
     * Returns all points, excluding oneself, that are in the epsilon neighborhood of this point.
     *
     * @param dataset Points to consider
     * @return the neighborhood
     *
     * @see #distanceTo(DBPoint) Note the distance metric has a direct impact on the choice of the neighborhood size that should be submitted
     */
    public Set<DBPoint> getNeighbors(Collection<DBPoint> dataset, double eps) {

        if(neighborhood.size() > 0) {
            return neighborhood;
        }

        //Brute-force search. Check every point to see if they are close.
        //The original implementation and others optimizations so that only log(n) checks need to be made, not n like below.
        for(DBPoint point : dataset){
            if(this != point && eps >= this.distanceTo(point)){
                neighborhood.add(point);
            }
        }
        return neighborhood;
    }

    /**Returns the distance between this point and another  */
    public double distanceTo(DBPoint point) {

        //if these points are of difference dimensions, the longer is much bigger*/
        int sizeDiff = point.values.size() - this.values.size();
        if(sizeDiff!=0){
            if(sizeDiff>0){
                return Double.MAX_VALUE;
            }else{
                return (-1) * Double.MAX_VALUE;
            }
        }
        return sizeDiff;
    }


    /**
     * describes the distance to the minimum points^th closest point
     * = { UNDEFINED if |Neps(p)|<MinPts
     *   { MinPts-th smallest distance to Neps(p) else
     * @param list list to use
     * @param eps
     * @param minPoints minimum number of points
     * @return double The distance from the minPoints-th point.  Undefined if min points not reached yet.
     */
    public double getCoreDistance(List<DBPoint> list, double eps, int minPoints) {
        if (list.size() < minPoints) {
            return Undefined;
        }
        DBPoint pointB = list.get(minPoints);
        double distance = this.distanceTo(pointB);
        if (eps < distance) {
            return Undefined;
        }
        return this.distanceTo(pointB);
    }
}