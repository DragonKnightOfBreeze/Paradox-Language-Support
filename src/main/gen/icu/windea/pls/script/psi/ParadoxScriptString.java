// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.script.reference.ParadoxScriptStringReference;

public interface ParadoxScriptString extends ParadoxScriptStringValue, PsiLiteralValue {

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptString setValue(@NotNull String name);

  @NotNull
  ParadoxScriptStringReference getReference();

  @NotNull
  String getStringValue();

}
