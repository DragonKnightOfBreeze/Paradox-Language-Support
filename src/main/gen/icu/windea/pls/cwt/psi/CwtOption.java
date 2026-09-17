// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface CwtOption extends CwtNamedElement, CwtOptionMember, PsiPresentableTextAwareElement, NavigatablePsiElement {

  @NotNull
  CwtOptionKey getOptionKey();

  @Nullable
  CwtValue getOptionValue();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull CwtOption setName(@NotNull String name);

  @NotNull PsiElement getNameIdentifier();

  @Nullable String getValue();

  @NotNull String getPresentableText();

  @NotNull CwtElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
