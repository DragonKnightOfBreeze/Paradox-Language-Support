// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ParadoxScriptPropertyValue extends PsiElement {

  @Nullable
  ParadoxScriptBlock getBlock();

  @Nullable
  ParadoxScriptBoolean getBoolean();

  @Nullable
  ParadoxScriptColor getColor();

  @Nullable
  ParadoxScriptFloat getFloat();

  @Nullable
  ParadoxScriptInlineMath getInlineMath();

  @Nullable
  ParadoxScriptInt getInt();

  @Nullable
  ParadoxScriptString getString();

  @Nullable
  ParadoxScriptVariableReference getVariableReference();

  @NotNull
  ParadoxScriptValue getValue();

}
