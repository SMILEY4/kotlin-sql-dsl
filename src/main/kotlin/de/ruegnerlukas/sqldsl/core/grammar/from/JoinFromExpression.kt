package de.ruegnerlukas.sqldsl.core.grammar.from

import de.ruegnerlukas.sqldsl.core.grammar.expression.Expression
import de.ruegnerlukas.sqldsl.core.grammar.refs.column.ColumnRef
import de.ruegnerlukas.sqldsl.core.grammar.refs.table.TableRef

class JoinFromExpression(
	val left: FromExpression,
	val right: FromExpression,
	val joinOp: JoinOp,
	val constraint: JoinConstraint,
) : FromExpression

enum class JoinOp {
	LEFT,
	INNER,
	CROSS
}

interface JoinConstraint

class ConditionJoinConstraint(val expression: Expression) : JoinConstraint

class UsingJoinConstraint(val columns: List<ColumnRef<*, *>>) : JoinConstraint


fun joinLeft(block: JoinBuilder.() -> Unit): JoinFromExpression {
	return JoinBuilder(JoinOp.LEFT).apply(block).build()
}

fun joinInner(block: JoinBuilder.() -> Unit) {
	JoinBuilder(JoinOp.INNER).apply(block).build()
}

fun joinCross(block: JoinBuilder.() -> Unit) {
	JoinBuilder(JoinOp.CROSS).apply(block).build()
}

class JoinBuilder(private val op: JoinOp) {

	private var left: FromExpression? = null
	private var right: FromExpression? = null
	private var on: Expression? = null
	private var using: List<ColumnRef<*, *>>? = null

	fun left(table: TableRef) {
		this.left = table
	}

	fun right(table: TableRef) {
		this.right = table
	}

	fun on(expression: Expression) {
		this.on = expression
	}

	fun using(vararg columns: ColumnRef<*, *>) {
		this.using = columns.toList()
	}

	internal fun build(): JoinFromExpression {
		if (on != null) {
			return JoinFromExpression(left!!, right!!, op, ConditionJoinConstraint(on!!))
		} else {
			return JoinFromExpression(left!!, right!!, op, UsingJoinConstraint(using!!))
		}
	}
}