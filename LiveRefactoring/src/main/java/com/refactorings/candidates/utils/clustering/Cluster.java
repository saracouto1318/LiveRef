package com.refactorings.candidates.utils.clustering;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;

public class Cluster {
    ArrayList<PsiElement> entities;

    PsiMethod oldMethod;

    public Cluster(ArrayList<PsiElement> entities) {
        this.entities = entities;
    }

    public Cluster(ArrayList<PsiElement> entities, PsiMethod oldMethod) {
        this.entities = entities;
        this.oldMethod = oldMethod;
    }

    public Cluster(PsiMethod oldMethod) {
        this.entities = new ArrayList<>();
        this.oldMethod = oldMethod;
    }

    public Cluster() {
        this.entities = new ArrayList<>();
    }

    public void addEntity(PsiElement entity) {
        if (!this.entities.contains(entity)) {
            this.entities.add(entity);
        }
    }

    public ArrayList<PsiElement> getEntities() {
        return this.entities;
    }

    public PsiMethod getOldMethod() { return this.oldMethod; }

    public void addEntities(ArrayList<PsiElement> entities) {
        int counter = (int) entities.stream().filter(entity -> this.entities.contains(entity)).count();

        if (counter != entities.size())
            this.entities.addAll(entities);
    }
}
