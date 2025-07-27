// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface CwtString extends CwtValue, CwtNamedElement, CwtStringExpressionElement, CwtLiteralValue {

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull CwtString setName(@NotNull String name);

  @NotNull PsiElement getNameIdentifier();

  @NotNull String getValue();

  @NotNull CwtValue setValue(@NotNull String value);

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull ItemPresentation getPresentation();

  @NotNull SearchScope getUseScope();

}
