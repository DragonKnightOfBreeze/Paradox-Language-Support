// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface ParadoxScriptValue extends ParadoxScriptExpression {

  @Nullable
  ParadoxScriptStringTemplate getStringTemplate();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  @Nullable
  String getConfigExpression();

}
