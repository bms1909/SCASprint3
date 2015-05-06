package ulbra.bms.scaid5.models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ulbra.bms.scaid5.controllers.clsJSONgetAssincrono;
import ulbra.bms.scaid5.controllers.clsJSONpost;
import ulbra.bms.scaid5.interfaces.alertasCarregadosListener;
import ulbra.bms.scaid5.interfaces.downloadFeitoListener;

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
    public void addListener(alertasCarregadosListener listener)
    {
        ouvinte = listener;
    }

    public void carregaAlertas(int raio, LatLng local,Context contexto) {

        clsJSONgetAssincrono executor = new clsJSONgetAssincrono(contexto);

        executor.addListener(new downloadFeitoListener() {
            @Override
            public void downloadConcluido(JSONArray result) {
                ArrayList<clsAlertas> retorno = new ArrayList<>();
                JSONObject loop;
                try {
                    if (result != null) {
                        for (int i = 0; i < result.length(); i++) {
                            loop = result.getJSONObject(i);
                            retorno.add(new clsAlertas(loop.getInt("idAlerta"), loop.getInt("idUsuario"), loop.getDouble("latitudeAlerta"), loop.getDouble("longitudeAlerta"), loop.getString("descricaoAlerta"), loop.getInt("tipoAlerta"), loop.getInt("riscoAlerta")));
                        }
                    }
                } catch (JSONException | NullPointerException e) {
                    Log.d(null, e.getMessage());
                }
                ouvinte.alertasCarregados(retorno);
            }
        });

        executor.execute("http://scaws.azurewebsites.net/api/clsAlertas?raioLongoemKM=" + raio + "&lat=" + local.latitude + "&lon=" + local.longitude);
    }

    public static void denunciaAlerta(int idAlerta, int idUsuario, Context contexto) {
        clsJSONpost executor = new clsJSONpost(contexto);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsAlertas?idAlerta=" + idAlerta + "&idUsuario=" + idUsuario);
    }

    public void cadastraAlerta(Context contexto) {

        clsJSONpost executor = new clsJSONpost(contexto);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsAlertas?idUsuario=" + this.idUsuario + "&lat=" + this.latlonAlerta.latitude + "&lon=" + this.latlonAlerta.longitude + "&tipo=" + this.tipoAlerta + "&descricao=" + Uri.encode(this.descricaoAlerta) + "&risco=" + this.riscoAlerta);
    }

    public void editaAlerta(int riscoAlerta, int tipoAlerta, String descricaoAlerta,Context contexto) {
        clsJSONpost executor = new clsJSONpost(contexto);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsAlertas?idUsuario=" + this.idUsuario + "&lat=" + this.latlonAlerta.latitude + "&lon=" + this.latlonAlerta.longitude + "&tipo=" + tipoAlerta + "&descricao=" + Uri.encode(descricaoAlerta) + "&risco=" + riscoAlerta);

    }

    public void excluiAlerta(Context contexto) {
        //TODO fazer esse e o de cima no WebService
        clsJSONpost executor = new clsJSONpost(contexto);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsAlertas?idUsuario=" + this.idUsuario + "&lat=" + this.latlonAlerta.latitude + "&lon=" + this.latlonAlerta.longitude + "&tipo=" + this.tipoAlerta + "&descricao=" + Uri.encode(this.descricaoAlerta) + "&risco=" + this.riscoAlerta);
    }

    }

