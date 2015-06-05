package ulbra.bms.sca.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import ulbra.bms.sca.R;
import ulbra.bms.sca.interfaces.downloadFeitoListener;

public class clsJSONget extends AsyncTask<String, Void, JSONArray> {

    private final Context contexto;
    private downloadFeitoListener ouvinte;
    private boolean deuErroInternet = false;

    public clsJSONget(Context ctx) {
        this.contexto = ctx;
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

    public void addListener(downloadFeitoListener listener) {
        ouvinte = listener;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        //utilizado pois o webservice pode retornar null em uma consulta sem resultados
        if (deuErroInternet) {
            AlertDialog.Builder dlgErro = new AlertDialog.Builder(contexto);

            dlgErro.setTitle(contexto.getString(R.string.erro_de_conexao));
            dlgErro.setMessage(contexto.getString(R.string.clsget_erro_sincronizar));
            dlgErro.setPositiveButton(contexto.getString(R.string.ok), null);
            dlgErro.setCancelable(true);
            dlgErro.show();
        }
        ouvinte.downloadConcluido(result);
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        JSONArray retorno = new JSONArray();

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
            if ((conteudo.equals("[]"))) {
                return retorno;
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
            if (o.getMessage() != null) {
                Log.d("get ", o.getMessage());
            }
            deuErroInternet = true;
            return null;
        } catch (JSONException o) {
            //previne crash se a mensagem for vazia
            if (o.getMessage() != null) {
                Log.d("json ", o.getMessage());
            }
            return null;
        }
        return retorno;
    }
}
