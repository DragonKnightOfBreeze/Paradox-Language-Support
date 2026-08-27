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
import java.util.List;

public interface ParadoxScriptInlineConditionalBlock extends PsiBoundElement {

  @Nullable
  ParadoxScriptConditionalExpression getConditionalExpression();

  @NotNull
  List<ParadoxScriptInlineConditionalBlock> getInlineConditionalBlockList();

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getConditionExpression();

  @Nullable String getPresentationText();

  @Nullable PsiElement getLeftBound();

  @Nullable PsiElement getRightBound();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
