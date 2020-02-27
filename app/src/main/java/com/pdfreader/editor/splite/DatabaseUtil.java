package com.pdfreader.editor.splite;

import android.content.Context;


import com.pdfreader.editor.model.Favorite;
import com.pdfreader.editor.model.PDFInfo;
import com.pdfreader.editor.model.Reading;

import java.util.List;

public class DatabaseUtil {

    private static final int DB_VERSION = 2;
    private static final String DN_NAME = "PDFREADER";
    private SQLiteQueryHelper dbHelper;
    private static DatabaseUtil instant;
    private Context context;

    private DatabaseUtil() {

    }

    public static DatabaseUtil getInstant(Context ctx) {
        if (instant == null) {
            synchronized (DatabaseUtil.class) {
                if (instant == null) {
                    instant = new DatabaseUtil();
                    instant.context = ctx;
                    instant.dbHelper = new SQLiteQueryHelper(ctx, DN_NAME,
                            DB_VERSION, PDFInfo.class, Favorite.class, Reading.class);
                    instant.dbHelper.init();
                }
            }
        }
        return instant;
    }


    public void insertPDFInfo(PDFInfo pdfInfo) {
        try {
            if (dbHelper != null) {
                dbHelper.insert(pdfInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertFavorite(Favorite favorite) {
        try {
            if (dbHelper != null) {
                dbHelper.insert(favorite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertPageReading(Reading reading) {
        try {
            if (dbHelper != null) {
                dbHelper.insert(reading);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upDateReading(Reading reading, String whereClause) {
        try {
            if (dbHelper != null) {
                dbHelper.updateFix(reading, whereClause);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<PDFInfo> getList() {
        if (dbHelper == null) return null;
        List<PDFInfo> list = dbHelper.get(PDFInfo.class, null);
        return list;
    }

    public void deleteFavorite(Favorite favorite, String whereClause) {
        try {
            if (dbHelper != null) {
                dbHelper.delete(favorite, whereClause,null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteReading(Reading reading, String whereClause) {
        try {
            if (dbHelper != null) {
                dbHelper.delete(reading, whereClause,null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteHistory(PDFInfo pdfInfo, String whereClause) {
        try {
            if (dbHelper != null) {
                dbHelper.delete(pdfInfo, whereClause,null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upDateFavorite(Favorite favorite, String whereClause) {
        try {
            if (dbHelper != null) {
                dbHelper.updateFix(favorite, whereClause);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upDatePdfInfo(PDFInfo pdfInfo, String whereClause) {
        try {
            if (dbHelper != null) {
                dbHelper.updateFix(pdfInfo, whereClause);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Favorite> getListFavorite() {
        if (dbHelper == null) return null;
        List<Favorite> list = dbHelper.get(Favorite.class, null);
        return list;
    }

    public List<Reading> getReading() {
        if (dbHelper == null) return null;
        List<Reading> list = dbHelper.get(Reading.class, null);
        return list;
    }

    public void deleteAllFile(Favorite favorite){
        try {
            if (dbHelper != null) {
                dbHelper.deleteAllFile(favorite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
