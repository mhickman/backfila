package app.cash.backfila.client.misk.hibernate

import app.cash.backfila.client.misk.Backfill
import app.cash.backfila.client.misk.BackfillConfig
import app.cash.backfila.client.misk.ClientMiskService
import app.cash.backfila.client.misk.DbMenu
import app.cash.backfila.client.misk.MenuQuery
import app.cash.backfila.client.misk.UnshardedPartitionProvider
import javax.inject.Inject
import misk.hibernate.Id
import misk.hibernate.Query
import misk.hibernate.Transacter

class SinglePartitionHibernateTestBackfill @Inject constructor(
  @ClientMiskService private val transacter: Transacter,
  private val queryFactory: Query.Factory
) : Backfill<DbMenu, Id<DbMenu>, SandwichParameters>() {
  val idsRanDry = mutableListOf<Id<DbMenu>>()
  val idsRanWet = mutableListOf<Id<DbMenu>>()
  val parametersLog = mutableListOf<SandwichParameters>()

  override fun backfillCriteria(config: BackfillConfig<SandwichParameters>): Query<DbMenu> {
    return queryFactory.newQuery(MenuQuery::class).name(config.parameters.type)
  }

  override fun runBatch(pkeys: List<Id<DbMenu>>, config: BackfillConfig<SandwichParameters>) {
    parametersLog.add(config.parameters)

    if (config.dryRun) {
      idsRanDry.addAll(pkeys)
    } else {
      idsRanWet.addAll(pkeys)
    }
  }

  override fun partitionProvider() = UnshardedPartitionProvider(transacter)
}
data class SandwichParameters(
  // TODO add description fields
  val type: String = "chicken" // "The type of sandwich to backfill. e.g. chicken, beef"
)
