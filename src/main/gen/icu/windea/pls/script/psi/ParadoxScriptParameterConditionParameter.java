// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.references.script.ParadoxConditionParameterPsiReference;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface ParadoxScriptParameterConditionParameter extends ParadoxConditionParameter {

  @NotNull PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxScriptParameterConditionParameter setName(@NotNull String name);

  @NotNull String getValue();

  int getTextOffset();

  @NotNull ParadoxConditionParameterPsiReference getReference();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
