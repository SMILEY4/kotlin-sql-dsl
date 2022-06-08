package de.ruegnerlukas.sqldsl.grammar.table

import de.ruegnerlukas.sqldsl.builders.QueryBuilderEndStep
import de.ruegnerlukas.sqldsl.grammar.expr.ColumnExpr
import de.ruegnerlukas.sqldsl.grammar.expr.DerivedColumn
import de.ruegnerlukas.sqldsl.grammar.from.FromExpression
import de.ruegnerlukas.sqldsl.grammar.from.TableFromExpression
import de.ruegnerlukas.sqldsl.grammar.target.CommonTarget
import de.ruegnerlukas.sqldsl.schema.AnyValueType
import de.ruegnerlukas.sqldsl.schema.BooleanValueType
import de.ruegnerlukas.sqldsl.schema.FloatValueType
import de.ruegnerlukas.sqldsl.schema.IntValueType
import de.ruegnerlukas.sqldsl.schema.StringValueType


/**
 * Base interface for all table-like objects
 */
interface TableBase: TableFromExpression


/**
 * A standard table
 */
interface StandardTable : TableBase, CommonTarget {
	val tableName: String
}


/**
 * A table with a different name
 */
interface AliasTable : TableBase {
	val baseTable: TableBase
	val aliasName: String
}


/**
 * A derived table, e.g. the result from a sub-query or a join
 */
class DerivedTable(val tableName: String) : TableBase {

	private var content: FromExpression? = null

	fun assign(content: FromExpression): DerivedTable {
		this.content = content
		return this
	}

	fun assign(content: QueryBuilderEndStep<*>): DerivedTable {
		this.content = content.build()
		return this
	}

	fun getContent(): FromExpression {
		return this.content ?: throw IllegalStateException("No content assigned to derived table '$tableName'")
	}

	fun columnInt(columnName: String): DerivedColumn<IntValueType> {
		return DerivedColumn(this, columnName)
	}

	fun columnFloat(columnName: String): DerivedColumn<FloatValueType> {
		return DerivedColumn(this, columnName)
	}

	fun columnString(columnName: String): DerivedColumn<StringValueType> {
		return DerivedColumn(this, columnName)
	}
	fun columnBool(columnName: String): DerivedColumn<BooleanValueType> {
		return DerivedColumn(this, columnName)
	}


	fun <T: AnyValueType> column(columnExpr: ColumnExpr<T>): DerivedColumn<T> {
		return DerivedColumn(this, columnExpr.getColumnName())
	}

}


