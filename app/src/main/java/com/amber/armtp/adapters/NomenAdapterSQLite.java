package com.amber.armtp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.amber.armtp.R;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.interfaces.TBUpdate;
import com.amber.armtp.ui.SettingFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NomenAdapterSQLite extends SimpleCursorAdapter implements TBUpdate {

    public static int CurVisiblePosition = 0;
    public int beginPos;
    public int endPos;
    public float Discount = 0;
    public boolean isSales;
    public boolean isDiscount;

    public Context context;

    public AdapterView.OnLongClickListener PhotoLongClick;

    private DBHelper dbHelper;
    private android.support.v7.widget.Toolbar toolbar;

    public NomenAdapterSQLite(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
    }

    public void setDbHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Cursor cursor = getCursor();
        CurVisiblePosition = cursor.getCount();
        int resID;

        String kod5 = cursor.getString(cursor.getColumnIndex("KOD5"));

        TextView tvDescr = view.findViewById(R.id.ColNomDescr);
        TextView tvPrice = view.findViewById(R.id.ColNomPrice);
        TextView tvPosition = view.findViewById(R.id.ColNomPosition);
        TextView tvMP = view.findViewById(R.id.ColNomMP);
        TextView tvGofra = view.findViewById(R.id.ColNomVkorob);
        TextView tvOst = view.findViewById(R.id.ColNomOst);
        TextView tvCod = view.findViewById(R.id.ColNomCod);
        TextView tvPhoto = view.findViewById(R.id.ColNomPhoto);
        TextView tvZakaz = view.findViewById(R.id.ColNomZakaz);

        TextView[] tvListForChange = new TextView[]{
                tvDescr,
                tvPrice,
                tvPosition,
                tvMP,
                tvGofra,
                tvOst,
                tvCod,
        };

        Button btPlus = view.findViewById(R.id.btPlus);
        Button btMinus = view.findViewById(R.id.btMinus);

        tvDescr.setTextSize(SettingFragment.nomenDescriptionFontSize);
        if (tvPhoto != null) {
            tvPhoto.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));
            tvPhoto.setOnLongClickListener(PhotoLongClick);
        }

        if (tvPrice != null && !tvPrice.getText().toString().equals("null")) {
            tvPrice.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(tvPrice.getText().toString())));
        }

        btPlus.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));
        btMinus.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));

        if (DBHelper.pricesMap.containsKey(kod5) && isSales) {
//                System.out.println(kod5 + " " + DBHelper.pricesMap.get(kod5));
            tvPrice.setText(String.format(Locale.ROOT, "%.2f", DBHelper.pricesMap.get(kod5)));
        }

        if (isDiscount && tvPhoto != null) {
            tvPrice.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(tvPrice.getText().toString()) * (1 - Discount / 100f)));
        }

        if (tvPhoto != null && (cursor.getString(cursor.getColumnIndex("FOTO")) != null)) {
            if (cursor.getInt(cursor.getColumnIndex("PD")) == 1) {
                resID = context.getResources().getIdentifier("photo_downloaded", "drawable", context.getPackageName());
            } else {
                resID = context.getResources().getIdentifier("photo_no_downloaded", "drawable", context.getPackageName());
            }
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(" ").append(" ");
            builder.setSpan(new ImageSpan(context, resID), builder.length() - 1, builder.length(), 0);
            builder.append(" ");
            tvPhoto.setText(builder);
        }

        long daysSubtraction = _countDaySubtraction(cursor);

        int backgroundColor;
        if ((beginPos != 0 || endPos != 0) && position >= beginPos - 1 && position <= endPos - 1) {
            backgroundColor = context.getResources().getColor(R.color.multiSelectedNomen);
        } else if (!tvZakaz.getText().toString().equals("0")) {
            backgroundColor = context.getResources().getColor(R.color.selectedNomen);
        } else {
            if (position % 2 != 0) {
                backgroundColor = context.getResources().getColor(R.color.gridViewFirstColor);
            } else {
                backgroundColor = context.getResources().getColor(R.color.gridViewSecondColor);
            }
        }
        view.setBackgroundColor(backgroundColor);

        int color = context.getResources().getColor(R.color.black);
        if (daysSubtraction <= 2) {
            color = context.getResources().getColor(R.color.postDataColorRed);
        } else if (daysSubtraction <= 4) {
            color = context.getResources().getColor(R.color.postDataColorGreen);
        }
        _setTextColorOnTextView(tvListForChange, color);

        int style = Typeface.NORMAL;
        String action_list_temp = cursor.getString(cursor.getColumnIndex("ACT_LIST"));
        String[] action_list = action_list_temp == null ? new String[]{} : action_list_temp.split(",");

        if (action_list.length > 0) {
            style = Typeface.BOLD_ITALIC;
        }
        _setTypeFaceOnTextView(tvListForChange, style);

        tvPosition.setText(String.valueOf(position + 1));

        return view;
    }

    private void _setTextColorOnTextView(TextView[] tvList, int color) {
        for (TextView tv : tvList) {
            tv.setTextColor(color);
        }
    }

    private void _setTypeFaceOnTextView(TextView[] tvList, int style) {
        for (TextView tv : tvList) {
            tv.setTypeface(Typeface.defaultFromStyle(style));
        }
    }

    private long _countDaySubtraction(Cursor cursor) {
        long CurrentTime = Calendar.getInstance().getTimeInMillis();
        String PostData = cursor.getString(cursor.getColumnIndex("POSTDATA"));

        String[] data = PostData.split("\\.");
        data[2] = "20" + data[2];

        PostData = data[0] + "." + data[1] + "." + data[2];

        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        long PostDataTime = 0;
        try {
            PostDataTime = format.parse(PostData).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return (long) Math.abs((PostDataTime - CurrentTime) / (1000d * 60 * 60 * 24));
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
//        setContrAndSum(GlobalVars.this);
        setContrAndSumValue(dbHelper, toolbar, isSales);
    }
}
