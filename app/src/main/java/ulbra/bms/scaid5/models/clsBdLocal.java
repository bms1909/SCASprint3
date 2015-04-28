package ulbra.bms.scaid5.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Criado por Bruno on 06/04/2015.
 */
public class clsBdLocal {
    private SQLiteDatabase db;

    public clsBdLocal(Context ctx) {
        clsBdLocalConstrutor auxBD = new clsBdLocalConstrutor(ctx);
        db = auxBD.getWritableDatabase();
    }

    public void insereTemp(String comando) {
        ContentValues valores = new ContentValues();
        valores.put("comando", comando);
        db.insert("temp", null, valores);
    }

    public void removeTemp(String comando) {
        // ContentValues valores = new ContentValues();
        //   valores.put("comando", comando);
        //  db.delete("temp","where comando = '"+comando+"'",null);
        db.execSQL("DELETE FROM temp where comando = '" + comando + "'");
    }

    public ArrayList<String> buscaTemp() {
        ArrayList<String> retorno = new ArrayList<>();
        String[] colunaConsulta = new String[]{"comando"};
        //nome tabela,colunas da consulta,where,where args,groupby,having,orderby

        Cursor cursor = db.query("temp", colunaConsulta, null /*"comando != null"*/, null, null, null, null);

        while (cursor.moveToNext()) {
            retorno.add(cursor.getString(0));
        }
        return retorno;
    }
}
