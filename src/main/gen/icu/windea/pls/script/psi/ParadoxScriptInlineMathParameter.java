// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxScriptInlineMathParameter extends ParadoxScriptInlineMathFactor, ParadoxScriptParameter, ParadoxScriptArgumentAwareElement {

  @Nullable PsiElement getIdElement();

  @Nullable ParadoxScriptInlineMathParameterArgument getArgumentElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getName();

  @NotNull ParadoxScriptInlineMathParameter setName(@NotNull String name);

  int getTextOffset();

  @Nullable String getDefaultValue();

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
