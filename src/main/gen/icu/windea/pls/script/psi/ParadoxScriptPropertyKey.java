// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import icu.windea.pls.script.exp.ParadoxDataType;

public interface ParadoxScriptPropertyKey extends ParadoxScriptExpressionElement, StubBasedPsiElement<ParadoxScriptPropertyKeyStub> {

  @NotNull
  List<ParadoxScriptParameter> getParameterList();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptPropertyKey setValue(@NotNull String value);

  @Nullable
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @NotNull
  ParadoxDataType getExpressionType();

  @Nullable
  String getConfigExpression();

  @Nullable
  ItemPresentation getPresentation();

  boolean isEquivalentTo(@NotNull PsiElement another);

}
