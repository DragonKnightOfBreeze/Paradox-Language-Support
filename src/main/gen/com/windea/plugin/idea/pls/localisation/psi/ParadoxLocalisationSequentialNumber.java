// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

import javax.swing.Icon;

public interface ParadoxLocalisationSequentialNumber extends ParadoxLocalisationRichText, ParadoxLocalisationNamedElement {

  @Nullable
  PsiElement getSequentialNumberId();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  void checkRename();

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

  @NotNull
  Icon getIcon(@IconFlags int flags);

}
