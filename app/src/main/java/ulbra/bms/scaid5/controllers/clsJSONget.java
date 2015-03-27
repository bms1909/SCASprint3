package ulbra.bms.scaid5.controllers;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Criador por Bruno em 17/03/2015.
 */
public class clsJSONget extends AsyncTask<String, Void, JSONArray> {

    public static boolean temInternet() {
        Runtime runtime = Runtime.getRuntime();
        try {
            //8.8.8.8 refere-se ao servidor de DNS do Google
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        JSONArray retorno = null;

        StringBuilder builder = new StringBuilder();
        for (String s : params) {
            builder.append(s);
        }

        String url = builder.toString();
        try {
            ByteArrayOutputStream intermediario = new ByteArrayOutputStream();  //intermediario para transformar o url em stream
            URL link = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) link.openConnection();

            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // geInputStream faz o download
            //conversao de inputstream para string
            IOUtils.copy(conn.getInputStream(), intermediario);
            String conteudo = intermediario.toString();
            if (conteudo.startsWith("{")) {
                builder = new StringBuilder();
                builder.append("[");
                builder.append(conteudo);
                builder.append("]");
                conteudo = builder.toString();
            }
            try {
                retorno = new JSONArray(conteudo); //converte os dados recebidos de uma string para um objeto manipulável
            } catch (JSONException e) {
                Log.d("pau no json", e.getMessage());
            }

        } catch (IOException o) {
            Log.d("defeito feio ao carrega", o.getMessage());
        }
        return retorno;
    }
}
