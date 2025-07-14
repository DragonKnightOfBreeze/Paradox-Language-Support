// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.references.ParadoxParameterPsiReference;
import javax.swing.Icon;

public interface ParadoxScriptInlineMathParameter extends ParadoxScriptInlineMathFactor, ParadoxParameter, ParadoxScriptArgumentAwareElement {

  @Nullable PsiElement getIdElement();

  @Nullable ParadoxScriptParameterArgument getArgumentElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getName();

  @NotNull ParadoxScriptInlineMathParameter setName(@NotNull String name);

  @Nullable String getValue();

  int getTextOffset();

  @Nullable String getDefaultValue();

  @Nullable ParadoxParameterPsiReference getReference();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
