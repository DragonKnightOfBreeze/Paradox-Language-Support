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

public interface ParadoxScriptInlineConditionalBlock extends PsiElement {

  @NotNull
  List<ParadoxScriptInlineConditionalBlock> getInlineConditionalBlockList();

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @Nullable
  ParadoxScriptConditionalBlockExpression getConditionalBlockExpression();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getConditionExpression();

  @Nullable String getPresentationText();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
