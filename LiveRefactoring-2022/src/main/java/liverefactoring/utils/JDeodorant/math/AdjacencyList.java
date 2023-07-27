package liverefactoring.utils.JDeodorant.math;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AdjacencyList {

    private Map<Node, LinkedHashSet<Edge>> adjacencies = new HashMap<>();

    public void addEdge(Node source, Node target, int weight) {
        LinkedHashSet<Edge> list;
        if (!adjacencies.containsKey(source)) {
            list = new LinkedHashSet<>();
            adjacencies.put(source, list);
        } else {
            list = adjacencies.get(source);
        }
        list.add(new Edge(source, target, weight));
    }

    public LinkedHashSet<Edge> getAdjacent(Node source) {
        if (adjacencies.containsKey(source))
            return adjacencies.get(source);
        else
            return new LinkedHashSet<>();
    }

    public Set<Node> getSourceNodeSet() {
        return adjacencies.keySet();
    }

}
