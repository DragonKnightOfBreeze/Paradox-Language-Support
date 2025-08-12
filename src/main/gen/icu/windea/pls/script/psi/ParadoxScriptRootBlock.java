// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface ParadoxScriptRootBlock extends ParadoxScriptBlockElement {

  @NotNull
  List<ParadoxScriptParameterCondition> getParameterConditionList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull String getValue();

  @NotNull List<@NotNull ParadoxScriptMemberElement> getMemberList();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
