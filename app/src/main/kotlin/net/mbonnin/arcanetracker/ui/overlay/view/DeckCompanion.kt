package net.mbonnin.arcanetracker.ui.overlay.view

import android.graphics.Color
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mbonnin.arcanetracker.CardUtil
import net.mbonnin.arcanetracker.R
import net.mbonnin.arcanetracker.Utils
import net.mbonnin.arcanetracker.ViewManager
import net.hearthsim.hslog.util.getClassIndex
import net.hearthsim.hslog.util.getPlayerClass
import net.hearthsim.hslog.parser.decks.Deck
import net.hearthsim.hsmodel.Card
import timber.log.Timber

/**
 * Created by martin on 10/14/16.
 */

open class DeckCompanion(v: View) {
    internal val settings: ImageButton
    internal val winLoss: TextView
    internal val nodeck: View
    var deckName: TextView

    protected val recyclerView: RecyclerView
    protected val mViewManager: ViewManager

    private val mParams: ViewManager.Params
    private val mRecyclerViewParams: ViewManager.Params

    fun checkClassIndex(deck: Deck) {
    }

    open var deck: Deck? = null
        set(value) {
            field = value

            if (value == null) {
                return
            }

            checkClassIndex(value)

            background.setImageDrawable(Utils.getDrawableForClassIndex(value.classIndex))
            deckName.text = value.name

            nodeck.visibility = View.GONE
        }
    private val background: ImageView


    init {
        mViewManager = ViewManager.get()

        Timber.d("screen: " + mViewManager.width + "x" + mViewManager.height)

        val w = (0.33 * 0.5 * mViewManager.width.toDouble()).toInt()
        val h = mViewManager.height

        settings = v.findViewById(R.id.edit)
        winLoss = v.findViewById(R.id.winLoss)
        deckName = v.findViewById(R.id.deckName)
        background = v.findViewById(R.id.background)
        recyclerView = v.findViewById(R.id.recyclerView)
        nodeck = v.findViewById(R.id.nodeck)

        mParams = ViewManager.Params()
        mParams.x = 0
        mParams.y = 0
        mParams.w = w
        mParams.h = h

        mRecyclerViewParams = ViewManager.Params()
        mRecyclerViewParams.w = w
        mRecyclerViewParams.h = mViewManager.height - h

        recyclerView.setBackgroundColor(Color.BLACK)
        recyclerView.layoutManager = LinearLayoutManager(v.context)
    }
}

