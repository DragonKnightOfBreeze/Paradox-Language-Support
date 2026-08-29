// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiRootBlock;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface ParadoxScriptRootBlock extends PsiRootBlock, ParadoxScriptMemberContainer {

  @NotNull
  List<ParadoxScriptNormalConditionalBlock> getNormalConditionalBlockList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull ParadoxScriptRootBlock getMemberContainer();

  @NotNull List<@NotNull ParadoxScriptMember> getMembers();

  @NotNull String getValue();

  @NotNull List<@NotNull ParadoxScriptStatement> getComponents();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
