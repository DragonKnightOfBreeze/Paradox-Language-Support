// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.windea.plugin.idea.pls.localisation.reference.ParadoxLocalisationCommandScopePsiReference;
import javax.swing.Icon;

public interface ParadoxLocalisationCommandScope extends ParadoxLocalisationCommandIdentifier, ParadoxLocalisationNamedElement {

  @NotNull
  PsiElement getCommandScopeId();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  void checkRename();

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  ParadoxLocalisationCommandScopePsiReference getReference();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  ParadoxLocalisationCommandIdentifier getPrevIdentifier();

  @Nullable
  ParadoxLocalisationCommandIdentifier getNextIdentifier();

}
