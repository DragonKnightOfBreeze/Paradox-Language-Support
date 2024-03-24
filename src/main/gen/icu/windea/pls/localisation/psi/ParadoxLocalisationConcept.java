// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.localisation.references.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxLocalisationConcept extends PsiElement {

  @NotNull
  ParadoxLocalisationConceptName getConceptName();

  @Nullable
  ParadoxLocalisationConceptText getConceptText();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationConcept setName(@NotNull String name);

  @Nullable
  ParadoxLocalisationConceptPsiReference getReference();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
