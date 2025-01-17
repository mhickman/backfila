package app.cash.backfila.ui

import app.cash.backfila.ui.actions.ServiceAutocompleteAction
import app.cash.backfila.ui.pages.ServiceIndexAction
import app.cash.backfila.ui.pages.ServiceShowAction
import misk.inject.KAbstractModule
import misk.web.WebActionModule

class UiModule : KAbstractModule() {
  override fun configure() {
    // Pages
    install(WebActionModule.create<ServiceShowAction>())
    install(WebActionModule.create<ServiceIndexAction>())

    // Other
    install(WebActionModule.create<ServiceAutocompleteAction>())
  }
}
