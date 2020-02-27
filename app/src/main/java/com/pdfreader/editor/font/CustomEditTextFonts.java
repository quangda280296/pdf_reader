package com.pdfreader.editor.font;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import com.pdfreader.pdf.reader.editor.R;


public class CustomEditTextFonts extends AppCompatEditText {

    private int currentColor = Color.BLACK;
    public CustomEditTextFonts(Context context) {
        super(context);
    }

    private String typeFont = "Calibre-Medium.ttf";

    public CustomEditTextFonts(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentColor = getCurrentTextColor();
        try {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CustomTextView, 0, 0);

            typeFont = a.getString(R.styleable.CustomTextView_fonts);

            setTypeface(Typeface.createFromAsset(context.getAssets(), typeFont));
            a.recycle();
        } catch (Exception ex) {
            try {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "Calibre-Medium.ttf"));

            }catch (Exception ex1) {
                setTypeface(Typeface.DEFAULT);
            }
        }
    }
}
