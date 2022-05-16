package com.refactorings.candidates.utils.clustering;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiStatement;
import com.refactorings.candidates.ExtractMethodCandidate;
import com.utils.RefactorUtils;
import com.utils.ThresholdsCandidates;
import com.utils.Utilities;

import java.util.ArrayList;
import java.util.HashSet;

public class HierarchicalClustering {
    public HashSet<Cluster> clusterList;
    public double[][] distanceMatrix;
    public Utilities utilities = new Utilities();
    public RefactorUtils refactorUtils = new RefactorUtils();

    public HierarchicalClustering(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.clusterList = new HashSet<>();
    }

    public HashSet<Cluster> getClusters(ArrayList<PsiElement> entities) {
        ArrayList<Cluster> clusters = new ArrayList<>();
        for (PsiElement entity : entities) {
            Cluster cluster = new Cluster();
            cluster.addEntity(entity);
            clusters.add(cluster);
        }

        while (clusters.size() > 2) {
            HashSet<Cluster> clustersNotPresentable = new HashSet<>();
            double minVal = 2.0;
            int minRow = 0;
            int minCol = 1;
            for (int i = 1; i < distanceMatrix.length; i++) {
                for (int j = 0; j < i; j++) {
                    if (distanceMatrix[i][j] < minVal) {
                        minVal = distanceMatrix[i][j];
                        minRow = i;
                        minCol = j;
                    }
                }
            }

            if (minVal >= 1.0)
                break;
            if (minRow < minCol) {
                if (clusters.get(minCol).getEntities().size() == 1 && clusters.get(minRow).getEntities().size() > 1) {
                    clustersNotPresentable.add(new Cluster(clusters.get(minRow).getEntities()));
                } else if (clusters.get(minCol).getEntities().size() > 1 && clusters.get(minRow).getEntities().size() == 1) {
                    clustersNotPresentable.add(new Cluster(clusters.get(minCol).getEntities()));
                }
                clusters.get(minRow).addEntities(clusters.get(minCol).getEntities());
                double[] newDistances = new double[distanceMatrix.length - 1];
                for (int i = 0; i < distanceMatrix.length; i++) {
                    if (i != minCol) {
                        if (i != minRow) {
                            if (distanceMatrix[minRow][i] < distanceMatrix[minCol][i]) {
                                if (i > minCol) {
                                    newDistances[i - 1] = distanceMatrix[minRow][i];
                                } else {
                                    newDistances[i] = distanceMatrix[minRow][i];
                                }
                            } else {
                                if (i > minCol) {
                                    newDistances[i - 1] = distanceMatrix[minCol][i];
                                } else {
                                    newDistances[i] = distanceMatrix[minCol][i];
                                }
                            }
                        } else {
                            newDistances[i] = 0.0;
                        }
                    }
                }
                distanceMatrix = Utilities.deleteRows(distanceMatrix, minRow, minCol);
                distanceMatrix = Utilities.deleteColumns(distanceMatrix, minCol);
                distanceMatrix = Utilities.insertRows(distanceMatrix, minRow, newDistances);
                distanceMatrix = Utilities.deleteColumns(distanceMatrix, minRow);
                distanceMatrix = Utilities.insertColumns(distanceMatrix, minRow, newDistances);
                clusters.remove(minCol);
            } else {
                if (clusters.get(minRow).getEntities().size() == 1 && clusters.get(minCol).getEntities().size() > 1) {
                    clustersNotPresentable.add(new Cluster(clusters.get(minCol).getEntities()));
                } else if (clusters.get(minRow).getEntities().size() > 1 && clusters.get(minCol).getEntities().size() == 1) {
                    clustersNotPresentable.add(new Cluster(clusters.get(minRow).getEntities()));
                }
                clusters.get(minCol).addEntities(clusters.get(minRow).getEntities());
                double[] newDistances = new double[distanceMatrix.length - 1];
                for (int i = 0; i < distanceMatrix.length; i++) {
                    if (i != minRow) {
                        if (i != minCol) {
                            if (distanceMatrix[minRow][i] < distanceMatrix[minCol][i]) {
                                if (i > minRow) {
                                    newDistances[i - 1] = distanceMatrix[minRow][i];
                                } else {
                                    newDistances[i] = distanceMatrix[minRow][i];
                                }
                            } else {
                                if (i > minRow) {
                                    newDistances[i - 1] = distanceMatrix[minCol][i];
                                } else {
                                    newDistances[i] = distanceMatrix[minCol][i];
                                }
                            }
                        } else {
                            newDistances[i] = 0.0;
                        }
                    }
                }
                distanceMatrix = Utilities.deleteRows(distanceMatrix, minRow, minCol);
                distanceMatrix = Utilities.deleteColumns(distanceMatrix, minRow);
                distanceMatrix = Utilities.insertRows(distanceMatrix, minCol, newDistances);
                distanceMatrix = Utilities.deleteColumns(distanceMatrix, minCol);
                distanceMatrix = Utilities.insertColumns(distanceMatrix, minCol, newDistances);
                clusters.remove(minRow);
            }
            clusterList.removeAll(clustersNotPresentable);
            for (Cluster cluster : clusters) {
                if (!(clusters.size() == 2 && (clusters.get(0).getEntities().size() == 1 || clusters.get(1).getEntities().size() == 1))) {
                    if (cluster.getEntities().size() > 1) {
                        Cluster c = new Cluster(cluster.getEntities());
                        clusterList.add(c);
                    }
                }
            }
        }
        return clusterList;
    }

    public HashSet<Cluster> getClustersStatements(ArrayList<ExtractMethodCandidate> candidates) {
        ArrayList<Cluster> clusters = new ArrayList<>();

        for (ExtractMethodCandidate candidate : candidates) {
            for (PsiStatement node : candidate.nodes) {
                Cluster cluster = new Cluster(candidate.method);
                cluster.addEntity(node);
                clusters.add(cluster);
            }
        }

        while (clusters.size() > 2) {
            HashSet<Cluster> clustersNotPresentable = new HashSet<>();
            double minVal = 2.0;
            int minRow = 0;
            int minCol = 1;
            for (int i = 1; i < distanceMatrix.length; i++) {
                for (int j = 0; j < i; j++) {
                    if (distanceMatrix[i][j] < minVal) {
                        minVal = distanceMatrix[i][j];
                        minRow = i;
                        minCol = j;
                    }
                }
            }

            if (minVal >= 1.0)
                break;
            if (minRow < minCol) {
                if (clusters.get(minCol).getEntities().size() == 1 && clusters.get(minRow).getEntities().size() > 1) {
                    clustersNotPresentable.add(new Cluster(clusters.get(minRow).getEntities()));
                } else if (clusters.get(minCol).getEntities().size() > 1 && clusters.get(minRow).getEntities().size() == 1) {
                    clustersNotPresentable.add(new Cluster(clusters.get(minCol).getEntities()));
                }
                clusters.get(minRow).addEntities(clusters.get(minCol).getEntities());
                double[] newDistances = new double[distanceMatrix.length - 1];
                for (int i = 0; i < distanceMatrix.length; i++) {
                    if (i != minCol) {
                        if (i != minRow) {
                            if (distanceMatrix[minRow][i] < distanceMatrix[minCol][i]) {
                                if (i > minCol) {
                                    newDistances[i - 1] = distanceMatrix[minRow][i];
                                } else {
                                    newDistances[i] = distanceMatrix[minRow][i];
                                }
                            } else {
                                if (i > minCol) {
                                    newDistances[i - 1] = distanceMatrix[minCol][i];
                                } else {
                                    newDistances[i] = distanceMatrix[minCol][i];
                                }
                            }
                        } else {
                            newDistances[i] = 0.0;
                        }
                    }
                }
                distanceMatrix = Utilities.deleteRows(distanceMatrix, minRow, minCol);
                distanceMatrix = Utilities.deleteColumns(distanceMatrix, minCol);
                distanceMatrix = Utilities.insertRows(distanceMatrix, minRow, newDistances);
                distanceMatrix = Utilities.deleteColumns(distanceMatrix, minRow);
                distanceMatrix = Utilities.insertColumns(distanceMatrix, minRow, newDistances);
                clusters.remove(minCol);
            } else {
                if (clusters.get(minRow).getEntities().size() == 1 && clusters.get(minCol).getEntities().size() > 1) {
                    clustersNotPresentable.add(new Cluster(clusters.get(minCol).getEntities()));
                } else if (clusters.get(minRow).getEntities().size() > 1 && clusters.get(minCol).getEntities().size() == 1) {
                    clustersNotPresentable.add(new Cluster(clusters.get(minRow).getEntities()));
                }
                clusters.get(minCol).addEntities(clusters.get(minRow).getEntities());
                double[] newDistances = new double[distanceMatrix.length - 1];
                for (int i = 0; i < distanceMatrix.length; i++) {
                    if (i != minRow) {
                        if (i != minCol) {
                            if (distanceMatrix[minRow][i] < distanceMatrix[minCol][i]) {
                                if (i > minRow) {
                                    newDistances[i - 1] = distanceMatrix[minRow][i];
                                } else {
                                    newDistances[i] = distanceMatrix[minRow][i];
                                }
                            } else {
                                if (i > minRow) {
                                    newDistances[i - 1] = distanceMatrix[minCol][i];
                                } else {
                                    newDistances[i] = distanceMatrix[minCol][i];
                                }
                            }
                        } else {
                            newDistances[i] = 0.0;
                        }
                    }
                }
                distanceMatrix = Utilities.deleteRows(distanceMatrix, minRow, minCol);
                distanceMatrix = Utilities.deleteColumns(distanceMatrix, minRow);
                distanceMatrix = Utilities.insertRows(distanceMatrix, minCol, newDistances);
                distanceMatrix = Utilities.deleteColumns(distanceMatrix, minCol);
                distanceMatrix = Utilities.insertColumns(distanceMatrix, minCol, newDistances);
                clusters.remove(minRow);
            }
            clusterList.removeAll(clustersNotPresentable);
            for (Cluster cluster : clusters) {
                if(cluster.getEntities().size() >= ThresholdsCandidates.minNumStatements &&
                        (refactorUtils.getAllStatements(candidates.get(0).method).size() - cluster.getEntities().size()) <=
                                (ThresholdsCandidates.maxOrigMethodPercentage * refactorUtils.getAllStatements(candidates.get(0).method).size())){
                    clusterList.add(cluster);
                }
            }
        }

        return clusterList;
    }
}
