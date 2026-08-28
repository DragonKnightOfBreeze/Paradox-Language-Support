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
import java.util.List;

public interface ParadoxScriptBlock extends ParadoxScriptValue, ParadoxScriptMemberContainer, ParadoxScriptBoundMemberContainer {

  @NotNull
  List<ParadoxScriptNormalConditionalBlock> getNormalConditionalBlockList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull ParadoxScriptBlock getMemberContainer();

  @NotNull List<@NotNull ParadoxScriptMember> getMembers();

  @Nullable PsiElement getLeftBound();

  @Nullable PsiElement getRightBound();

  @NotNull List<@NotNull ParadoxScriptStatement> getComponents();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getValue();

  @NotNull String getPresentableText();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
