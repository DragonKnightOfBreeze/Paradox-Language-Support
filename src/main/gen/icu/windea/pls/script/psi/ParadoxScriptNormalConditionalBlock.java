// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public interface ParadoxScriptNormalConditionalBlock extends ParadoxScriptConditionalBlock, ParadoxScriptStatement, ParadoxScriptMemberContainer, ParadoxScriptBoundMemberContainer, PsiListLikeElement {

  @Nullable
  ParadoxScriptConditionalExpression getConditionalExpression();

  @NotNull
  List<ParadoxScriptNormalConditionalBlock> getNormalConditionalBlockList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull ParadoxScriptNormalConditionalBlock getMemberContainer();

  @NotNull List<@NotNull ParadoxScriptMember> getMembers();

  @Nullable PsiElement getLeftBound();

  @Nullable PsiElement getRightBound();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
