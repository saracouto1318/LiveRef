package liverefactoring.utils.importantValues;

import liverefactoring.core.Fragment;
import liverefactoring.core.MyAttribute;
import liverefactoring.core.MyMethod;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import liverefactoring.utils.RefactorUtils;
import liverefactoring.utils.UtilitiesOverall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DistanceCalculator {

    public RefactorUtils refactorUtils = new RefactorUtils();
    public UtilitiesOverall utilitiesOverall = new UtilitiesOverall();

    public DistanceCalculator() {

    }

    public static double getDistance(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty())
            return 1.0;
        return 1.0 - (double) intersection(set1, set2).size() / (double) union(set1, set2).size();
    }

    private static Set<String> union(Set<String> set1, Set<String> set2) {
        Set<String> set = new HashSet<>();
        set.addAll(set1);
        set.addAll(set2);
        return set;
    }

    public static Set<String> intersection(Set<String> set1, Set<String> set2) {
        Set<String> set = new HashSet<>(set1);
        set.retainAll(set2);
        return set;
    }

    public static double getDistanceEntities(Set<Object> set1, Set<Object> set2) {
        if (set1.isEmpty() && set2.isEmpty())
            return 1.0;
        return 1.0 - (double) intersectionEntities(set1, set2).size() / (double) unionEntities(set1, set2).size();
    }

    private static Set<Object> unionEntities(Set<Object> set1, Set<Object> set2) {
        Set<Object> set = new HashSet<>();
        set.addAll(set1);
        set.addAll(set2);
        return set;
    }

    public static Set<Object> intersectionEntities(Set<Object> set1, Set<Object> set2) {
        Set<Object> set = new HashSet<>();
        for (Object o1 : set1) {
            boolean found = false;
            for (Object o2 : set2) {
                if(o1 instanceof MyMethod && o2 instanceof MyMethod){
                    if(((MyMethod) o1).method.getName().equals(((MyMethod) o2).method.getName()) &&
                            ((MyMethod) o1).method.getContainingClass().getName().equals(((MyMethod) o2).method.getContainingClass().getName())) {
                        found = true;
                        break;
                    }
                }
                else if(o1 instanceof MyAttribute && o2 instanceof MyAttribute){
                    if(((MyAttribute) o1).attribute.getName().equals(((MyAttribute) o2).attribute.getName()) &&
                            ((MyAttribute) o1).attribute.getContainingClass().getName().equals(((MyAttribute) o2).attribute.getContainingClass().getName())) {
                        found = true;
                        break;
                    }
                }
            }
            if(found)
                set.add(o1);
        }
        //set.retainAll(set2);
        return set;
    }

    public double[][] getJaccardDistanceMatix(PsiClass sourceClass) {
        ArrayList<PsiElement> entities = new ArrayList<>();
        entities.addAll(Arrays.asList(sourceClass.getFields()));
        entities.addAll(refactorUtils.getMethods(sourceClass));
        double[][] jaccardDistanceMatrix = new double[entities.size()][entities.size()];
        for (int i = 0; i < jaccardDistanceMatrix.length; i++) {
            for (int j = 0; j < jaccardDistanceMatrix.length; j++) {
                if (i != j) {
                    jaccardDistanceMatrix[i][j] = DistanceCalculator.getDistance(utilitiesOverall.getEntities(entities.get(i)), utilitiesOverall.getEntities(entities.get(j)));
                } else {
                    jaccardDistanceMatrix[i][j] = 0.0;
                }
            }
        }
        return jaccardDistanceMatrix;
    }

    public double[][] getJaccardDistanceMatrixByMethod(Object[] fragments) {
        double[][] jaccardDistanceMatrix = new double[fragments.length][fragments.length];
        for (int i = 0; i < jaccardDistanceMatrix.length; i++) {
            for (int j = 0; j < jaccardDistanceMatrix.length; j++) {
                if (i != j) {
                    jaccardDistanceMatrix[i][j] = DistanceCalculator.getDistance(utilitiesOverall.getEntitiesStatement(((Fragment)fragments[i]).node),
                            utilitiesOverall.getEntitiesStatement(((Fragment)fragments[j]).node));
                } else {
                    jaccardDistanceMatrix[i][j] = 0.0;
                }
            }
        }
        return jaccardDistanceMatrix;
    }
}
