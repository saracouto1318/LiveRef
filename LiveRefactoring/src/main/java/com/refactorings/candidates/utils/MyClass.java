package com.refactorings.candidates.utils;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.utils.RefactorUtils;
import com.utils.ThresholdsCandidates;

import java.util.*;

public class MyClass {
    public PsiClass _class;
    public ArrayList<MyMethod> methods;
    public ArrayList<MyAttribute> attributes;
    public ArrayList<Object> classEntities;
    public HashMap<Object, Set<MyMethod>> accessedMethods;
    public HashMap<Object, Set<MyAttribute>> accessedAttributes;
    public HashMap<Object, Set<Object>> entities;
    public HashMap<Object, Set<MyMethod>> accessedMethodsIn;
    public HashMap<Object, Set<MyAttribute>> accessedAttributesIn;
    public HashMap<Object, Set<MyMethod>> accessedMethodsOut;
    public HashMap<Object, Set<MyAttribute>> accessedAttributesOut;
    public HashMap<PsiClass, PsiField[]> allClasses;
    public PsiJavaFile file;

    public HashMap<Object, List<Entity>> accessos;
    public HashMap<MyMethod, HashMap<PsiClass, Double>> accessosMethod;

    public MyClass(PsiClass _class, PsiJavaFile file){
        this._class = _class;
        this.file = file;
        this.methods = new ArrayList<>();
        this.attributes = new ArrayList<>();
        this.accessedMethods = new HashMap<>();
        this.accessedAttributes = new HashMap<>();
        this.entities = new HashMap<>();
        this.classEntities = new ArrayList<>();
        this.accessedMethodsIn = new HashMap<>();
        this.accessedAttributesIn = new HashMap<>();
        this.accessedMethodsOut = new HashMap<>();
        this.accessedAttributesOut = new HashMap<>();
        this.allClasses = new HashMap<>();
        this.accessos = new HashMap<>();
        this.accessosMethod = new HashMap<>();

        this.getAllClasses();
        this.getAllMethods();
        this.getAllAttributes();

        System.out.println("Size methods: " + this.methods.size());
        System.out.println("Size attr: " + this.attributes.size());

        for (MyMethod method : this.methods) {
            System.out.println(method.method.getName());
        }

        this.getAllEntities();
        for (MyMethod myMethod : this.accessosMethod.keySet()) {
            for (PsiClass psiClass : this.accessosMethod.get(myMethod).keySet()) {
                System.out.println("Method: " + myMethod.method.getName());
                System.out.println("Class: " + psiClass.getName());
                System.out.println("Distance: " + this.accessosMethod.get(myMethod).get(psiClass));
            }
        }

        /*this.classEntities.addAll(this.methods);
        this.classEntities.addAll(this.attributes);
        System.out.println("Size entities: " + this.classEntities.size());

        this.getAllClasses();
        System.out.println("All classes: " + this.allClasses.size());
        this.getAllAccessedMethods();
        System.out.println("Accessed methods: " + this.accessedMethods.size());
        this.getAllAccessedAttributes();
        System.out.println("Accessed attr: " + this.accessedAttributes.size());
        this.getAllInOut();
        System.out.println("In methods: " + this.accessedMethodsIn.size() + "In attr: " + this.accessedAttributesIn.size());
        System.out.println("Out methods: " + this.accessedMethodsOut.size() + "Out attr: " + this.accessedAttributesOut.size());

        for (Object o : this.accessedMethods.keySet()) {
            Set<Object> accessedEntities = new HashSet<>();
            accessedEntities.addAll(this.accessedMethods.get(o));
            this.entities.put(o, accessedEntities);
        }

        for (Object o : this.accessedAttributes.keySet()) {
            Set<Object> accessedEntities = new HashSet<>();
            if(!this.entities.containsKey(o)) {
                accessedEntities.addAll(this.accessedAttributes.get(o));
                this.entities.put(o, accessedEntities);
            }
            else{
                accessedEntities.addAll(this.entities.get(o));
                accessedEntities.addAll(this.accessedAttributes.get(o));
                this.entities.replace(o, accessedEntities);
            }
        }
        System.out.println("Size entities all: " + this.entities.size());
         */
    }

    public void getAllEntitiesMethods(){
        for (MyMethod method : this.methods) {
            Set<MyMethod> methodsAccessed = new HashSet<>();
            for (PsiMethodCallExpression psiMethodCallExpression : PsiTreeUtil.findChildrenOfType(method.method, PsiMethodCallExpression.class)) {
                PsiMethod calledMethod = psiMethodCallExpression.resolveMethod();
                if(calledMethod != null){
                    if(this.allClasses.keySet().contains(calledMethod.getContainingClass())) {
                        if (!calledMethod.getName().equals(method.method.getName())) {
                            String firstLine = calledMethod.getText().split("\n")[0];
                            if (firstLine.contains("static")) {
                                MyMethod newMethod = new MyMethod(calledMethod);
                                if (this.isGetter(calledMethod))
                                    newMethod.setGetter();
                                if (this.isSetter(calledMethod))
                                    newMethod.setSetter();
                                methodsAccessed.add(newMethod);
                            }
                        }
                    }
                }
            }

            for (MyMethod myMethod : methodsAccessed) {
                Entity entity = new Entity(myMethod, myMethod.method.getContainingClass());
                if(!this.accessos.containsKey(method))
                    this.accessos.put(method, new ArrayList<>());
                this.accessos.get(method).add(entity);
            }
        }

        for (MyAttribute attribute : this.attributes) {
            Set<MyMethod> methodsAccessed = new HashSet<>();
            for (PsiAssignmentExpression psiAssignmentExpression : PsiTreeUtil.findChildrenOfType(this._class, PsiAssignmentExpression.class)) {
                if(psiAssignmentExpression.getLExpression().getText().equals(attribute.attribute.getName())){
                    if(psiAssignmentExpression.getRExpression() instanceof PsiMethodCallExpression){
                        PsiMethod calledMethod = ((PsiMethodCallExpression)psiAssignmentExpression.getRExpression()).resolveMethod();
                        if (calledMethod != null) {
                            if(this.allClasses.keySet().contains(calledMethod.getContainingClass())) {
                                String firstLine = calledMethod.getText().split("\n")[0];
                                if (firstLine.contains("static")) {
                                    MyMethod newMethod = new MyMethod(calledMethod);
                                    if (this.isGetter(calledMethod))
                                        newMethod.setGetter();
                                    if (this.isSetter(calledMethod))
                                        newMethod.setSetter();
                                    methodsAccessed.add(newMethod);
                                }
                            }
                        }
                    }
                }
            }

            for (MyMethod myMethod : methodsAccessed) {
                Entity entity = new Entity(myMethod, myMethod.method.getContainingClass());
                if(!this.accessos.containsKey(attribute))
                    this.accessos.put(attribute, new ArrayList<>());
                this.accessos.get(attribute).add(entity);
            }
        }
    }

    public void getAllEntitiesAttributes(){
        PsiField[] fields = this._class.getFields();
        RefactorUtils utils = new RefactorUtils();
        for (MyMethod method : this.methods) {
            Set<MyAttribute> attributesAccessed = new HashSet<>();
            for (PsiStatement statement : utils.getAllStatements(method.method)) {
                for (PsiField field : fields) {
                    if(statement.getText().contains("this."+field.getName()) || statement.getText().contains(field.getName())) {
                        if(!field.getText().contains("static"))
                            attributesAccessed.add(new MyAttribute(field));
                    }
                }
            }
            for (PsiReferenceExpression reference : PsiTreeUtil.findChildrenOfType(method.method, PsiReferenceExpression.class)) {
                for (PsiClass psiClass : this.allClasses.keySet()) {
                    if(!psiClass.getName().equals(this._class.getName())){
                        for (PsiAssignmentExpression psiAssignmentExpression : PsiTreeUtil.findChildrenOfType(this._class, PsiAssignmentExpression.class)) {
                            if(psiAssignmentExpression.getLExpression().getType().toString().equals(psiClass.getName())){
                                for (PsiField psiField : this.allClasses.get(psiClass)) {
                                    if(reference.getText().contains(psiField.getName()) &&
                                            reference.getText().contains(psiAssignmentExpression.getLExpression().getText().split(" ")[1])){
                                        if(!psiField.getText().contains("static"))
                                            attributesAccessed.add(new MyAttribute(psiField));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (MyAttribute attribute : attributesAccessed) {
                Entity entity = new Entity(attribute, attribute.attribute.getContainingClass());
                if(!this.accessos.containsKey(method))
                    this.accessos.put(method, new ArrayList<>());
                this.accessos.get(method).add(entity);
            }
        }

        for (MyAttribute attribute : this.attributes) {
            Set<MyAttribute> attributesAccessed = new HashSet<>();
            for (PsiAssignmentExpression psiAssignmentExpression : PsiTreeUtil.findChildrenOfType(this._class, PsiAssignmentExpression.class)) {
                if(psiAssignmentExpression.getLExpression().getText().contains(attribute.attribute.getName()) || psiAssignmentExpression.getLExpression().getText().contains("this." + attribute.attribute.getName())) {
                    for (PsiField field : fields) {
                        if(psiAssignmentExpression.getRExpression().getText().contains("this."+field.getName()) || psiAssignmentExpression.getRExpression().getText().contains(field.getName())) {
                            if (!field.getText().contains("static"))
                                attributesAccessed.add(new MyAttribute(field));
                        }
                    }

                    for (PsiReferenceExpression reference : PsiTreeUtil.findChildrenOfType(psiAssignmentExpression, PsiReferenceExpression.class)) {
                        for (PsiField field : fields) {
                            if(reference.getText().contains("this."+field.getName()) || reference.getText().contains(field.getName())) {
                                if (!field.getText().contains("static"))
                                    attributesAccessed.add(new MyAttribute(field));
                            }
                        }

                        for (PsiClass psiClass : this.allClasses.keySet()) {
                            if(!psiClass.getName().equals(this._class.getName())){
                                if(attribute.attribute.getType().toString().equals(psiClass.getName())){
                                    for (PsiField psiField : this.allClasses.get(psiClass)) {
                                        if(reference.getText().contains(psiField.getName()) &&
                                                reference.getText().contains(psiAssignmentExpression.getLExpression().getText().split(" ")[1])){
                                            if(!psiField.getText().contains("static"))
                                                attributesAccessed.add(new MyAttribute(psiField));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (MyAttribute myAttribute : attributesAccessed) {
                Entity entity = new Entity(myAttribute, myAttribute.attribute.getContainingClass());
                if(!this.accessos.containsKey(attribute))
                    this.accessos.put(attribute, new ArrayList<>());
                this.accessos.get(attribute).add(entity);
            }
        }
    }

    public void getAllEntities(){
        this.getAllEntitiesMethods();
        this.getAllEntitiesAttributes();

        for (Object o : this.accessos.keySet()) {
            List<PsiClass> classes = new ArrayList<>();
            if(o instanceof MyMethod){
                MyMethod method = (MyMethod) o;
                for (Entity entity : this.accessos.get(method)) {
                    if(!classes.contains(entity._class))
                        classes.add(entity._class);
                }
            }
            for (PsiClass aClass : classes) {
                int numAttributes = 0;
                int numMethods = 0;
                MyMethod method = (MyMethod) o;
                for (Entity entity : this.accessos.get(method)) {
                    if(entity._class.getName().equals(aClass.getName())){
                        if(entity.type instanceof MyMethod)
                            numMethods++;
                        else if(entity.type instanceof MyAttribute)
                            numAttributes++;
                    }
                }
                double result = (double)(1/(0.6 * numMethods + 0.4 * numAttributes));
                HashMap<PsiClass,Double>values = new HashMap<>();
                values.put(aClass, result);
                this.accessosMethod.put(method, values);
            }
        }
    }

    public void getAllMethods(){
        RefactorUtils utils = new RefactorUtils();
        for (PsiMethod method : utils.getMethods(this._class)) {
            System.out.println(method.getName());
            if(utils.getAllStatements(method).size() > (2*ThresholdsCandidates.minNumStatements)){
                MyMethod myMethod = new MyMethod(method);
                if(this.isGetter(method))
                    myMethod.setGetter();
                if(this.isSetter(method))
                    myMethod.setSetter();

                this.methods.add(myMethod);
            }
        }
    }

    private void getAllAttributes() {
        PsiField[] fields = this._class.getFields();
        for (PsiField field : fields) {
            this.attributes.add(new MyAttribute(field));
        }
    }

    public void getAllAccessedMethods(){
        for (MyMethod method : this.methods) {
            Set<MyMethod> methodsAccessed = new HashSet<>();
            for (PsiMethodCallExpression psiMethodCallExpression : PsiTreeUtil.findChildrenOfType(method.method, PsiMethodCallExpression.class)) {
                PsiMethod calledMethod = psiMethodCallExpression.resolveMethod();
                if(calledMethod != null){
                    if(this.allClasses.keySet().contains(calledMethod.getContainingClass())){
                        if(!calledMethod.getName().equals(method.method.getName())){
                            String firstLine = calledMethod.getText().split("\n")[0];
                            if(firstLine.contains("static")){
                                MyMethod newMethod = new MyMethod(calledMethod);
                                if(this.isGetter(calledMethod))
                                    newMethod.setGetter();
                                if(this.isSetter(calledMethod))
                                    newMethod.setSetter();
                                methodsAccessed.add(newMethod);
                            }
                        }
                    }
                }
            }

            this.accessedMethods.put(method, methodsAccessed);
        }

        for (MyAttribute attribute : this.attributes) {
            Set<MyMethod> methodsAccessed = new HashSet<>();
            for (PsiAssignmentExpression psiAssignmentExpression : PsiTreeUtil.findChildrenOfType(this._class, PsiAssignmentExpression.class)) {
                if(psiAssignmentExpression.getLExpression().getText().equals(attribute.attribute.getName())){
                    if(psiAssignmentExpression.getRExpression() instanceof PsiMethodCallExpression){
                        PsiMethod calledMethod = ((PsiMethodCallExpression)psiAssignmentExpression.getRExpression()).resolveMethod();
                        if (calledMethod != null) {
                            if(this.allClasses.keySet().contains(calledMethod.getContainingClass())) {
                                String firstLine = calledMethod.getText().split("\n")[0];
                                if (firstLine.contains("static")) {
                                    MyMethod newMethod = new MyMethod(calledMethod);
                                    if (this.isGetter(calledMethod))
                                        newMethod.setGetter();
                                    if (this.isSetter(calledMethod))
                                        newMethod.setSetter();
                                    methodsAccessed.add(newMethod);
                                }
                            }
                        }
                    }
                }
            }

            this.accessedMethods.put(attribute, methodsAccessed);
        }
    }

    public void getAllAccessedAttributes(){
        PsiField[] fields = this._class.getFields();
        RefactorUtils utils = new RefactorUtils();
        for (MyMethod method : this.methods) {
            Set<MyAttribute> attributesAccessed = new HashSet<>();
            for (PsiStatement statement : utils.getAllStatements(method.method)) {
                for (PsiField field : fields) {
                    if(statement.getText().contains("this."+field.getName()) || statement.getText().contains(field.getName())) {
                        if(!field.getText().contains("static"))
                            attributesAccessed.add(new MyAttribute(field));
                    }
                }
            }
            for (PsiReferenceExpression reference : PsiTreeUtil.findChildrenOfType(method.method, PsiReferenceExpression.class)) {
                for (PsiClass psiClass : this.allClasses.keySet()) {
                    if(!psiClass.getName().equals(this._class.getName())){
                        for (PsiAssignmentExpression psiAssignmentExpression : PsiTreeUtil.findChildrenOfType(this._class, PsiAssignmentExpression.class)) {
                            if(psiAssignmentExpression.getLExpression().getType().toString().equals(psiClass.getName())){
                                for (PsiField psiField : this.allClasses.get(psiClass)) {
                                    if(reference.getText().contains(psiField.getName()) &&
                                            reference.getText().contains(psiAssignmentExpression.getLExpression().getText().split(" ")[1])){
                                        if(!psiField.getText().contains("static"))
                                            attributesAccessed.add(new MyAttribute(psiField));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.accessedAttributes.put(method, attributesAccessed);
        }

        for (MyAttribute attribute : this.attributes) {
            Set<MyAttribute> attributesAccessed = new HashSet<>();
            for (PsiAssignmentExpression psiAssignmentExpression : PsiTreeUtil.findChildrenOfType(this._class, PsiAssignmentExpression.class)) {
                if(psiAssignmentExpression.getLExpression().getText().contains(attribute.attribute.getName()) || psiAssignmentExpression.getLExpression().getText().contains("this." + attribute.attribute.getName())) {
                    for (PsiField field : fields) {
                        if(psiAssignmentExpression.getRExpression().getText().contains("this."+field.getName()) || psiAssignmentExpression.getRExpression().getText().contains(field.getName())) {
                            if (!field.getText().contains("static"))
                                attributesAccessed.add(new MyAttribute(field));
                        }
                    }

                    for (PsiReferenceExpression reference : PsiTreeUtil.findChildrenOfType(psiAssignmentExpression, PsiReferenceExpression.class)) {
                        for (PsiField field : fields) {
                            if(reference.getText().contains("this."+field.getName()) || reference.getText().contains(field.getName())) {
                                if (!field.getText().contains("static"))
                                    attributesAccessed.add(new MyAttribute(field));
                            }
                        }

                        for (PsiClass psiClass : this.allClasses.keySet()) {
                            if(!psiClass.getName().equals(this._class.getName())){
                                if(attribute.attribute.getType().toString().equals(psiClass.getName())){
                                    for (PsiField psiField : this.allClasses.get(psiClass)) {
                                        if(reference.getText().contains(psiField.getName()) &&
                                                reference.getText().contains(psiAssignmentExpression.getLExpression().getText().split(" ")[1])){
                                            if(!psiField.getText().contains("static"))
                                                attributesAccessed.add(new MyAttribute(psiField));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.accessedAttributes.put(attribute, attributesAccessed);
        }
    }

    private void getAllInOut() {
        for (Object o : this.accessedMethods.keySet()) {
            Set<MyMethod> methodsIn = new HashSet<>();
            Set<MyMethod> methodsOut = new HashSet<>();
            for (MyMethod myMethod : this.accessedMethods.get(o)) {
                if(this.methods.contains(myMethod))
                   methodsIn.add(myMethod) ;
                else
                    methodsOut.add(myMethod) ;
            }
            this.accessedMethodsIn.put(o,methodsIn);
            this.accessedMethodsOut.put(o,methodsOut);
        }
        for (Object o : this.accessedAttributes.keySet()) {
            Set<MyAttribute> attributesIn = new HashSet<>();
            Set<MyAttribute> attributesOut = new HashSet<>();
            for (MyAttribute myAttribute : this.accessedAttributes.get(o)) {
                if(this.attributes.contains(myAttribute))
                    attributesIn.add(myAttribute) ;
                else
                    attributesOut.add(myAttribute);
            }
            this.accessedAttributesIn.put(o,attributesIn);
            this.accessedAttributesOut.put(o,attributesOut);
        }
    }

    public boolean isGetter(PsiMethod method){
        if(method.getBody().getStatements().length == 1) {
            PsiField[] fields = this._class.getFields();
            if (method.getBody().getStatements()[0] instanceof PsiReturnStatement) {
                PsiReturnStatement returnStm = (PsiReturnStatement) method.getBody().getStatements()[0];
                for (PsiField field : fields) {
                    if (returnStm.getReturnValue().getText().contains(field.getName())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isSetter(PsiMethod method) {
        if (method.getBody().getStatements().length == 1) {
            PsiField[] fields = this._class.getFields();
            for (PsiElement child : method.getBody().getStatements()[0].getChildren()) {
                if (child instanceof PsiAssignmentExpression) {
                    PsiAssignmentExpression assign = (PsiAssignmentExpression) child;
                    for (PsiField field : fields) {
                        if (assign.getLExpression().getText().contains(field.getName()) &&
                                assign.getText().contains("=")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void getAllClasses(){
        Project project = this._class.getProject();
        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        FileTypeIndex.NAME,
                        JavaFileType.INSTANCE,
                        GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : containingFiles) {
            if(virtualFile.getPath().contains("/src/") && (!virtualFile.getPath().contains("/test/")
                    || !virtualFile.getPath().contains("/resources/"))){
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                int pos = psiFile.getName().lastIndexOf(".");
                String fileName = psiFile.getName().substring(0,pos);
                if(this.file.getText().contains(fileName)){
                    if (psiFile instanceof PsiJavaFile) {
                        PsiClass[] classes = ((PsiJavaFile)psiFile).getClasses();
                        for (PsiClass aClass : classes) {
                            this.allClasses.put(aClass, aClass.getFields());
                        }
                    }
                }
            }
        }
    }
}
