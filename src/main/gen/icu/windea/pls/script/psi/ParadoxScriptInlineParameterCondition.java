// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public interface ParadoxScriptInlineParameterCondition extends PsiElement {

  @NotNull
  List<ParadoxScriptInlineParameterCondition> getInlineParameterConditionList();

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @Nullable
  ParadoxScriptParameterConditionExpression getParameterConditionExpression();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getConditionExpression();

  @Nullable String getPresentationText();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
