// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface ParadoxScriptInlineConditionalBlock extends ParadoxScriptConditionalBlock, ParadoxScriptInterpolation, ParadoxScriptInterpolationContainer {

    @Nullable
    ParadoxScriptConditionalExpression getConditionalExpression();

    @NotNull
    List<ParadoxScriptNormalParameter> getNormalParameterList();

    @Nullable PsiElement getLeftBound();

    @Nullable PsiElement getRightBound();

    // WARNING: parameter(...) is skipped
    // matching parameter(ParadoxScriptInlineConditionalBlock, ...)
    // methods are not found in ParadoxScriptPsiImplUtil

    @NotNull Icon getIcon(@IconFlags int flags);

    @NotNull String getPresentableText();

    @NotNull GlobalSearchScope getResolveScope();

    @NotNull SearchScope getUseScope();

    @NotNull ItemPresentation getPresentation();

}
