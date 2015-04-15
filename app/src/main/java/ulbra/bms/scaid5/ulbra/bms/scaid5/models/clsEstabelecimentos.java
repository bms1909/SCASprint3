package ulbra.bms.scaid5.ulbra.bms.scaid5.models;

import android.content.Context;
import android.location.Location;
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
 * Criador por Bruno em 16/03/2015.
 */
public class clsEstabelecimentos {
    public int idCategoria;
    public int idEstabelecimento;
    public LatLng latlonEstabelecimento;
    public String nomeEstabelecimento;
    public String enderecoEstabelecimento;
    public String cidadeEstabelecimento;
    public double mediaEstrelasAtendimento;
    public boolean possuiBanheiro;
    public boolean possuiEstacionamento;
    public boolean alturaCerta;
    public boolean possuiRampa;
    public boolean larguraSuficiente;
    public String telefoneEstabelecimento;

    public clsEstabelecimentos(int idEstabelecimento) {
        this.idEstabelecimento = idEstabelecimento;
    }

    //cadastro
    public clsEstabelecimentos(int idCat, String nome, String endereco, String cidade, boolean possBanheiro, boolean altCerta, boolean rampa, boolean largo, boolean estacionamento, String telefone, LatLng latlon) {
        this.idCategoria = idCat;
        this.nomeEstabelecimento = nome;
        this.enderecoEstabelecimento = endereco;
        this.cidadeEstabelecimento = cidade;
        this.possuiBanheiro = possBanheiro;
        this.alturaCerta = altCerta;
        this.possuiRampa = rampa;
        this.larguraSuficiente = largo;
        this.telefoneEstabelecimento = telefone;
        this.latlonEstabelecimento = latlon;
        this.possuiEstacionamento = estacionamento;
    }

    private clsEstabelecimentos(int idCat, int idEstab, String nome, String endereco, String cidade, double avgEstrelas, boolean possBanheiro, boolean altCerta, boolean rampa, boolean largo, String telefone,/*LatLng latlon*/double latitude, double longitude) {
        this.idCategoria = idCat;
        this.idEstabelecimento = idEstab;
        this.nomeEstabelecimento = nome;
        this.enderecoEstabelecimento = endereco;
        this.cidadeEstabelecimento = cidade;
        this.mediaEstrelasAtendimento = avgEstrelas;
        this.possuiBanheiro = possBanheiro;
        this.alturaCerta = altCerta;
        this.possuiRampa = rampa;
        this.larguraSuficiente = largo;
        this.telefoneEstabelecimento = telefone;
        this.latlonEstabelecimento = new LatLng(latitude, longitude);
    }

    public static ArrayList<clsEstabelecimentos> estabelecimentosPorRaio(int raio, Location local) {
        return clsEstabelecimentos.carregaEstabelecimentos("http://scaws.azurewebsites.net/api/clsEstabelecimentos?raioLongoKM=" + raio + "&latitude=" + local.getLatitude() + "&longitude=" + local.getLongitude());
    }

  /*  public static ArrayList<clsEstabelecimentos> estabelecimentosPorCategoria(int idCategoria) {
        return clsEstabelecimentos.carregaEstabelecimentos("http://scaws.azurewebsites.net/api/clsEstabelecimentos?idCategoria=" + idCategoria);
    }*/

    private static ArrayList<clsEstabelecimentos> carregaEstabelecimentos(String URL) {
        ArrayList<clsEstabelecimentos> retorno = new ArrayList<>();
        clsJSONget executor = new clsJSONget();
        JSONArray recebido = null;
        JSONObject loop;

        executor.execute(URL);

        try {
            recebido = executor.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(null, e.getMessage());
        }

        try {
            if (recebido != null) {
                for (int i = 0; i < recebido.length(); i++) {
                    loop = recebido.getJSONObject(i);
                    retorno.add(new clsEstabelecimentos(loop.getInt("idCategoria"), loop.getInt("idEstabelecimento"), loop.getString("nomeEstabelecimento"), loop.getString("enderecoEstabelecimento"), loop.getString("cidadeEstabelecimento"), loop.getInt("estrelasAtendimento") / loop.getInt("avaliadoresEstrelas"), loop.getBoolean("possuiBanheiro"), loop.getBoolean("alturaCerta"), loop.getBoolean("possuiRampa"), loop.getBoolean("larguraSuficiente"), loop.getString("telefoneEstabelecimento"), loop.getDouble("latitudeEstabelecimento"), loop.getDouble("longitudeEstabelecimento")));
                }
            }
        } catch (JSONException e) {
            Log.d(null, e.getMessage());
        }
        return retorno;

    }

    public clsEstabelecimentos carregaDetalhesEstabelecimento() {
        clsEstabelecimentos retorno = null;
        clsJSONget executor = new clsJSONget();
        JSONArray recebido = null;
        JSONObject loop;
        if (this.idEstabelecimento == 0)
            return null;
        executor.execute("http://scaws.azurewebsites.net/api/clsEstabelecimentos?SemUso=0&idEstabelecimento=" + this.idEstabelecimento);

        try {
            recebido = executor.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(null, e.getMessage());
        }

        try {

            if (recebido != null) {
                loop = recebido.getJSONObject(0);
                retorno = new clsEstabelecimentos(loop.getInt("idCategoria"), loop.getInt("idEstabelecimento"), loop.getString("nomeEstabelecimento"), loop.getString("enderecoEstabelecimento"), loop.getString("cidadeEstabelecimento"), loop.getDouble("estrelasAtendimento") / loop.getDouble("avaliadoresEstrelas"), loop.getBoolean("possuiBanheiro"), loop.getBoolean("alturaCerta"), loop.getBoolean("possuiRampa"), loop.getBoolean("larguraSuficiente"), loop.getString("telefoneEstabelecimento"), loop.getDouble("latitudeEstabelecimento"), loop.getDouble("longitudeEstabelecimento"));
            }


        } catch (JSONException e) {
            Log.d(null, e.getMessage());
        }
        return retorno;
    }

    public void avaliaEstabelecimento(int notaAvaliacao, Context context) {
        clsJSONpost executor = new clsJSONpost(context);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsEstabelecimentos?idEstabelecimento=" + this.idEstabelecimento + "&nota=" + notaAvaliacao);
    }

    public void cadastraEstabelecimento(int notaAvaliacao, Context context) {
        clsJSONpost executor = new clsJSONpost(context);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsEstabelecimentos?idCategoria=" + this.idCategoria + "&nomeEstabelecimento=" + Uri.encode(this.nomeEstabelecimento) + "&enderecoEstabelecimento=" + Uri.encode(this.enderecoEstabelecimento) + "&cidadeEstabelecimento=" + Uri.encode(this.cidadeEstabelecimento) + "&estrelasAtendimento=" + notaAvaliacao + "&avaliadoresEstrelas=" + 1 + "&possuiBanheiro=" + this.possuiBanheiro + "&possuiEstacionamento=" + this.possuiEstacionamento + "&alturaCerta=" + this.alturaCerta + "&possuiRampa=" + this.possuiRampa + "&larguraSuficiente=" + this.larguraSuficiente + "&telefoneEstabelecimento=" + Uri.encode(this.telefoneEstabelecimento) + "&latitudeEstabelecimento=" + this.latlonEstabelecimento.latitude + "&longitudeEstabelecimento=" + this.latlonEstabelecimento.longitude);
    }

}
