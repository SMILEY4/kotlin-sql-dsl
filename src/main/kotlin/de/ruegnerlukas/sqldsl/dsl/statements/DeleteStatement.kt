package de.ruegnerlukas.sqldsl.dsl.statements

import de.ruegnerlukas.sqldsl.dsl.expression.Expr
import de.ruegnerlukas.sqldsl.dsl.expression.Returning
import de.ruegnerlukas.sqldsl.dsl.expression.Table

interface SqlDeleteStatement

class DeleteStatement(
	val target: Table,
	val where: Expr<Boolean>? = null,
	val returning: Returning? = null,
): SqlDeleteStatement

interface DeleteBuilderEndStep: SqlDeleteStatement {
	fun build(): DeleteStatement
}