// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.script.psi.stubs.ParadoxScriptScriptedVariableStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxScriptScriptedVariable extends ParadoxScriptNamedElement, ParadoxScriptStatement, StubBasedPsiElement<ParadoxScriptScriptedVariableStub> {

  @NotNull
  ParadoxScriptScriptedVariableName getScriptedVariableName();

  @Nullable
  ParadoxScriptValue getScriptedVariableValue();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getName();

  @NotNull ParadoxScriptScriptedVariable setName(@NotNull String name);

  @Nullable PsiElement getNameIdentifier();

  int getTextOffset();

  @Nullable String getValue();

  @NotNull String getPresentableText();

  @NotNull IElementType getIElementType();

  boolean isEquivalentTo(@NotNull PsiElement another);

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
