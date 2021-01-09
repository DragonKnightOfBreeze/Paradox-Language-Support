// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface ParadoxScriptProperty extends ParadoxScriptNamedElement, StubBasedPsiElement<ParadoxScriptPropertyStub> {

  @NotNull
  ParadoxScriptPropertyKey getPropertyKey();

  @Nullable
  ParadoxScriptPropertyValue getPropertyValue();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getValue();

  @Nullable
  String getUnquotedValue();

  @Nullable
  String getTruncatedValue();

  @Nullable
  ParadoxScriptProperty findProperty(@NotNull String propertyName);

  @Nullable
  ParadoxScriptValue findValue(@NotNull String value);

}
