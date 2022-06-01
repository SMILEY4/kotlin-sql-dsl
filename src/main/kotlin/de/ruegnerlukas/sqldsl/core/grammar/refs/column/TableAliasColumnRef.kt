package de.ruegnerlukas.sqldsl.core.grammar.refs.column

import de.ruegnerlukas.sqldsl.core.grammar.refs.AliasTableRef
import de.ruegnerlukas.sqldsl.core.schema.Column
import de.ruegnerlukas.sqldsl.core.schema.Table

class TableAliasColumnRef<D, T : Table>(val ref: AliasTableRef<T>, val column: Column<D, T>) : ColumnRef<D, T>

operator fun <D, T : Table> AliasTableRef<T>.get(column: Column<D, T>) = TableAliasColumnRef(this, column)
