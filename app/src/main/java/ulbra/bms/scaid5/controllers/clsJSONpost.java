package ulbra.bms.scaid5.controllers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsBdLocal;

/**
 * Criador por Bruno em 17/03/2015.
 */
public class clsJSONpost extends AsyncTask<String, Void, Boolean> {
    private Context contexto;

    public clsJSONpost(Context ctx) {
        this.contexto = ctx;
    }

    public static void executaPendentes(Context contexto) {
        clsJSONpost executa = new clsJSONpost(contexto);

        //necessário pois o doInBackground só recebe um tipo de parâmetro
        String vazia = null;
        executa.execute(vazia);
    }

    public void executaPost(String URL) {
        this.execute(URL);
    }


    @Override
    protected Boolean doInBackground(String... params) {
        StringBuilder builder = new StringBuilder();
        for (String s : params) {
            builder.append(s);
        }
        String comando = builder.toString();
        clsBdLocal temp = new clsBdLocal(contexto);
        if (!comando.equals("null"))
            temp.insereTemp(comando);

        for (String url : temp.buscaTemp()) {
            try {

                URL link = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) link.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);

                // faz o download
                conn.connect();

                //conversao de inputstream para string
                ByteArrayOutputStream intermediario = new ByteArrayOutputStream();  //intermediario para transformar o url em stream
                IOUtils.copy(conn.getInputStream(), intermediario);
                if (intermediario.toString().equals("true"))
                    temp.removeTemp(url);
            } catch (Exception e) {
                Log.d("pau no POSTsem internet", e.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        //obtém resultado do asynctask
    }
}

