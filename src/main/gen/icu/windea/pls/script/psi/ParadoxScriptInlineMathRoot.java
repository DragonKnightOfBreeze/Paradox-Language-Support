// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface ParadoxScriptInlineMathRoot extends PsiElement {

  @Nullable
  ParadoxScriptInlineMathAbsExpression getInlineMathAbsExpression();

  @Nullable
  ParadoxScriptInlineMathBiExpression getInlineMathBiExpression();

  @Nullable
  ParadoxScriptInlineMathFactor getInlineMathFactor();

  @Nullable
  ParadoxScriptInlineMathParExpression getInlineMathParExpression();

  @Nullable
  ParadoxScriptInlineMathUnaryExpression getInlineMathUnaryExpression();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
