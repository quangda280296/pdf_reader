package com.pdfreader.editor.font;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.pdfreader.pdf.reader.editor.R;


public class Font_UTMAvo extends TextView {


 //   private static final String TYPE_FONT_DEFAULT = "fonts/Roboto-Regular.ttf";
    private int currentColor = Color.BLACK;
    //<<<<<<< .merge_file_sWK8O9
//    private String typeFont = TYPE_FONT_DEFAULT;
//=======
    private String typeFont = "fonts/rubik-bold.ttf";
//>>>>>>> .merge_file_wjQAKg

    public Font_UTMAvo(Context context) {
        super(context);
    }

    public Font_UTMAvo(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentColor = getCurrentTextColor();
        try {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CustomTextView, 0, 0);

            typeFont = a.getString(R.styleable.CustomTextView_fonts);

            setTypeface(FontCache.getTypeface(context,typeFont));
            a.recycle();
        } catch (Exception ex) {
            try {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/rubik-bold.ttf"));
            }catch (Exception ex1) {
                setTypeface(Typeface.DEFAULT);
            }
        }
    }

    public void setTypeFontByName(Context context, String fontName) {
        String fontFile= String.format("fonts/%s",fontName);
        setTypeFont(context,fontFile);
    }

    public void setTypeFont(Context context, String typeFont){
        this.typeFont = typeFont;
        setTypeface(FontCache.getTypeface(context,typeFont));
    }

    public String getTypeFont() {
        return typeFont;
    }
}
