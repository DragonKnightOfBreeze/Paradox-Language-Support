// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxScriptInlineMathScriptedVariableReference extends ParadoxScriptInlineMathFactor, ParadoxScriptedVariableReference, ParadoxScriptMacro, ParadoxScriptInterpolationContainer {

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxScriptInlineMathScriptedVariableReference setName(@NotNull String name);

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
