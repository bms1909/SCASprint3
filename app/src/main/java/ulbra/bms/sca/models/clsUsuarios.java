package ulbra.bms.sca.models;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import ulbra.bms.sca.controllers.clsJSONget;
import ulbra.bms.sca.interfaces.booleanRetornadoListener;
import ulbra.bms.sca.interfaces.downloadFeitoListener;
import ulbra.bms.sca.interfaces.usuarioCarregadoListener;

/**
 * Criador por Bruno em 16/03/2015.
 */
public class clsUsuarios extends AsyncTask<Void,Void,String> {
    public int idUsuario;
    public String nomeUsuario;
    public String emailUsuario;
    public String senhaUsuario;
    private usuarioCarregadoListener ouvinte;
    private String comando;

    public clsUsuarios(String nome, String email, String senha) {
        this.nomeUsuario = nome;
        this.emailUsuario = email;
        this.senhaUsuario = senha;
    }

    private clsUsuarios(int id, String nome, String email, String senha) {
        this.idUsuario = id;
        this.nomeUsuario = nome;
        this.emailUsuario = email;
        this.senhaUsuario = senha;
    }

    public clsUsuarios() {
        super();
    }

    public static void recuperaUsuario(final booleanRetornadoListener listener, String nomeOuEmail, Context contexto) {
        clsJSONget executor = new clsJSONget(contexto);

        executor.addListener(new downloadFeitoListener() {
            @Override
            public void downloadConcluido(JSONArray result) {
                boolean retorno = false;
                if (result != null) {
                    JSONObject recebido;
                    try {
                        recebido = result.getJSONObject(0);
                        retorno = recebido.getBoolean("resposta");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                listener.booleanRetornado(retorno);
            }
        });
        executor.execute("http://scaws.azurewebsites.net/api/clsUsuarios?nomeOuEmail=" + Uri.encode(nomeOuEmail));
    }

    @Override
    protected String doInBackground(Void... params) {
            try {
                URL link = new URL(comando);
                HttpURLConnection conn = (HttpURLConnection) link.openConnection();
                conn.setReadTimeout(30000 /* milliseconds */);
                conn.setConnectTimeout(30000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);

                // faz o download
                conn.connect();

                //conversao de inputstream para string
                ByteArrayOutputStream intermediario = new ByteArrayOutputStream();  //intermediario para transformar o url em stream
                IOUtils.copy(conn.getInputStream(), intermediario);
                String retorno = intermediario.toString();
                return retorno.substring(1,retorno.length()-1);
            } catch (Exception o) {
                //previne crash se a mensagem for vazia
                if (o.getMessage()!=null)
                    Log.d("POST login", o.getMessage());
                return "ERRO_DOWNLOAD";
            }
        }

    public void addListener(usuarioCarregadoListener listener)
    {
        ouvinte=listener;
    }

    public void carregaUsuario(String nomeOuEmail, String senha,Context contexto) {

        clsJSONget executor = new clsJSONget(contexto);

        executor.addListener(new downloadFeitoListener() {
            @Override
            public void downloadConcluido(JSONArray result) {
                if (result != null) {
                    clsUsuarios retorno = null;
                    try {
                        JSONObject loop;
                        loop = result.getJSONObject(0);
                        retorno = new clsUsuarios(loop.getInt("idUsuario"), loop.getString("nomeUsuario"), loop.getString("emailUsuario"), loop.getString("senhaUsuario"));
                    } catch (JSONException e) {
                        Log.d(null, e.getMessage());
                    }
                    ouvinte.usuarioCarregado(retorno);
                } else {
                    ouvinte.usuarioCarregado(null);
                }
            }
        });
        executor.execute("http://scaws.azurewebsites.net/api/clsUsuarios?nomeouEmail=" + Uri.encode(nomeOuEmail) + "&senha=" + Uri.encode(senha));
    }

    public String cadastraUsuario() {
        //this nao foi usado pos asynctask so permite uma execucao por instancia
        clsUsuarios executa = new clsUsuarios();
        executa.comando="http://scaws.azurewebsites.net/api/clsUsuarios?nome=" + Uri.encode(this.nomeUsuario) + "&email=" + Uri.encode(this.emailUsuario) + "&senha=" + Uri.encode(this.senhaUsuario);
        executa.execute();
        try {
            return executa.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "ERRO_ASYNCTASK";
    }
}

