package liverefactoring.utils.firebase.domain;

import liverefactoring.utils.firebase.model.FireNode;
import liverefactoring.utils.firebase.model.ObserveContract;

import java.io.IOException;
import java.util.Map;

public interface DataManager extends ObserveContract.FireObserver {
    FireNode getRoot();

    FireNode getNode(String node);

    FireNode updateNode(String pathToNode, String value);

    void addNode(String pathToParent, Map<String, Object> value);

    void deleteNode(String pathToNode);

    void moveNode(String from, String to);

    void configureFirebase(String pathToConfig) throws IOException;

    ObserveContract.FireObservable getObservable();
}
