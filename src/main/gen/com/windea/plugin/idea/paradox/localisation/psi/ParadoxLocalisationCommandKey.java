// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxLocalisationCommandKeyPsiReference;

public interface ParadoxLocalisationCommandKey extends ParadoxLocalisationNamedElement {

  @NotNull
  PsiElement getCommandKeyToken();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name) throws IncorrectOperationException;

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  ParadoxLocalisationCommandKeyPsiReference getReference();

}
