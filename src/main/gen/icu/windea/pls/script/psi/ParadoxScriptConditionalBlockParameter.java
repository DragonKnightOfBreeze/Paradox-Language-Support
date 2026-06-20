// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.references.script.ParadoxConditionParameterPsiReference;
import javax.swing.Icon;

public interface ParadoxScriptConditionalBlockParameter extends ParadoxConditionParameter {

  @NotNull PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxScriptConditionalBlockParameter setName(@NotNull String name);

  int getTextOffset();

  @NotNull ParadoxConditionParameterPsiReference getReference();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
