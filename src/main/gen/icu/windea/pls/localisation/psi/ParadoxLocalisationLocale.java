// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationLocaleStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxLocalisationLocale extends NavigatablePsiElement, StubBasedPsiElement<ParadoxLocalisationLocaleStub> {

  @NotNull PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxLocalisationLocale setName(@NotNull String name);

  @NotNull IElementType getIElementType();

  @NotNull ParadoxLocalisationElementPresentation getPresentation();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
