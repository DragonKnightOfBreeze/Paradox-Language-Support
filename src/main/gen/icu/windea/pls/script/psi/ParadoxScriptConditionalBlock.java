// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface ParadoxScriptConditionalBlock extends ParadoxScriptStatement, ParadoxScriptMemberContainer, ParadoxScriptBoundMemberContainer {

  @NotNull
  List<ParadoxScriptConditionalBlock> getConditionalBlockList();

  @Nullable
  ParadoxScriptConditionalExpression getConditionalExpression();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @Nullable PsiElement getLeftBound();

  @Nullable PsiElement getRightBound();

  @NotNull List<@NotNull ParadoxScriptStatement> getComponents();

  @NotNull ParadoxScriptConditionalBlock getMemberContainer();

  @NotNull List<@NotNull ParadoxScriptMember> getMembers();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getConditionExpression();

  @NotNull String getPresentableText();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
