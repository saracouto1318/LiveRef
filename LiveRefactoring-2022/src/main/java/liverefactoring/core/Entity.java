package liverefactoring.core;

import com.intellij.psi.PsiClass;

public class Entity {
    public Object type;
    public PsiClass _class;

    public Entity(Object type, PsiClass _class){
        this.type = type;
        this._class = _class;
    }
}
