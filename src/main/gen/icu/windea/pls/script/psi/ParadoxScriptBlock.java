// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public interface ParadoxScriptBlock extends ParadoxScriptValue, ParadoxScriptBlockElement, ContributedReferenceHost {

  @NotNull
  List<ParadoxScriptParameterCondition> getParameterConditionList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull
  List<PsiElement> getComponents();

  @NotNull
  ParadoxType getType();

  @NotNull
  String getExpression();

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
