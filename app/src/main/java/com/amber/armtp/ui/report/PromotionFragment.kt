package com.amber.armtp.ui.report

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.amber.armtp.Config
import com.amber.armtp.R
import com.amber.armtp.annotations.PGShowing
import com.amber.armtp.dbHelpers.DBHelper
import java.lang.String.format
import java.util.*

class PromotionFragment : Fragment() {

    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_promotion, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dbHelper = DBHelper(activity)
        val tvPreShow: TextView = activity!!.findViewById(R.id.tvPreShow)
        tvPreShow.setOnClickListener(View.OnClickListener {
            if (!dbHelper.isTableExisted("ACTION")) {
                Config.sout("Таблица ACTION не существует, обновите базу данных")
                return@OnClickListener
            }

            activity!!.findViewById<RelativeLayout>(R.id.layoutWithActionTable).visibility =
                View.VISIBLE
            it.visibility = View.GONE

            showTable()
        })
    }

    private fun showTable() {
        activity!!.runOnUiThread @PGShowing {
            val settings = activity!!.getSharedPreferences("apk_version", 0)
            val tradeRepresentativeID = settings.getString("ReportTPId", "") as String

            val gridView: GridView = activity!!.findViewById(R.id.actionGridView)
            val adapter = ActionAdapter(
                activity!!,
                R.layout.action_result_layout,
                getActionCursor(tradeRepresentativeID),
                arrayOf(
                    "ACTION", "DATAN", "DATAK", "VAL", "PLN"
                ),
                intArrayOf(
                    R.id.actionDesc,
                    R.id.actionDateStart,
                    R.id.actionDateEnd,
                    R.id.ActionFactValue,
                    R.id.ActionPlanValue
                ),
                0
            )
            gridView.adapter = adapter
        }
    }

    private fun getActionCursor(torgID: String): Cursor {
        return dbHelper.readableDatabase.rawQuery(
            "SELECT ROWID as _id, [ACTION], DATAN, DATAK, PLN, ISKOL, CASE WHEN ISKOL=1 THEN KOL ELSE SUMMA END AS 'VAL' FROM [ACTION] WHERE TORG_PRED=?",
            arrayOf(torgID)
        )
    }

    class ActionAdapter(
        val context: Context,
        layout: Int,
        c: Cursor?,
        from: Array<out String>?,
        private val to: IntArray?,
        flags: Int
    ) : SimpleCursorAdapter(context, layout, c, from, to, flags) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent)

            val tvPercent: TextView = view.findViewById(R.id.ActionPercent)
            val tvFact: TextView = view.findViewById(R.id.ActionFactValue)
            val tvPlan: TextView = view.findViewById(R.id.ActionPlanValue)

            val percent = cursor.getFloat(cursor.getColumnIndex("VAL")) / cursor.getFloat(
                cursor.getColumnIndex("PLN")
            ) * 100
            tvPercent.text = format(Locale.ROOT, "%.1f", percent)

            tvFact.text = if (cursor.getInt(cursor.getColumnIndex("ISKOL")) == 0) {
                format(Locale.ROOT, "%.2f", cursor.getFloat(cursor.getColumnIndex("VAL"))) + " руб"
            } else {
                tvFact.text.toString() + " шт"
            }

            tvPlan.text = if (cursor.getInt(cursor.getColumnIndex("ISKOL")) == 0) {
                tvPlan.text.toString() + " руб"
            } else {
                tvPlan.text.toString() + " шт"
            }

            val backgroundColor: Int = if (position % 2 != 0) {
                ContextCompat.getColor(context, R.color.gridViewFirstColor)
            } else {
                ContextCompat.getColor(context, R.color.gridViewSecondColor)
            }
            view.setBackgroundColor(backgroundColor)

            if (percent >= 100) {
                for (i in to!!) {
                    view.findViewById<TextView>(i)
                        .setTextColor(ContextCompat.getColor(context, R.color.postDataColorGreen))
                }
                tvPercent.setTextColor(ContextCompat.getColor(context, R.color.postDataColorGreen))
            }

            return view
        }
    }
}