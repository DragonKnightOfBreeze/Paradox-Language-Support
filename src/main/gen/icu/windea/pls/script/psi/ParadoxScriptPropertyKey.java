// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.*;
import icu.windea.pls.core.model.*;
import icu.windea.pls.script.expression.reference.*;
import org.jetbrains.annotations.*;

import java.util.*;

public interface ParadoxScriptPropertyKey extends ParadoxScriptExpressionElement, ParadoxScriptConfigAwareElement {

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptPropertyKey setName(@NotNull String value);

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptPropertyKey setValue(@NotNull String value);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  ParadoxScriptKeyReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @NotNull
  ParadoxValueType getValueType();

  @Nullable
  String getConfigExpression();

}
