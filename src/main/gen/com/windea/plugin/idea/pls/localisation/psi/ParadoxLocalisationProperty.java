// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;

import javax.swing.Icon;

public interface ParadoxLocalisationProperty extends ParadoxLocalisationNamedElement, StubBasedPsiElement<ParadoxLocalisationPropertyStub> {

  @NotNull
  ParadoxLocalisationPropertyKey getPropertyKey();

  @Nullable
  ParadoxLocalisationPropertyValue getPropertyValue();

  @Nullable
  PsiElement getNumber();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  void checkRename();

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getValue();

}
