// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.script.references.ParadoxParameterPsiReference;
import javax.swing.Icon;

public interface ParadoxScriptParameterConditionParameter extends ParadoxArgument {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptParameterConditionParameter setName(@NotNull String name);

  int getTextOffset();

  @NotNull
  ParadoxParameterPsiReference getReference();

}
