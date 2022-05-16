package com.utils;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.refactorings.candidates.utils.Fragment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Features {
    public HashMap<PsiStatement, ArrayList<String>> statementFeatures;
    public Set<String> features;

    public Features(HashMap<PsiStatement, ArrayList<String>> statementFeatures, Set<String> features){
        this.statementFeatures = statementFeatures;
        this.features = features;
    }
}

class Encoding {
    HashMap<PsiStatement, ArrayList<String>> variableFeatures = new HashMap<>();
    HashMap<PsiStatement, ArrayList<String>> structureFeatures = new HashMap<>();
    Map<String, Integer> variableCountMap = new HashMap<>();
    Map<Integer, Integer> levelCountMap = new HashMap<>();

    public Encoding(){

    }

    public Features bagOfFeatures(PsiStatement[] statements, FeatureType featureType){
        HashMap<PsiStatement, ArrayList<String>> statementFeatures;

        switch (featureType) {
            case DataFlow:
                statementFeatures = getVariableFeatures(statements);
                break;

            case ControlFlow:
                statementFeatures = getStructureFeatures(statements);
                break;

            case DataAndControlFlow:
                statementFeatures = getStructureAndVariableFeatures(statements);
                break;

            default:
                statementFeatures = getVariableFeatures(statements);
                break;
        }

        Set<String> features = new HashSet<>();

        for (ArrayList<String> value : statementFeatures.values()) {
            features.addAll(value);
        }

        return new Features(statementFeatures, features);
    }

    public HashMap<PsiStatement, ArrayList<String>> getVariableFeatures(PsiStatement[] statements){
        this._variableFeaturesRecursive(statements);
        return variableFeatures;
    }

    public HashMap<PsiStatement, ArrayList<String>> getStructureFeatures(PsiStatement[] statements){
        this._structureFeaturesRecursive(statements);
        return structureFeatures;
    }

    public HashMap<PsiStatement, ArrayList<String>> getStructureAndVariableFeatures(PsiStatement[] statements){
        HashMap<PsiStatement, ArrayList<String>> structureVariableFeatures = new HashMap<>();
        HashMap<PsiStatement, ArrayList<String>> variableFeatures = this.getVariableFeatures(statements);
        HashMap<PsiStatement, ArrayList<String>> structureFeatures = this.getStructureFeatures(statements);

        structureVariableFeatures.putAll(variableFeatures);
        structureVariableFeatures.putAll(structureFeatures);

        return structureVariableFeatures;
    }

    public void _variableFeaturesRecursive(PsiElement[] statements){
        for (PsiElement statement : statements) {
            ArrayList<String> variableNames = new ArrayList<>();
            for (PsiVariable psiVariable : PsiTreeUtil.findChildrenOfType(statement, PsiVariable.class)) {
                String varName = psiVariable.getName();

                if (!variableCountMap.containsKey(varName))
                    variableCountMap.put(varName, 1);
                if (statement instanceof PsiDeclarationStatement) {
                    variableCountMap.replace(varName, variableCountMap.get(varName) + 1);
                }

                variableNames.add(varName + "_" + variableCountMap.get(varName));
            }

            if(statement instanceof PsiStatement)
                variableFeatures.put((PsiStatement) statement, variableNames);

            if((statement instanceof PsiForStatement || statement instanceof PsiIfStatement ||
                    statement instanceof PsiSwitchStatement || statement instanceof PsiTryStatement) &&
                    PsiTreeUtil.findChildrenOfType(statement, PsiCodeBlock.class).size() > 0){
                this._variableFeaturesRecursive(statement.getChildren());
            }
        }
    }

    public void _structureFeaturesRecursive(PsiElement[] statements){
        for (PsiElement stm : statements) {
            if(stm instanceof PsiTryStatement) {
                PsiStatement statement = (PsiStatement) stm;

                int level;

                if (PsiTreeUtil.findChildrenOfType(statement, PsiCodeBlock.class).size() > 0) {
                    level = 1;

                    if (levelCountMap.containsKey(level))
                        levelCountMap.replace(level, levelCountMap.get(level) + 1);
                    else levelCountMap.replace(level, 1);

                    structureFeatures.put(statement, this.buildFeaturesUntilLevel(level, levelCountMap));
                    this._structureFeaturesRecursive(statement.getChildren());

                } else {
                    level = 0;

                    structureFeatures.put(statement, this.buildFeaturesUntilLevel(level, levelCountMap));
                }
            }
        }
    }

    public ArrayList<String> buildFeaturesUntilLevel(int level, Map<Integer, Integer> levelCountMap){
        ArrayList<String> features = new ArrayList<>();
        for(int i = 1; i <= level; i++){
            features.add("level-" + i + "_" + levelCountMap.get(i));
        }

        return features;
    }
}

public class BinaryRepresentation {

    public static final String STRUCTURE_FEATURE_PREFIX = "level-";
    public HashMap<PsiStatement, ArrayList<String>> featuresByStatement;
    public List<String> features;
    public PsiStatement[] statements;
    public ConcurrentHashMap<PsiStatement, ArrayList<Integer>> representation;
    public Utilities utils = new Utilities();

    public BinaryRepresentation(ArrayList<Fragment> fragments){
        this.statements = new PsiStatement[fragments.size()];
        for (int i = 0; i < fragments.size(); i++) {
            this.statements[i] = fragments.get(i).node;
        }

        Encoding encoding = new Encoding();
        Features bagOfFeatures = encoding.bagOfFeatures(this.statements, FeatureType.DataAndControlFlow);
        this.featuresByStatement = bagOfFeatures.statementFeatures;
        this.features = new ArrayList<>(bagOfFeatures.features);

        this.features.sort((a, b) -> {
            if (a.startsWith(STRUCTURE_FEATURE_PREFIX) && !b.startsWith(STRUCTURE_FEATURE_PREFIX)) {
                return 1;
            }
            if (!a.startsWith(STRUCTURE_FEATURE_PREFIX) && b.startsWith(STRUCTURE_FEATURE_PREFIX)) {
                return -1;
            }
            return localeCompare(a, b);
        });

        this.representation = new ConcurrentHashMap<>();
        for (PsiStatement statement : this.statements) {
            ArrayList<Integer> aux = new ArrayList<>();
            for (String s : this.features) {
                if(this.featuresByStatement.get(statement).contains(s)) {
                    aux.add(1);
                }
                else {
                    aux.add(0);
                }
            }
            this.representation.put(statement, aux);
        }
    }

    public BinaryRepresentation(PsiStatement[] statements, FeatureType featureType){
        this.statements = statements;
        Encoding encoding = new Encoding();
        Features bagOfFeatures = encoding.bagOfFeatures(this.statements, featureType);
        this.featuresByStatement = bagOfFeatures.statementFeatures;
        this.features = new ArrayList<>(bagOfFeatures.features);

        this.features.sort((a, b) -> {
            if (a.startsWith(STRUCTURE_FEATURE_PREFIX) && !b.startsWith(STRUCTURE_FEATURE_PREFIX)) {
                return 1;
            }
            if (!a.startsWith(STRUCTURE_FEATURE_PREFIX) && b.startsWith(STRUCTURE_FEATURE_PREFIX)) {
                return -1;
            }
            return localeCompare(a, b);
        });

        this.representation = new ConcurrentHashMap<>();
        for (PsiStatement statement : this.statements) {
            ArrayList<Integer> aux = new ArrayList<>();
            for (String s : this.features) {
                if(this.featuresByStatement.get(statement).contains(s)) {
                    aux.add(1);
                }
                else {
                    aux.add(0);
                }
            }
            this.representation.put(statement, aux);
        }
    }

    public int localeCompare(String t1, String t2){
        int len1 = t1.length();
        int len2 = t2.length();
        int lim = Math.min(len1, len2);

        int k = 0;
        while(k < lim)
        {
            char c1 = t1.charAt(k);
            char c2 = t2.charAt(k);
            if(c1 != c2)
            {
                if((int) c1 == 32 )
                {
                    return (int)c1 + (int)c2;
                }
                else{
                    return (int)c1 - (int)c2;
                }

            }
            k++;
        }
        return len1 - len2;
    }
}
