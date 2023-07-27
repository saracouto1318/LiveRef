package liverefactoring.utils.clustering;

import liverefactoring.utils.binaryData.DBPoint;

import java.util.List;

public class SilhouetteCoefficient {

    public SilhouetteCoefficient(){}

    public SilhouetteCoefficient(List<List<DBPoint>> clusters)
    {
    }

    /**
     * This method will calculate the silhouette coefficient based off of the distances
     * in weightsTable and how the points are clustered.
     * @return a double that represents the silhouette coefficient
     */
    public double calculateCoefficient(List<DBPoint> cluster, int otherClusters)
    {
        double result = 0.0;
        if(cluster.size() <= 1 || otherClusters == 0)
            return result;

        double aux = 0.0;
        for(int i = 1; i <= cluster.size(); i++){
            result +=  aux + this.silhouetteIndex(i, cluster);
        }

        return result/cluster.size();
    }

    public double silhouetteIndex(int index, List<DBPoint> cluster){
        //ArrayList<Integer> dataPoint = cluster.values.get(index);
        return 0.0;
    }

   /*
   silhouetteIndex: function (
            dataPointIndex: number,
            cluster: number[][],
            otherClusters: number[][][],
            distanceFunction: DistanceFunction
    ) {
    const dataPoint = cluster[dataPointIndex];
    const avgIntra = this.pointAverageDistance(
                dataPoint,
                cluster,
                distanceFunction,
                cluster.length - 1
        );

    const minInter = Math.min(
      ...otherClusters.map((cluster) =>
                this.pointAverageDistance(dataPoint, cluster, distanceFunction)
      )
    );

        return (minInter - avgIntra) / Math.max(minInter, avgIntra);
    }




   pointAverageDistance: function (
            point: number[],
            cluster: number[][],
            distanceFunction: DistanceFunction,
            N: number = cluster.length
    ) {
        return (
                cluster.reduce(
                        (acc, p) => acc + _distance.apply(point, p, distanceFunction),
        0
      ) / N
    );
    },
*/
}
