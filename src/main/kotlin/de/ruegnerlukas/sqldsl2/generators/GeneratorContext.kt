package de.ruegnerlukas.sqldsl2.generators

interface GeneratorContext {
	fun query(): QueryGenerator
	fun select(): SelectExpressionGenerator
	fun from(): FromExpressionGenerator
	fun where(): WhereExpressionGenerator
	fun groupBy(): GroupByExpressionGenerator
	fun having(): HavingExpressionGenerator
	fun orderBy(): OrderByExpressionGenerator
	fun aggFunc(): AggregateFunctionGenerator
	fun expr(): ExpressionGenerator
	fun columnExpr(): ColumnExprGenerator
	fun condition(): ConditionGenerator
	fun literal(): LiteralGenerator
	fun operation(): OperationExprGenerator
	fun joinClause(): JoinClauseGenerator
}