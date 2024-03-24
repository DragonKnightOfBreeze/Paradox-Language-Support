// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptValue extends NavigatablePsiElement, ParadoxScriptExpressionElement, ParadoxScriptMemberElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptValue setValue(@NotNull String value);

  @NotNull
  ParadoxType getType();

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
