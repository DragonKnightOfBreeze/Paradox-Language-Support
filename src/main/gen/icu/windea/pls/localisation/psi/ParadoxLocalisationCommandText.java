// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.ParadoxType;

public interface ParadoxLocalisationCommandText extends NavigatablePsiElement, ContributedReferenceHost, ParadoxLocalisationExpressionElement, ParadoxLocalisationParameterAwareElement {

  @Nullable
  PsiElement getIdElement();

  @NotNull
  String getName();

  @NotNull
  String getValue();

  @NotNull
  ParadoxLocalisationCommandText setValue(@NotNull String value);

  @Nullable
  ParadoxType getType();

  @NotNull
  String getExpression();

  @Nullable
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
