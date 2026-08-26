// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiRootBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ParadoxScriptRootBlock extends PsiRootBlock, ParadoxScriptMemberContainer {

  @NotNull
  List<ParadoxScriptConditionalBlock> getConditionalBlockList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull ParadoxScriptRootBlock getMemberContainer();

  @NotNull List<@NotNull ParadoxScriptMember> getMembers();

  @NotNull List<@NotNull ParadoxScriptStatement> getComponents();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
