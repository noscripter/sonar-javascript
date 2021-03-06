/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.javascript.checks;

import org.sonar.check.Rule;
import org.sonar.javascript.checks.utils.CheckUtils;
import org.sonar.plugins.javascript.api.symbols.Symbol;
import org.sonar.plugins.javascript.api.symbols.Usage;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.expression.ExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.IdentifierTree;
import org.sonar.plugins.javascript.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.javascript.api.visitors.DoubleDispatchVisitorCheck;

@Rule(key = "S3001")
public class DeleteNonPropertyCheck extends DoubleDispatchVisitorCheck {

  private static final String MESSAGE = "Remove this \"delete\" operator or pass an object property to it.";

  @Override
  public void visitUnaryExpression(UnaryExpressionTree tree) {
    ExpressionTree argument = CheckUtils.removeParenthesis(tree.expression());
    if (tree.is(Tree.Kind.DELETE) && !isMemberAccess(argument) && !isGlobalProperty(argument)) {
      addIssue(tree, MESSAGE);
    }

    super.visitUnaryExpression(tree);
  }

  /**
   * Return true for variables declared without var
   */
  private static boolean isGlobalProperty(ExpressionTree expression) {
    if (expression.is(Kind.IDENTIFIER_REFERENCE)) {

      Symbol symbol = ((IdentifierTree) expression).symbol();
      if (symbol != null) {

        for (Usage usage : symbol.usages()) {
          if (usage.isDeclaration()) {
            return false;
          }
        }
        return true;

      }
    }

    return false;
  }

  private static boolean isMemberAccess(ExpressionTree tree) {
    return tree.is(Kind.DOT_MEMBER_EXPRESSION, Kind.BRACKET_MEMBER_EXPRESSION);
  }
}
