// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.windea.plugin.idea.paradox.ParadoxColor;
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxLocalisationPropertyPsiReference;

public interface ParadoxLocalisationPropertyReference extends ParadoxLocalisationRichText, ParadoxLocalisationNamedElement {

  @Nullable
  ParadoxLocalisationCommand getCommand();

  @Nullable
  PsiElement getPropertyReferenceId();

  @Nullable
  PsiElement getPropertyReferenceParameter();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

  @Nullable
  ParadoxLocalisationPropertyPsiReference getReference();

  @Nullable
  ParadoxColor getParadoxColor();

}
