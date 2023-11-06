// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import icu.windea.pls.core.psi.*;
import icu.windea.pls.core.references.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public interface ParadoxScriptScriptedVariableReference extends ParadoxScriptValue, ParadoxScriptedVariableReference, ParadoxParameterAwareElement {

  @NotNull
  List<ParadoxScriptInlineParameterCondition> getInlineParameterConditionList();

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptScriptedVariableReference setName(@NotNull String name);

  @NotNull
  String getValue();

  @Nullable
  ParadoxScriptedVariablePsiReference getReference();

  @NotNull
  ParadoxType getType();

  @NotNull
  String getExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
