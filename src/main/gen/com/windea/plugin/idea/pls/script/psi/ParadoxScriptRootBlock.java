// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

import javax.swing.Icon;

public interface ParadoxScriptRootBlock extends ParadoxScriptBlock {

  boolean isEmpty();

  boolean isNotEmpty();

  boolean isObject();

  boolean isArray();

  @NotNull
  List<PsiElement> getComponents();

  @NotNull
  Icon getIcon(@IconFlags int flags);

}
