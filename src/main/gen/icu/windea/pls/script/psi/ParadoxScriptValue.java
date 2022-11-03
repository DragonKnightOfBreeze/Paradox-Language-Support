// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.core.expression.ParadoxExpressionType;
import javax.swing.Icon;

public interface ParadoxScriptValue extends ParadoxScriptTypedElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  @NotNull
  ParadoxExpressionType getExpressionType();

  @Nullable
  String getConfigExpression();

}
