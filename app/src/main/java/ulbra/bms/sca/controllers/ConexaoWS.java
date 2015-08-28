package ulbra.bms.sca.controllers;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ulbra.bms.sca.interfaces.downloadFeitoListener;
import ulbra.bms.sca.models.clsBdLocal;

/**
 * a
 * Created by bms_c on 27/08/2015.
 */
public class ConexaoWS {

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

    public static void executaPendentes(final Context context) {
        clsBdLocal temp = new clsBdLocal(context);
        if (temp.buscaTemp().size() > 0) {
            final String url = temp.buscaTemp().get(0);
            temp.desconectaBanco();

            executaComando(new downloadFeitoListener() {
                @Override
                public void downloadConcluido(JSONArray result) {
                    clsBdLocal tempLocal = new clsBdLocal(context);
                    try {
                        JSONObject tempjson = result.getJSONObject(0);
                        if (tempjson.getBoolean("resposta")) {
                            tempLocal.removeTemp(url);
                        }
                        if (tempLocal.buscaTemp().size() > 0)
                            executaPendentes(context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        tempLocal.desconectaBanco();
                    }
                }
            }, context, url, CustomJsonArrayRequest.Method.POST);
        } else {
            temp.desconectaBanco();
        }
    }

    public static void executaGet(final downloadFeitoListener ouvinte, Context context, String URL) {
        executaComando(ouvinte, context, URL, CustomJsonArrayRequest.Method.GET);
    }

    public static void executaPost(Context contexto, String URL) {
        clsBdLocal temp = new clsBdLocal(contexto);
        temp.insereTemp(URL);
        executaPendentes(contexto);
    }

    private static void executaComando(final downloadFeitoListener ouvinte, Context context, String URL, int metodo) {
        RequestQueue rq = Volley.newRequestQueue(context);
        Map<String, String> params = new HashMap<String, String>();

        // editar recebimento no webservice params.put("nome", "1");

        CustomJsonArrayRequest request = new CustomJsonArrayRequest(metodo, URL,
                params,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("Script", "SUCCESS: " + response);
                        ouvinte.downloadConcluido(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Script", "deupau: " + error.getMessage());
                        ouvinte.downloadConcluido(null);
                    }
                });
        request.setTag("tag");
        rq.add(request);
    }
}
