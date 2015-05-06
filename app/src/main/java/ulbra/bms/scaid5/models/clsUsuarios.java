package ulbra.bms.scaid5.models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ulbra.bms.scaid5.controllers.clsJSONgetAssincrono;
import ulbra.bms.scaid5.controllers.clsJSONpost;
import ulbra.bms.scaid5.interfaces.downloadFeitoListener;
import ulbra.bms.scaid5.interfaces.usuarioCarregadoListener;

/**
 * Criador por Bruno em 16/03/2015.
 */
public class clsUsuarios {
    public int idUsuario;
    public String nomeUsuario;
    public String emailUsuario;
    public String senhaUsuario;
    private usuarioCarregadoListener ouvinte;

    public clsUsuarios(String nome, String email, String senha) {
        this.nomeUsuario = nome;
        this.emailUsuario = email;
        this.senhaUsuario = senha;
    }

    public clsUsuarios(int id, String nome, String email, String senha) {
        this.idUsuario = id;
        this.nomeUsuario = nome;
        this.emailUsuario = email;
        this.senhaUsuario = senha;
    }

    public clsUsuarios() {
        super();
    }

    public void addListener(usuarioCarregadoListener listener)
    {
        ouvinte=listener;
    }

    public void carregaUsuario(String nomeOuEmail, String senha,Context contexto) {

        clsJSONgetAssincrono executor = new clsJSONgetAssincrono(contexto);

        executor.addListener(new downloadFeitoListener() {
            @Override
            public void downloadConcluido(JSONArray result) {
                clsUsuarios retorno = null;
                try {
                if (result != null) {
                    JSONObject loop;
                        loop = result.getJSONObject(0);
                        retorno = new clsUsuarios(loop.getInt("idUsuario"), loop.getString("nomeUsuario"), loop.getString("emailUsuario"), loop.getString("senhaUsuario"));
                    }
                }
                catch (JSONException e) {
                    Log.d(null, e.getMessage());
                }
                ouvinte.usuarioCarregado(retorno);
            }
        });

        executor.execute("http://scaws.azurewebsites.net/api/clsUsuarios?nomeouEmail=" + Uri.encode(nomeOuEmail) + "&senha=" + Uri.encode(senha));
    }

    public void cadastraUsuario(Context context) {
        clsJSONpost executor = new clsJSONpost(context);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsUsuarios?nome=" + Uri.encode(this.nomeUsuario) + "&email=" + Uri.encode(this.emailUsuario) + "&senha=" + Uri.encode(this.senhaUsuario));
    }
}
