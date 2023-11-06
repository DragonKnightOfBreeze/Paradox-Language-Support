// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public interface ParadoxScriptParameterCondition extends PsiListLikeElement {

  @NotNull
  List<ParadoxScriptParameterCondition> getParameterConditionList();

  @Nullable
  ParadoxScriptParameterConditionExpression getParameterConditionExpression();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getConditionExpression();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<PsiElement> getComponents();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
