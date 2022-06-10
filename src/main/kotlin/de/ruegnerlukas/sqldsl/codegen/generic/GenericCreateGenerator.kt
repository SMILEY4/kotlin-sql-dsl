package de.ruegnerlukas.sqldsl.codegen.generic

import de.ruegnerlukas.sqldsl.codegen.CreateGenerator
import de.ruegnerlukas.sqldsl.codegen.GeneratorContext
import de.ruegnerlukas.sqldsl.dsl.grammar.column.AutoIncrementPseudoConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.Column
import de.ruegnerlukas.sqldsl.dsl.grammar.column.ColumnConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.ColumnType
import de.ruegnerlukas.sqldsl.dsl.grammar.column.DefaultBooleanValueConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.DefaultFloatValueConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.DefaultIntValueConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.DefaultStringValueConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.DefaultValueConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.ForeignKeyConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.NotNullConstraint
import de.ruegnerlukas.sqldsl.dsl.grammar.column.OnConflict
import de.ruegnerlukas.sqldsl.dsl.grammar.column.OnDelete
import de.ruegnerlukas.sqldsl.dsl.grammar.column.OnUpdate
import de.ruegnerlukas.sqldsl.dsl.grammar.column.PrimaryKeyConstraint
import de.ruegnerlukas.sqldsl.dsl.schema.Table
import de.ruegnerlukas.sqldsl.dsl.grammar.column.UniqueConstraint
import de.ruegnerlukas.sqldsl.codegen.tokens.CsvListToken
import de.ruegnerlukas.sqldsl.codegen.tokens.GroupToken
import de.ruegnerlukas.sqldsl.codegen.tokens.ListToken
import de.ruegnerlukas.sqldsl.codegen.tokens.NoOpToken
import de.ruegnerlukas.sqldsl.codegen.tokens.StringToken
import de.ruegnerlukas.sqldsl.codegen.tokens.Token

open class GenericCreateGenerator(private val genCtx: GeneratorContext) : CreateGenerator, GenericGeneratorBase<de.ruegnerlukas.sqldsl.dsl.grammar.create.CreateTableStatement>() {

	override fun buildToken(e: de.ruegnerlukas.sqldsl.dsl.grammar.create.CreateTableStatement): Token {
		return ListToken()
			.add("CREATE TABLE")
			.addIf(e.onlyIfNotExists, "IF NOT EXISTS")
			.add(e.table.tableName)
			.add(GroupToken(CsvListToken(e.table.getTableColumns().map { column(it) })))
			.add(tableConstraints(e.table))
	}

	protected fun column(column: Column<*>): Token {
		return ListToken()
			.add(column.getColumnName())
			.add(mapColumnType(column.getColumnType()))
			.add(constraints(column.getConstraints(), column))
	}

	protected fun mapColumnType(type: ColumnType): String {
		return when (type) {
			ColumnType.INT -> "INT"
			ColumnType.FLOAT -> "FLOAT"
			ColumnType.TEXT -> "TEXT"
			ColumnType.BOOLEAN -> "BOOLEAN"
		}
	}

	protected fun constraints(constraints: List<ColumnConstraint>, column: Column<*>): Token {
		return ListToken(constraints.map { constraint(it, column) })
	}

	protected fun constraint(c: ColumnConstraint, column: Column<*>): Token {
		return when (c) {
			is PrimaryKeyConstraint -> primaryKeyConstraint(c, column)
			is AutoIncrementPseudoConstraint -> autoIncrementConstraint(c, column)
			is NotNullConstraint -> notNullConstraint(c, column)
			is UniqueConstraint -> uniqueConstraint(c, column)
			is ForeignKeyConstraint -> foreignKeyConstraint(c, column)
			is DefaultValueConstraint<*> -> defaultValueConstraint(c, column)
			else -> throwUnknownType(c)
		}
	}

	protected fun primaryKeyConstraint(c: PrimaryKeyConstraint, column: Column<*>): Token {
		return ListToken()
			.add("PRIMARY KEY")
			.addIf(c.onConflict != OnConflict.ABORT, mapConflict(c.onConflict))
			.addIf(column.hasConstraint<AutoIncrementPseudoConstraint>(), "AUTOINCREMENT")
	}

	protected fun autoIncrementConstraint(c: AutoIncrementPseudoConstraint, column: Column<*>): Token {
		return NoOpToken()
	}

	protected fun notNullConstraint(c: NotNullConstraint, column: Column<*>): Token {
		return ListToken()
			.add("NOT NULL")
			.addIf(c.onConflict != OnConflict.ABORT, mapConflict(c.onConflict))
	}

	protected fun uniqueConstraint(c: UniqueConstraint, column: Column<*>): Token {
		return ListToken()
			.add("UNIQUE")
			.addIf(c.onConflict != OnConflict.ABORT, mapConflict(c.onConflict))
	}

	protected fun foreignKeyConstraint(c: ForeignKeyConstraint, column: Column<*>): Token {
		return ListToken()
			.add("REFERENCES")
			.add(c.table.tableName)
			.addIf(c.column != null, "(${c.column!!.getColumnName()})")
			.addIf(c.onDelete != OnDelete.NO_ACTION, mapOnDelete(c.onDelete))
			.addIf(c.onUpdate != OnUpdate.NO_ACTION, mapOnUpdate(c.onUpdate))
	}

	protected fun defaultValueConstraint(c: DefaultValueConstraint<*>, column: Column<*>): Token {
		return ListToken()
			.add("DEFAULT")
			.add(
				when (c) {
					is DefaultIntValueConstraint -> StringToken(c.getDefaultValue().toString())
					is DefaultFloatValueConstraint -> StringToken(c.getDefaultValue().toString())
					is DefaultStringValueConstraint -> StringToken("'${c.getDefaultValue()}'")
					is DefaultBooleanValueConstraint -> StringToken(if (c.getDefaultValue()) "TRUE" else "FALSE")
					else -> throwUnknownType(c)
				}
			)
	}

	protected fun tableConstraints(table: Table<*>): Token {
		val primaryKeys = table.getTableColumns().filter { it.hasConstraint<PrimaryKeyConstraint>() }
		if (primaryKeys.size > 1) {
			return ListToken()
				.add("PRIMARY KEY")
				.add(
					GroupToken(
						CsvListToken(
							primaryKeys.map { StringToken(it.getColumnName()) }
						)
					)
				)
				.add(mapConflict(primaryKeys[0].getConstraint<PrimaryKeyConstraint>()!!.onConflict))
		} else {
			return NoOpToken()
		}
	}

	protected fun mapConflict(conflict: OnConflict): String {
		return when (conflict) {
			OnConflict.ROLLBACK -> "ON CONFLICT ROLLBACK"
			OnConflict.ABORT -> "ON CONFLICT ABORT"
			OnConflict.FAIL -> "ON CONFLICT FAIL"
			OnConflict.IGNORE -> "ON CONFLICT IGNORE"
			OnConflict.REPLACE -> "ON CONFLICT REPLACE"
		}
	}

	protected fun mapOnDelete(onDelete: OnDelete): String {
		return when (onDelete) {
			OnDelete.NO_ACTION -> "ON DELETE NO ACTION"
			OnDelete.RESTRICT -> "ON DELETE RESTRICT"
			OnDelete.SET_NULL -> "ON DELETE SET NULL"
			OnDelete.SET_DEFAULT -> "ON DELETE SET DEFAULT"
			OnDelete.CASCADE -> "ON DELETE CASCADE"
		}
	}

	protected fun mapOnUpdate(onUpdate: OnUpdate): String {
		return when (onUpdate) {
			OnUpdate.NO_ACTION -> "ON UPDATE NO ACTION"
			OnUpdate.RESTRICT -> "ON UPDATE RESTRICT"
			OnUpdate.SET_NULL -> "ON UPDATE SET NULL"
			OnUpdate.SET_DEFAULT -> "ON UPDATE SET DEFAULT"
			OnUpdate.CASCADE -> "ON UPDATE CASCADE"
		}
	}


}