// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

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
