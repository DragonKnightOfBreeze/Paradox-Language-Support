// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.*;
import icu.windea.pls.core.model.*;
import icu.windea.pls.script.expression.reference.*;
import org.jetbrains.annotations.*;

import java.util.*;

public interface ParadoxScriptString extends ParadoxScriptValue, ParadoxScriptExpressionElement, StubBasedPsiElement<ParadoxScriptStringStub> {

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptString setName(@NotNull String value);

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptString setValue(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  ParadoxScriptValueReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @NotNull
  String getStringValue();

  @NotNull
  ParadoxValueType getValueType();

  @Nullable
  String getConfigExpression();

}
