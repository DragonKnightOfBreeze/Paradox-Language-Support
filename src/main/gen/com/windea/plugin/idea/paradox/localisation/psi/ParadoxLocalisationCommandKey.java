// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxLocalisationCommandKeyPsiReference;

public interface ParadoxLocalisationCommandKey extends PsiElement {

  @Nullable
  ParadoxLocalisationPropertyReference getPropertyReference();

  @Nullable
  PsiElement getCommandKeyToken();

  @Nullable
  String getName();

  @Nullable
  ParadoxLocalisationCommandKeyPsiReference getReference();

}
