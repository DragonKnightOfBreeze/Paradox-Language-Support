// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.localisation.reference.ParadoxLocalisationCommandScopeReference;
import javax.swing.Icon;

public interface ParadoxLocalisationCommandScope extends ParadoxLocalisationCommandIdentifier, ParadoxLocalisationNamedElement {

  @NotNull
  PsiElement getCommandScopeId();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationCommandScope setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  ParadoxLocalisationCommandScopeReference getReference();

  @Nullable
  ParadoxLocalisationCommandIdentifier getPrevIdentifier();

  @Nullable
  ParadoxLocalisationCommandIdentifier getNextIdentifier();

}
