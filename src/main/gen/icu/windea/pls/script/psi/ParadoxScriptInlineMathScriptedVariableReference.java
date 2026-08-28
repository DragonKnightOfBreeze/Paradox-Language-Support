// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxScriptInlineMathScriptedVariableReference extends ParadoxScriptInlineMathFactor, ParadoxScriptedVariableReference, ParadoxScriptInterpolationContainer {

  @Nullable PsiElement getIdElement();

  //WARNING: inline_conditional_block(...) is skipped
  //matching inline_conditional_block(ParadoxScriptInlineMathScriptedVariableReference, ...)
  //methods are not found in ParadoxScriptPsiImplUtil

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getName();

  @NotNull ParadoxScriptInlineMathScriptedVariableReference setName(@NotNull String name);

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
