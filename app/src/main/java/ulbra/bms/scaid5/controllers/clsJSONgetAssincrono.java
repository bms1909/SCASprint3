package ulbra.bms.scaid5.controllers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
    private final Context contexto;
    private boolean deuErroInternet = false;

    public clsJSONgetAssincrono(Context ctx)
    {
        this.contexto=ctx;
    }

    public void addListener(downloadFeitoListener listener) {
        ouvinte = listener;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        //utilizado pois o webservice pode retornar null em uma consulta sem resultados
        if(deuErroInternet)
            Toast.makeText(contexto,"Erro de internet, alertas e estabelecimentos podem estar desatualizados",Toast.LENGTH_LONG).show();
        else
            ouvinte.downloadConcluido(result);
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
            //se retorno for vazio
            if ((conteudo.equals("[]")))
            {
                return null;
            }
            //se nao comeca com [, eh um JSON object, e para garantir o reuso, eh transformado em jsonArray
            else if (!conteudo.startsWith("[")) {
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
            retorno = new JSONArray(conteudo); //converte os dados recebidos de uma string para um objeto manipulável

        } catch (IOException o) {
            //previne crash se a mensagem for vazia
            if (o.getMessage()!=null) {
                Log.d("get ", o.getMessage());
            }
            deuErroInternet = true;
        } catch (JSONException o) {
            //previne crash se a mensagem for vazia
            if (o.getMessage()!=null) {
                Log.d("json ", o.getMessage());
            }
        }
        return retorno;
    }
}
