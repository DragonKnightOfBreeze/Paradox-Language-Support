// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.ParadoxType;
import javax.swing.Icon;

public interface ParadoxLocalisationConceptName extends NavigatablePsiElement, ContributedReferenceHost, ParadoxLocalisationExpressionElement, ParadoxLocalisationParameterAwareElement {

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxLocalisationConceptName setValue(@NotNull String value);

  @Nullable ParadoxType getType();

  @NotNull String getExpression();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
