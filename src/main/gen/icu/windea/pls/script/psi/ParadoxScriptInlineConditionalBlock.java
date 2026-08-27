// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiBoundElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxScriptInlineConditionalBlock extends PsiBoundElement, ParadoxScriptInterpolation, ParadoxScriptInterpolationContainer {

  @Nullable
  ParadoxScriptConditionalExpression getConditionalExpression();

  @Nullable PsiElement getLeftBound();

  @Nullable PsiElement getRightBound();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getPresentableText();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
