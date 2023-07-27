package liverefactoring.utils.firebase.model;

import liverefactoring.utils.firebase.model.protocol.UpdateType;

import java.util.ArrayList;

public interface ObserveContract {
    public interface FireObserver {
        void update(UpdateType type, FireNode data);
    }

    public abstract class FireObservable {
        private ArrayList<FireObserver> observers;

        public FireObservable() {
            this.observers = new ArrayList<>();
        }

        public void addObserver(FireObserver observer) {
            if (this.observers.contains(observer))
                return;
            this.observers.add(observer);
        }

        public void removeObserver(FireObserver observer) {
            if (!this.observers.contains(observer))
                return;
            this.observers.remove(observer);
        }

        public void updateAll(UpdateType type, FireNode data) {
            for (FireObserver observer : observers) {
                observer.update(type, data);
            }
        }
    }

    public class FirePayload {
        private final int actionType;
        private final FireNode data;

        public FirePayload(int actionType, FireNode data) {
            this.actionType = actionType;
            this.data = data;
        }

        public int getActionType() {
            return actionType;
        }

        public FireNode getData() {
            return data;
        }
    }
}