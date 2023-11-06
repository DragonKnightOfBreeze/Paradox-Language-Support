// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.core.psi.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptProperty extends ParadoxScriptNamedElement, ParadoxTypedElement, ParadoxScriptDefinitionElement, StubBasedPsiElement<ParadoxScriptPropertyStub> {

  @NotNull
  ParadoxScriptPropertyKey getPropertyKey();

  @Nullable
  ParadoxScriptValue getPropertyValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptProperty setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  String getValue();

  int getDepth();

  @Nullable
  ParadoxScriptBlock getBlock();

  @Nullable
  ParadoxType getType();

  @Nullable
  String getConfigExpression();

  @NotNull
  String getExpression();

  boolean isEquivalentTo(@NotNull PsiElement another);

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
