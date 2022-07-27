// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import icu.windea.pls.model.ParadoxValueType;
import icu.windea.pls.script.expression.reference.ParadoxScriptKeyReference;

public interface ParadoxScriptPropertyKey extends ParadoxScriptExpressionElement {

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

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

  @Nullable
  String getConfigExpression();

  @NotNull
  ParadoxValueType getValueType();

}
