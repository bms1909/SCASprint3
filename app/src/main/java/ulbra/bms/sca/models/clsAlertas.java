package ulbra.bms.sca.models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ulbra.bms.sca.controllers.ConexaoWS;
import ulbra.bms.sca.interfaces.alertasCarregadosListener;
import ulbra.bms.sca.interfaces.downloadFeitoListener;

/**
 * Criador por Bruno em 13/03/2015.
 */
public class clsAlertas{

    public int idUsuario;
    public int idAlerta;
    public LatLng latlonAlerta;
    public int tipoAlerta;
    public String descricaoAlerta;
    public int riscoAlerta;
    private alertasCarregadosListener ouvinte;

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
    public clsAlertas()
    {
        super();
    }

    public static void denunciaAlerta(int idAlerta, int idUsuario, Context contexto) {
        ConexaoWS.executaPost(contexto, "http://hefestows.azurewebsites.net/api/clsAlertas?idAlerta=" + idAlerta + "&idUsuario=" + idUsuario);
    }

    public void addListener(alertasCarregadosListener listener)
    {
        ouvinte = listener;
    }

    public void carregaAlertas(int raio, LatLng local,Context contexto) {

        downloadFeitoListener listener = new downloadFeitoListener() {
            @Override
            public void downloadConcluido(JSONArray result) {
                if (result != null) {
                    ArrayList<clsAlertas> retorno = new ArrayList<>();
                    JSONObject loop;
                    try {
                        for (int i = 0; i < result.length(); i++) {
                            loop = result.getJSONObject(i);
                            retorno.add(new clsAlertas(loop.getInt("idAlerta"), loop.getInt("idUsuario"), loop.getDouble("latitudeAlerta"), loop.getDouble("longitudeAlerta"), loop.getString("descricaoAlerta"), loop.getInt("tipoAlerta"), loop.getInt("riscoAlerta")));
                        }
                    } catch (JSONException | NullPointerException e) {
                        Log.d(null, e.getMessage());
                    }
                    ouvinte.alertasCarregados(retorno);
                } else
                    ouvinte.alertasCarregados(null);

            }
        };
        ConexaoWS.executaGet(listener, contexto, "http://hefestows.azurewebsites.net/api/clsAlertas?raioLongoemKM=" + raio + "&lat=" + local.latitude + "&lon=" + local.longitude);
    }

    public void cadastraAlerta(Context contexto) {
        ConexaoWS.executaPost(contexto, "http://hefestows.azurewebsites.net/api/clsAlertas?idUsuario=" + this.idUsuario + "&lat=" + this.latlonAlerta.latitude + "&lon=" + this.latlonAlerta.longitude + "&tipo=" + this.tipoAlerta + "&descricao=" + Uri.encode(this.descricaoAlerta) + "&risco=" + this.riscoAlerta);
    }

    public void editaAlerta(int riscoAlerta, int tipoAlerta, String descricaoAlerta,Context contexto) {
        ConexaoWS.executaPost(contexto, "http://hefestows.azurewebsites.net/api/clsAlertas?idAlerta=" + this.idAlerta + "&tipo=" + tipoAlerta + "&descricao=" + Uri.encode(descricaoAlerta) + "&risco=" + riscoAlerta);
    }

    public void excluiAlerta(Context contexto) {
        ConexaoWS.executaPost(contexto, "http://hefestows.azurewebsites.net/api/clsAlertas?idAlerta=" + this.idAlerta);
    }

}

