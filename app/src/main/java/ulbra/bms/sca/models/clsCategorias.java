package ulbra.bms.sca.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ulbra.bms.sca.controllers.clsJSONgetAssincrono;
import ulbra.bms.sca.interfaces.downloadFeitoListener;

/**
 * Criador por Bruno em 18/03/2015.
 */
//gambiarra confessa, sem isso travava ao solicitar as activities Main e DetalhesEstabelecimento
public class clsCategorias implements Parcelable {
    private int idCategoria;
    private String nomeCategoria;

    public clsCategorias(int id, String nome) {
        this.idCategoria = id;
        this.nomeCategoria = nome;
    }

    public static void sincronizaCategoriasServidor(Context contexto) {
        final clsBdLocal BD = new clsBdLocal(contexto);
        clsJSONgetAssincrono executor = new clsJSONgetAssincrono(contexto);
        executor.addListener(new downloadFeitoListener() {
            @Override
            public void downloadConcluido(JSONArray result) {
                if (result != null) {
                    JSONObject loop;
                    ArrayList<clsCategorias> salvar = new ArrayList<>();
                    try {
                        for (int i = 0; i < result.length(); i++) {
                            loop = result.getJSONObject(i);
                            salvar.add(new clsCategorias(loop.getInt("idCategoria"), loop.getString("nomeCategoria")));
                        }
                    } catch (JSONException e) {
                        Log.d(null, e.getMessage());
                    }
                    //apaga todas as categorias salvas localmente
                    BD.limpaCategorias();
                    //feito em duas etapas pra garantir que nao ocorreu nenhum JSONexception com o banco vazio
                    for (clsCategorias percorre : salvar) {
                        BD.insereCategoria(percorre.idCategoria, percorre.nomeCategoria);
                    }
                    BD.desconectaBanco();
                }
            }
        });
        executor.execute("http://scaws.azurewebsites.net/api/clsCategorias");
    }


    public static ArrayList<clsCategorias> carregaCategorias(Context contexto) {

        clsBdLocal BD = new clsBdLocal(contexto);
        ArrayList<clsCategorias> retorno = BD.buscaCategorias();
        BD.desconectaBanco();
        return retorno;
    }

    public static String getNomeCategoria(int idCategoria, Context contexto) {
        ArrayList<clsCategorias> percorre = carregaCategorias(contexto);
        for (clsCategorias loop : percorre) {
            if (loop.idCategoria == idCategoria)
                return loop.nomeCategoria;
        }
        return null;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
