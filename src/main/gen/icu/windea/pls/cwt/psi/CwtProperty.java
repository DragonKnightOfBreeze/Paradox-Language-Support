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
import java.util.List;

public interface CwtProperty extends CwtNamedElement, CwtMember, PsiPresentableTextAwareElement, NavigatablePsiElement {

  @Nullable CwtBlock getMemberContainer();

  @Nullable List<@NotNull CwtMember> getMembers();

  @NotNull
  CwtPropertyKey getPropertyKey();

  @Nullable
  CwtValue getPropertyValue();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull CwtProperty setName(@NotNull String name);

  @NotNull PsiElement getNameIdentifier();

  @Nullable String getValue();

  @NotNull String getPresentableText();

  @NotNull CwtElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
