package ulbra.bms.sca.models;

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
    //chamado quando o objeto vai ser destruido, neste caso, fechando a conexao com o BD local
    @Override
    public void finalize()
    {
        desconectaBanco();
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    //fecha a conexao com o BD local
    public void desconectaBanco() {
        db.close();
    }

    public void insereTemp(String comando) {
        ContentValues valores = new ContentValues();
        valores.put("comando", comando);
        db.insert("postcache", null, valores);
    }

    public void removeTemp(String comando) {
        // ContentValues valores = new ContentValues();
        //   valores.put("comando", comando);
        //  db.delete("temp","where comando = '"+comando+"'",null);
        db.execSQL("DELETE FROM postcache where comando = '" + comando + "'");
    }

    public ArrayList<String> buscaTemp() {
        ArrayList<String> retorno = new ArrayList<>();
        String[] colunaConsulta = new String[]{"comando"};
        //nome tabela,colunas da consulta,where,where args,groupby,having,orderby

        Cursor cursor = db.query("postcache", colunaConsulta, null /*"comando != null"*/, null, null, null, null);
        while (cursor.moveToNext()) {
            retorno.add(cursor.getString(0));
        }
        cursor.close();
        return retorno;

    }
}
