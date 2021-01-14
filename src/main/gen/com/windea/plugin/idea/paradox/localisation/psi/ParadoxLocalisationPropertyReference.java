// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxLocalisationPropertyPsiReference;

public interface ParadoxLocalisationPropertyReference extends ParadoxLocalisationRichText {

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

  int getTextOffset();

  @Nullable
  ParadoxLocalisationPropertyPsiReference getReference();

}
