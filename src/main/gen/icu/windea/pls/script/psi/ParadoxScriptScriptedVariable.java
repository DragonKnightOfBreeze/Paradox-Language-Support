// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.lang.psi.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptScriptedVariable extends ParadoxScriptNamedElement, ParadoxTypedElement, StubBasedPsiElement<ParadoxScriptScriptedVariableStub> {

  @NotNull
  ParadoxScriptScriptedVariableName getScriptedVariableName();

  @Nullable
  ParadoxScriptValue getScriptedVariableValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxScriptScriptedVariable setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

  @Nullable
  String getValue();

  @Nullable
  String getUnquotedValue();

  @Nullable
  ParadoxType getType();

  boolean isEquivalentTo(@NotNull PsiElement another);

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
