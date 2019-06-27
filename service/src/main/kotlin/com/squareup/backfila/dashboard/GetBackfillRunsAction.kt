package com.squareup.backfila.dashboard

import com.squareup.backfila.service.BackfilaDb
import com.squareup.backfila.service.BackfillRunQuery
import com.squareup.backfila.service.BackfillState
import com.squareup.backfila.service.DbBackfillRun
import com.squareup.backfila.service.ServiceQuery
import misk.exceptions.BadRequestException
import misk.hibernate.Query
import misk.hibernate.Transacter
import misk.hibernate.newQuery
import misk.logging.getLogger
import misk.security.authz.Authenticated
import misk.web.PathParam
import misk.web.Post
import misk.web.RequestBody
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes
import javax.inject.Inject

data class UiBackfillRun(
  val id: String,
  val name: String
)

data class GetBackfillRunsRequest(val pagination_token: String? = null)

data class GetBackfillRunsResponse(
  val running_backfills: List<UiBackfillRun>,
  val paused_backfills: List<UiBackfillRun>
)

class GetBackfillRunsAction @Inject constructor(
  @BackfilaDb private val transacter: Transacter,
  private val queryFactory: Query.Factory
) : WebAction {
  @Post("/services/{service}/backfill-runs")
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  @Authenticated
  fun backfillRuns(
    @PathParam service: String,
    @RequestBody request: GetBackfillRunsRequest
  ): GetBackfillRunsResponse {
    return transacter.transaction { session ->
      val dbService = queryFactory.newQuery<ServiceQuery>()
          .registryName(service)
          .uniqueResult(session) ?: throw BadRequestException("`$service` doesn't exist")
      // TODO pagination, filtering
      val runningBackfills = queryFactory.newQuery<BackfillRunQuery>()
          .serviceId(dbService.id)
          .state(BackfillState.RUNNING)
          .list(session)
          .map(this::dbToUi)
      val pausedBackfills = queryFactory.newQuery<BackfillRunQuery>()
          .serviceId(dbService.id)
          .stateNot(BackfillState.RUNNING)
          .list(session)
          .map(this::dbToUi)

      GetBackfillRunsResponse(runningBackfills, pausedBackfills)
    }
  }

  private fun dbToUi(run: DbBackfillRun): UiBackfillRun {
    return UiBackfillRun(
        run.id.toString(),
        run.registered_backfill.name
    )
  }

  companion object {
    private val logger = getLogger<GetBackfillRunsAction>()
  }
}