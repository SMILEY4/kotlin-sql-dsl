package de.ruegnerlukas.sqldsl.grammar.select


open class SelectStatement(val expressions: List<SelectExpression<*>>, val distinct: Boolean = false)

// TODO: maybe not needed anymore
class SelectDistinctStatement(expressions: List<SelectExpression<*>>): SelectStatement(expressions, true)
