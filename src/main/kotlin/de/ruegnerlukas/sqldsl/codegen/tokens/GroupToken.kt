package de.ruegnerlukas.sqldsl.codegen.tokens

class GroupToken(private val token: Token) : Token() {
	override fun buildString(): String {
		return "(${token.buildString()})"
	}
}