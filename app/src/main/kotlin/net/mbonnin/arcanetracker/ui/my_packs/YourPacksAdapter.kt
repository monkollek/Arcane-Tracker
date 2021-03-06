package net.mbonnin.arcanetracker.ui.my_packs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import net.mbonnin.arcanetracker.CardUtil
import net.mbonnin.arcanetracker.R
import net.hearthsim.hslog.parser.achievements.AchievementsParser
import net.mbonnin.arcanetracker.room.RDatabaseSingleton
import net.mbonnin.arcanetracker.room.RPack
import net.hearthsim.hsmodel.enum.CardId
import java.util.*

class YourPacksAdapter(val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val list = mutableListOf<Item>()

    val TYPE_PACKS = 0
    val TYPE_DUST = 2
    val TYPE_DUST_AVERAGE = 3
    val TYPE_PITY_COUNTER = 4
    val TYPE_PACK = 5

    var lastListSize = 0
    val lock = Object()
    val requestQueue = LinkedList<Any>()

    val publishSubject = PublishSubject.create<List<Item>>()

    class Sentinel

    init {
        val thread = Thread{
            worker()
        }

        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                synchronized(lock) {
                    requestQueue.addLast(Sentinel())
                    lock.notifyAll()
                }
                thread.join(3000)
            }
        })

        publishSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    list.addAll(it)
                    notifyDataSetChanged()
                }

        thread.start()

        requestMoreDataIfNeeded(0)
    }

    private fun requestMoreDataIfNeeded(position: Int) {
        var needed = false
        if (lastListSize == 0) {
            needed = true
        } else if (list.size > lastListSize && list.size - position < 5) {
            needed = true
        }

        if (needed) {
            lastListSize = list.size
            synchronized(lock) {
                requestQueue.addLast(Any())
                lock.notifyAll()
            }
        }
    }

    private fun worker() {
        //instertTestPacks()
        var list = mutableListOf<Item>()

        val stats = RDatabaseSingleton.instance.packDao().stats()

        list.add(PacksItem(stats.count))
        list.add(DustItem(stats.dust))
        val average = if (stats.count > 0) stats.dust / stats.count else 0
        list.add(DustAverageItem(average))

        publishSubject.onNext(list)

        val cursor = RDatabaseSingleton.instance.packDao().all()

        while (true) {
            synchronized(lock) {
                while (requestQueue.isEmpty()) {
                    lock.wait()
                }

                val request = requestQueue.removeFirst()
                if (request is Sentinel) {
                    cursor.close()
                    return
                }
            }

            val _cursorIndexOfId = cursor.getColumnIndexOrThrow("id")
            val _cursorIndexOfTimeMillis = cursor.getColumnIndexOrThrow("timeMillis")
            val _cursorIndexOfCardList = cursor.getColumnIndexOrThrow("cardList")
            val _cursorIndexOfDust = cursor.getColumnIndexOrThrow("dust")

            var i = 0
            list = mutableListOf()
            while (cursor.moveToNext() && i++ < 20) {
                val _tmpTimeMillis = cursor.getLong(_cursorIndexOfTimeMillis)
                val _tmpCardList = cursor.getString(_cursorIndexOfCardList)
                val _tmpId = cursor.getLong(_cursorIndexOfId)
                val _tmpDust = cursor.getInt(_cursorIndexOfDust)

                val rpack = RPack(_tmpTimeMillis, _tmpCardList, _tmpDust)
                rpack.id = _tmpId

                list.add(PackItem(rpack))
            }

            publishSubject.onNext(list)
        }
    }

    private fun instertTestPacks() {
        val cardList = listOf(
                AchievementsParser.CardGained(CardId.MILLHOUSE_MANASTORM, true),
                AchievementsParser.CardGained(CardId.RAGNAROS_THE_FIRELORD, false),
                AchievementsParser.CardGained(CardId.MURLOC_WARLEADER, false),
                AchievementsParser.CardGained(CardId.IRONBEAK_OWL, true),
                AchievementsParser.CardGained(CardId.BRING_IT_ON, false)
        )

        for (i in 0..100) {
            val dust = cardList.sumBy {
                val card = CardUtil.getCard(it.id)
                CardUtil.getDust(card.rarity, it.golden)
            }
            val rPack = RPack(cardList = cardList.map { it.toString() }.joinToString(","), dust = dust)
            RDatabaseSingleton.instance.packDao().insert(rPack)
        }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val item = list.get(position)

            when (item) {
                is PacksItem -> return 2
                is DustItem -> return 2
                is DustAverageItem -> return 2
                is PityCounterItem -> return 3
                is PackItem -> return 6
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = list.get(position)

        when (item) {
            is PacksItem -> return TYPE_PACKS
            is DustItem -> return TYPE_DUST
            is DustAverageItem -> return TYPE_DUST_AVERAGE
            is PityCounterItem -> return TYPE_PITY_COUNTER
            is PackItem -> return TYPE_PACK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_PACKS,
            TYPE_DUST,
            TYPE_DUST_AVERAGE,
            TYPE_PITY_COUNTER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_packs_header, parent, false)
                return PacksHeaderViewHolder(view)
            }
            TYPE_PACK -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pack, parent, false)
                return PackViewHolder(view)
            }
            else -> {
                throw Exception()
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list.get(position)

        requestMoreDataIfNeeded(position)

        when (item) {
            is PacksItem,
            is DustItem,
            is DustAverageItem,
            is PityCounterItem -> (holder as PacksHeaderViewHolder).bind(item)
            is PackItem -> (holder as PackViewHolder).bind(item.rpack)
        }
    }
}

