// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.StubBasedPsiElement;
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationLocaleStub;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import javax.swing.Icon;

public interface ParadoxLocalisationLocale extends PsiPresentableTextAwareElement, NavigatablePsiElement, StubBasedPsiElement<ParadoxLocalisationLocaleStub> {

  @NotNull PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxLocalisationLocale setName(@NotNull String name);

  @NotNull IElementType getIElementType();

  @NotNull String getPresentableText();

  @NotNull ParadoxLocalisationElementPresentation getPresentation();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
