// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.expression.ParadoxDataType;
import javax.swing.Icon;

public interface ParadoxScriptValue extends NavigatablePsiElement, ParadoxScriptExpressionElement, ParadoxScriptMemberElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptValue setValue(@NotNull String name);

  @NotNull
  ParadoxDataType getType();

  @NotNull
  String getExpression();

  @Nullable
  String getConfigExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
