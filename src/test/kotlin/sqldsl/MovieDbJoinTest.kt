package sqldsl

import de.ruegnerlukas.sqldsl.builders.QueryBuilderEndStep
import de.ruegnerlukas.sqldsl.builders.SQL
import de.ruegnerlukas.sqldsl.builders.assign
import de.ruegnerlukas.sqldsl.builders.isEqual
import de.ruegnerlukas.sqldsl.builders.isNull
import de.ruegnerlukas.sqldsl.builders.join
import de.ruegnerlukas.sqldsl.generators.generic.GenericGeneratorContext
import de.ruegnerlukas.sqldsl.generators.generic.GenericQueryGenerator
import de.ruegnerlukas.sqldsl.grammar.table.DerivedTable
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


/**
 * https://www.w3resource.com/sql-exercises/movie-database-exercise/joins-exercises-on-movie-database.php
 */
class MovieDbJoinTest {

	private val generator = GenericQueryGenerator(GenericGeneratorContext())


	private fun assertQuery(query: QueryBuilderEndStep<*>, expected: String) {
		val strQuery = generator.buildString(query.build())
		println(strQuery)
		assertEquals(expected, strQuery)
	}


	/**
	 * SELECT rev_name
	 * FROM reviewer
	 * 		INNER JOIN rating USING(rev_id)
	 * WHERE rev_stars IS NULL;
	 */
	@Test
	fun query1() {
		val derived = DerivedTable("result")
		val query = SQL
			.select(derived.column(Reviewer.name))
			.from(
				Reviewer.join(Rating)
					.using(derived.column(Reviewer.id))
					.assign(derived)
			)
			.where(derived.column(Rating.stars).isNull())
		assertQuery(query, "SELECT result.rev_name FROM (reviewer JOIN rating USING result.rev_id) AS result WHERE (result.rev_stars) IS NULL"
		)
	}


	@Test
	fun queryTest() {
		val derived = DerivedTable("result")
		val query = SQL
			.selectAll()
			.from(
				Actor.join(MovieCast)
					.on(Actor.id.isEqual(MovieCast.actorId))
					.assign(derived)
			)
			.where(derived.column(Actor.gender).isEqual("F"))
		assertQuery(query, "SELECT * FROM (actor JOIN movie_cast ON ((actor.act_id) = (movie_cast.act_id))) AS result WHERE (result.act_gender) = ('F')")
	}

}