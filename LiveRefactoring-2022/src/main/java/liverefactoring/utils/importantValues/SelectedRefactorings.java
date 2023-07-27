package liverefactoring.utils.importantValues;

import liverefactoring.core.Refactorings;

import java.util.ArrayList;

public class SelectedRefactorings {
    public static Refactorings selectedRefactoring = null;
    public static ArrayList<Refactorings> selectedRefactorings = new ArrayList<>();

    public SelectedRefactorings() {
        selectedRefactorings = new ArrayList<>();
    }
}
