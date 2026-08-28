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

public interface ParadoxScriptInlineConditionalBlock extends ParadoxScriptConditionalBlock, ParadoxScriptInterpolation, ParadoxScriptInterpolationContainer {

  @Nullable
  ParadoxScriptConditionalExpression getConditionalExpression();

  @NotNull
  List<ParadoxScriptNormalParameter> getNormalParameterList();

  @Nullable PsiElement getLeftBound();

  @Nullable PsiElement getRightBound();

  //WARNING: parameter(...) is skipped
  //matching parameter(ParadoxScriptInlineConditionalBlock, ...)
  //methods are not found in ParadoxScriptPsiImplUtil

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getPresentableText();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
