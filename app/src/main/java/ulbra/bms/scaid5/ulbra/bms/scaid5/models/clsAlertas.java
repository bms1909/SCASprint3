package ulbra.bms.scaid5.ulbra.bms.scaid5.models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ulbra.bms.scaid5.controllers.clsJSONget;
import ulbra.bms.scaid5.controllers.clsJSONpost;

/**
 * Criador por Bruno em 13/03/2015.
 */
public class clsAlertas {

    public int idUsuario;
    public int idAlerta;
    public LatLng latlonAlerta;
    public int tipoAlerta;
    public String descricaoAlerta;
    public int riscoAlerta;

    private clsAlertas(int idAlerta, int idUsuario, double latitude, double longitude, String descricao, int tipo, int risco) {
        this.idUsuario = idUsuario;
        this.idAlerta = idAlerta;
        this.latlonAlerta = new LatLng(latitude, longitude);
        this.descricaoAlerta = descricao;
        this.tipoAlerta = tipo;
        this.riscoAlerta = risco;
    }

    public clsAlertas(int idUsuario, double latitude, double longitude, String descricao, int tipo, int risco) {
        this.idUsuario = idUsuario;
        this.latlonAlerta = new LatLng(latitude, longitude);
        this.descricaoAlerta = descricao;
        this.tipoAlerta = tipo;
        this.riscoAlerta = risco;
    }

    public static ArrayList<clsAlertas> carregaAlertas(int raio, LatLng local) {
        ArrayList<clsAlertas> retorno = new ArrayList<>();
        clsJSONget executor = new clsJSONget();
        JSONArray recebido = null;
        JSONObject loop;

        executor.execute("http://scaws.azurewebsites.net/api/clsAlertas?raioLongoemKM=" + raio + "&lat=" + local.latitude + "&lon=" + local.longitude);

        try {
            recebido = executor.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        try {
            if (recebido != null) {
                for (int i = 0; i < recebido.length(); i++) {
                    loop = recebido.getJSONObject(i);
                    retorno.add(new clsAlertas(loop.getInt("idAlerta"), loop.getInt("idUsuario"), loop.getDouble("latitudeAlerta"), loop.getDouble("longitudeAlerta"), loop.getString("descricaoAlerta"), loop.getInt("tipoAlerta"), loop.getInt("riscoAlerta")));
                }
            }
        } catch (JSONException | NullPointerException e) {
            Log.d(null, e.getMessage());
        }
        return retorno;
    }

    public static void denunciaAlerta(int idAlerta, int idUsuario, Context contexto) {
        clsJSONpost executor = new clsJSONpost(contexto);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsAlertas?idAlerta=" + idAlerta + "&idUsuario=" + idUsuario);
    }

    public void cadastraAlerta(Context contexto) {

        clsJSONpost executor = new clsJSONpost(contexto);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsAlertas?idUsuario=" + this.idUsuario + "&lat=" + this.latlonAlerta.latitude + "&lon=" + this.latlonAlerta.longitude + "&tipo=" + this.tipoAlerta + "&descricao=" + Uri.encode(this.descricaoAlerta) + "&risco=" + this.riscoAlerta);
    }
}
