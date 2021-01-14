// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ParadoxLocalisationSerialNumber extends ParadoxLocalisationRichText, ParadoxLocalisationNamedElement {

  @Nullable
  PsiElement getSerialNumberId();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  void checkRename();

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

}
