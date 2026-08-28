// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ParadoxScriptScriptedVariableName extends ParadoxScriptInterpolationContainer {

  @NotNull
  List<ParadoxScriptNormalParameter> getNormalParameterList();

  @Nullable PsiElement getIdElement();

  //WARNING: parameter(...) is skipped
  //matching parameter(ParadoxScriptScriptedVariableName, ...)
  //methods are not found in ParadoxScriptPsiImplUtil

  @NotNull String getName();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
