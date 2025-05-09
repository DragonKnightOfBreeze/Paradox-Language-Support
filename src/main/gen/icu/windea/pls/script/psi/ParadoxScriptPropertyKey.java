// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.lang.psi.ParadoxParameterAwareElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.ParadoxType;
import javax.swing.Icon;

public interface ParadoxScriptPropertyKey extends PsiLiteralValue, ParadoxScriptStringExpressionElement, ParadoxParameterAwareElement {

  @NotNull
  List<ParadoxScriptInlineParameterCondition> getInlineParameterConditionList();

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxScriptPropertyKey setValue(@NotNull String value);

  @NotNull ParadoxType getType();

  @NotNull String getExpression();

  @Nullable String getConfigExpression();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
