// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.core.psi.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public interface ParadoxScriptPropertyKey extends PsiLiteralValue, ParadoxScriptStringExpressionElement, ParadoxParameterAwareElement {

  @NotNull
  List<ParadoxScriptInlineParameterCondition> getInlineParameterConditionList();

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptPropertyKey setValue(@NotNull String value);

  @NotNull
  ParadoxType getType();

  @NotNull
  String getExpression();

  @Nullable
  String getConfigExpression();

  @Nullable
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
