// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface ParadoxScriptInlineMathUnaryExpression extends ParadoxScriptInlineMathExpression {

  @Nullable
  ParadoxScriptInlineMathAbsExpression getInlineMathAbsExpression();

  @Nullable
  ParadoxScriptInlineMathFactor getInlineMathFactor();

  @Nullable
  ParadoxScriptInlineMathParExpression getInlineMathParExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
