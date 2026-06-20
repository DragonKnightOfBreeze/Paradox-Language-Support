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

public interface ParadoxScriptConditionalBlock extends ParadoxScriptBoundMemberContainer {

  @NotNull
  List<ParadoxScriptConditionalBlock> getConditionalBlockList();

  @Nullable
  ParadoxScriptConditionalBlockExpression getConditionalBlockExpression();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getConditionExpression();

  @Nullable String getPresentationText();

  @NotNull ParadoxScriptConditionalBlock getMembersRoot();

  @NotNull List<@NotNull ParadoxScriptMember> getMembers();

  @Nullable PsiElement getLeftBound();

  @Nullable PsiElement getRightBound();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
