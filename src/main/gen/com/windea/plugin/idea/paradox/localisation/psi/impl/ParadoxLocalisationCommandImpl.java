// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*;
import com.windea.plugin.idea.paradox.localisation.psi.*;

public class ParadoxLocalisationCommandImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationCommand {

  public ParadoxLocalisationCommandImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommand(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandField getCommandField() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationCommandField.class);
  }

  @Override
  @NotNull
  public List<ParadoxLocalisationCommandScope> getCommandScopeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxLocalisationCommandScope.class);
  }

}
