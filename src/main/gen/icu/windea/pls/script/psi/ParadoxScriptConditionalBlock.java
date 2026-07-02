// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public interface ParadoxScriptConditionalBlock extends ParadoxScriptStatement, ParadoxScriptBoundMemberContainer {

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

  @NotNull List<@NotNull ParadoxScriptStatement> getComponents();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
