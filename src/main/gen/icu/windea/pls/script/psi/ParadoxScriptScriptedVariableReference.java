// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.script.exp.ParadoxDataType;
import icu.windea.pls.script.references.ParadoxScriptedVariablePsiReference;
import javax.swing.Icon;

public interface ParadoxScriptScriptedVariableReference extends ParadoxScriptValue, ParadoxScriptedVariableReference {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptScriptedVariableReference setName(@NotNull String name);

  @NotNull
  ParadoxScriptedVariablePsiReference getReference();

  @NotNull
  ParadoxDataType getExpressionType();

}
