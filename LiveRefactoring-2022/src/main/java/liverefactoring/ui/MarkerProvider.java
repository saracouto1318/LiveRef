package liverefactoring.ui;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import liverefactoring.ui.icons.IconRenderer;
import org.jetbrains.annotations.NotNull;

public class MarkerProvider implements LineMarkerProvider {

    public MarkerProvider(){

    }
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        System.out.println("tentei");
        return new LineMarkerInfo(element,
                element.getTextRange(),
                IconRenderer.gutter1,
                null,
                null,
                GutterIconRenderer.Alignment.CENTER);
    }

}
