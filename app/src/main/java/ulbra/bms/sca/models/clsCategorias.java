package ulbra.bms.sca.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ulbra.bms.sca.controllers.clsJSONget;

/**
 * Criador por Bruno em 18/03/2015.
 */
//gambiarra confessa, sem isso travava ao solicitar as activities Main e DetalhesEstabelecimento
public class clsCategorias implements Parcelable {
    private int idCategoria;
    private String nomeCategoria;

    private clsCategorias(int id, String nome) {
        this.idCategoria = id;
        this.nomeCategoria = nome;
    }

    public static ArrayList<clsCategorias> carregaCategorias() {
        ArrayList<clsCategorias> retorno = new ArrayList<>();
        clsJSONget executor = new clsJSONget();
        JSONArray recebido = null;
        JSONObject loop;

        executor.execute("http://scaws.azurewebsites.net/api/clsCategorias");

        try {
            recebido = executor.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(null, e.getMessage());
        }

        if (recebido != null) {
            try {
                for (int i = 0; i < recebido.length(); i++) {
                    loop = recebido.getJSONObject(i);
                    retorno.add(new clsCategorias(loop.getInt("idCategoria"), loop.getString("nomeCategoria")));
                }
            } catch (JSONException e) {
                Log.d(null, e.getMessage());
            }
        }
    /*  testes
        ArrayList<clsCategorias> a = new ArrayList<>();
        /*a.add(new clsCategorias(1,"Restaurante"));
        a.add(new clsCategorias(2,"Pizzaria"));
        a.add(new clsCategorias(2,"Prédio Público"));
        a.add(new clsCategorias(2,"Livraria"));
        a.add(new clsCategorias(2,"Outros"));
        return a;
        retorno = R.array.valores_array_categorias;*/
        return retorno;
    }

    public static String getNomeCategoria(int idCategoria) {
        ArrayList<clsCategorias> percorre = carregaCategorias();
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
