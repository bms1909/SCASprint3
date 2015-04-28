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

import ulbra.bms.scaid5.interfaces.downloadFeitoListener;

public class clsJSONgetAssincrono extends AsyncTask<String, Void, JSONArray>{

    private downloadFeitoListener ouvinte;

    public void addListener(downloadFeitoListener listener) {
        ouvinte = listener;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
            ouvinte.downloadConcluido(result);
    }


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

            conn.setReadTimeout(30000 /* milliseconds */);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // geInputStream faz o download
            //conversao de inputstream para string
            IOUtils.copy(conn.getInputStream(), intermediario);
            String conteudo = intermediario.toString();
            if (!conteudo.startsWith("[")) {
                builder = new StringBuilder();
                builder.append("[");
                if (conteudo.startsWith("t") || conteudo.startsWith("f")) {
                    builder.append("{'resposta':");
                }
                builder.append(conteudo);
                if (conteudo.startsWith("t") || conteudo.startsWith("f")) {
                    builder.append("}");
                }
                builder.append("]");
                conteudo = builder.toString();
            }
            try {
                retorno = new JSONArray(conteudo); //converte os dados recebidos de uma string para um objeto manipulável
            } catch (JSONException e) {
                Log.d("pau no json", e.getMessage());
            }

        } catch (IOException o) {
            Log.d("get ", o.getMessage());
        }
        return retorno;
    }


}
