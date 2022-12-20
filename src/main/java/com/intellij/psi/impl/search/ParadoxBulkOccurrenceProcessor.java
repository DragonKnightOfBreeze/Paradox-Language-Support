package com.intellij.psi.impl.search;

import com.intellij.psi.*;
import com.intellij.util.text.*;
import org.jetbrains.annotations.*;

@FunctionalInterface
interface ParadoxBulkOccurrenceProcessor {
  boolean execute(@NotNull PsiElement scope, int @NotNull [] offsetsInScope, @NotNull StringSearcher searcher);
}