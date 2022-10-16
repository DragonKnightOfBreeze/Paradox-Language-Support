// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import icu.windea.pls.core.model.ParadoxValueType;

public interface ParadoxScriptPropertyKey extends ParadoxScriptExpressionElement {

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
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @NotNull
  ParadoxValueType getValueType();

  @Nullable
  String getConfigExpression();

}
