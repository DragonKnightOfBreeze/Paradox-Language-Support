// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.PsiReference;
import icu.windea.pls.model.ParadoxValueType;
import icu.windea.pls.script.expression.reference.ParadoxScriptValueReference;

public interface ParadoxScriptString extends ParadoxScriptValue, ParadoxScriptExpression, StubBasedPsiElement<ParadoxScriptValueStub> {

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

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
