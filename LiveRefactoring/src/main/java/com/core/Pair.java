package com.core;

import java.util.Objects;

public class Pair<P, S> {

    private P first;
    private S second;

    public Pair(P first, S second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(P first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public P getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public String toString() {
        return "<"+first+","+second+">";
    }

    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair p = (Pair) o;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
    }

    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }
}
