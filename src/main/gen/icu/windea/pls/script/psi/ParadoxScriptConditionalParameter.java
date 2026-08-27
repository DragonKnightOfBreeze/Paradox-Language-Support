// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxScriptConditionalParameter extends ParadoxConditionParameter {

  @NotNull PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxScriptConditionalParameter setName(@NotNull String name);

  int getTextOffset();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
