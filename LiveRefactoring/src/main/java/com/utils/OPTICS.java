package com.utils;

import java.util.ArrayList;
import java.util.List;

public class OPTICS {
    public List<DBPoint> points;
    public List<DBPoint> order;

    public double max_distance;
    public int min_points;

    public boolean[] processed;
    private double[] reachability;
    private double[] core_distance;

    private double[][] distances;

    public OPTICS(double max_distance, int min_points) {
        this.points = new ArrayList();
        this.order = new ArrayList();
        this.max_distance = max_distance;
        this.min_points = min_points;
    }

    public void cluster() {
        calculateDistances();

        int n = 0;
        for (DBPoint point : points) {
            if(!processed[n]) {
                processed[n] = true;
                List<Integer> neighbors = getNeighbors(point,n);
                order.add(point);
                if(core_distance[n] != -1) {
                    List<Pair<Integer,Double>> seeds = new ArrayList<>();
                    update(point, seeds, neighbors, n);
                    for(int i=0; i < seeds.size(); i++){
                        Pair<Integer,Double> seed = seeds.get(i);
                        int node = seed.getFirst();
                        processed[node] = true;
                        DBPoint seedPoint = points.get(node);
                        List<Integer> seedNeighbors = getNeighbors(seedPoint,node);
                        order.add(seedPoint);

                        if(core_distance[node] != -1) {
                            update(seedPoint,seeds,seedNeighbors,node);
                        }
                    }
                }
            }
        }
    }

    private void update(DBPoint point, List<Pair<Integer, Double>> seeds,
                        List neighbors, int node) {

        for (Object neighbor : neighbors) {
            int index = (int) neighbor;
            if (!processed[index]) {
                double max = Math.max(core_distance[node], distances[node][index]);

                if (reachability[index] == -1) {
                    reachability[index] = max;
                    insertSeed(seeds, new Pair<>(index, max), false);
                } else if (max < reachability[index]) {
                    reachability[index] = max;
                    insertSeed(seeds, new Pair<>(index, max), true);
                }
            }
        }
    }

    public void insertSeed(List<Pair<Integer, Double>> seeds, Pair<Integer,Double> seed, boolean remove) {
        int index = seeds.size() +1;

        int node = seed.getFirst();
        double distance = seed.getSecond();

        boolean done = false;

        for(int i = 0; i < seeds.size(); i++) {
            Pair<Integer,Double> aux = seeds.get(i);
            double aux_distance = aux.getSecond();
            if(distance < aux_distance) {
                seeds.add(i,seed);
                done = true;
                if(remove) {
                    index = i + 1;
                }
                break;
            }
        }
        for(int i = index; i < seeds.size() ; i++) {
            Pair<Integer,Double> aux = seeds.get(i);
            int aux_index = aux.getFirst();
            if(node == aux_index) {
                seeds.remove(i);
                break;
            }
        }

        if(!done) {
            seeds.add(seeds.size(),seed);
        }
    }

    private List<Integer> getNeighbors(DBPoint d, int index) {
        List<Integer> neighbors = new ArrayList();
        int i = 0;
        for (DBPoint point : points) {
            double distance = distances[index][i];

            if (distance <= max_distance) {
                neighbors.add(i);
                if (i == min_points - 1) {
                    core_distance[index] = distance;
                }
            }
            i++;
        }

        return neighbors;
    }

    public void setPoints(List points) {
        this.points = points;
        this.processed = new boolean[points.size()];
        this.reachability = new double[points.size()];
        this.core_distance = new double[points.size()];

        for(int i = 0; i < reachability.length; i++) {
            reachability[i] = -1;
            core_distance[i] = -1;
        }
    }

    private void calculateDistances() {
        //System.out.println("Calculation distances between " + points.size() + " points...");
        this.distances = new double[points.size()][points.size()];

        for(int i = 0; i < points.size(); i++) {
            DBPoint p1 = points.get(i);

            for(int j = 0; j < points.size(); j++){
                DBPoint p2 = points.get(j);
                if(i != j){
                    this.distances[i][j] = DistanceCalculator.getDistance(p1.entities, p2.entities);
                }
                else
                    this.distances[i][j] = 0.0;
            }
        }
    }
}
