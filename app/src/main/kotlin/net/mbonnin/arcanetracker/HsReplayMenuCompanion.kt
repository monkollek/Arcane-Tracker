package net.mbonnin.arcanetracker

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.View.GONE
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.hsreplay_menu_view.*
import net.mbonnin.arcanetracker.ui.main.LoginCompanion
import net.mbonnin.arcanetracker.ui.main.MainActivity
import net.mbonnin.arcanetracker.ui.overlay.Overlay

class HsReplayMenuCompanion(override val containerView: View): LayoutContainer {
    init {
        val account = ArcaneTrackerApplication.get().hsReplay.account()

        if (account != null) {
            battleTag.setText(account.username)
            battleTag.setOnClickListener {
                ViewManager.get().removeView(containerView)
                Utils.openLink("https://hsreplay.net/account/?utm_source=arcanetracker&utm_medium=client")
            }
            signout.setOnClickListener {
                Overlay.hide()

                ArcaneTrackerApplication.get().hsReplay.logout()

                val intent = Intent()
                intent.setClass(containerView.context, MainActivity::class.java)
                containerView.context.startActivity(intent)
            }

            if (account.is_premium == true) {
                battleTag.setTextColor(Color.parseColor("#FFB00D"))
                premium.visibility = GONE
            } else {
                premium.setOnClickListener {
                    ViewManager.get().removeView(containerView)
                    Utils.openLink("https://hsreplay.net/premium/?utm_source=arcanetracker&utm_medium=client")
                }
            }
        } else {
            premium.visibility = View.GONE
            signout.visibility = View.GONE
            myReplays.visibility = View.GONE

            battleTag.setText(containerView.context.getText(R.string.signIn))
            battleTag.setOnClickListener {
                ViewManager.get().removeView(containerView)
                LoginCompanion.openHsReplayOauth()
            }
        }

        myReplays.setOnClickListener {
            ViewManager.get().removeView(containerView)

            Utils.openLink("https://hsreplay.net/games/mine/?utm_source=arcanetracker&utm_medium=client")
        }

        meta.setOnClickListener {
            ViewManager.get().removeView(containerView)

            Utils.openLink("https://hsreplay.net/meta/?utm_source=arcanetracker&utm_medium=client")
        }

        exploreDecks.setOnClickListener {
            ViewManager.get().removeView(containerView)

            Utils.openLink("https://hsreplay.net/decks/?utm_source=arcanetracker&utm_medium=client")
        }

    }

    fun oauthSuccess(view: View) {
        ViewManager.get().removeView(view)
    }

    fun oauthCancel(view: View) {
        ViewManager.get().removeView(view)
    }
}
