package ulbra.bms.sca.models;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ulbra.bms.sca.controllers.clsJSONget;
import ulbra.bms.sca.controllers.clsJSONgetAssincrono;
import ulbra.bms.sca.controllers.clsJSONpost;
import ulbra.bms.sca.interfaces.downloadFeitoListener;
import ulbra.bms.sca.interfaces.estabelecimentosCarregadosListener;

/**
 * Criador por Bruno em 16/03/2015.
 */

//gambiarra confessa, sem isso travava ao solicitar as activities Main e DetalhesEstabelecimento
public class clsEstabelecimentos implements Parcelable {
    public int idCategoria;
    public int idEstabelecimento;
    public LatLng latlonEstabelecimento;
    public String nomeEstabelecimento;

    public String enderecoEstabelecimento;
    public String bairroEstabelecimento;
    public String cidadeEstabelecimento;
    public String estadoEstabelecimento;

    public float mediaEstrelasAtendimento;
    public boolean possuiBanheiro;
    public boolean possuiEstacionamento;
    public boolean alturaCerta;
    public boolean possuiRampa;
    public boolean larguraSuficiente;
    public String telefoneEstabelecimento;

    private estabelecimentosCarregadosListener ouvinte;

    //region Construtores
    public clsEstabelecimentos(int idEstabelecimento) {
        this.idEstabelecimento = idEstabelecimento;
    }

    //cadastro
    public clsEstabelecimentos(int idCat, String nome, String endereco, String bairro,String cidade,String estado, boolean possBanheiro, boolean altCerta, boolean rampa, boolean largo, boolean estacionamento, String telefone, LatLng latlon, int nota) {
        this.idCategoria = idCat;
        this.nomeEstabelecimento = nome;
        this.enderecoEstabelecimento = endereco;
        this.cidadeEstabelecimento = cidade;
        this.estadoEstabelecimento = estado;
        this.bairroEstabelecimento = bairro;
        this.possuiBanheiro = possBanheiro;
        this.alturaCerta = altCerta;
        this.possuiRampa = rampa;
        this.mediaEstrelasAtendimento = nota;
        this.larguraSuficiente = largo;
        this.telefoneEstabelecimento = telefone;
        this.latlonEstabelecimento = latlon;
        this.possuiEstacionamento = estacionamento;
    }

    private clsEstabelecimentos(int idCat, int idEstab, String nome, String endereco, String bairro,String cidade,String estado, double avgEstrelas, boolean possBanheiro, boolean estacionamento, boolean altCerta, boolean rampa, boolean largo, String telefone, double latitude, double longitude) {
        this.idCategoria = idCat;
        this.idEstabelecimento = idEstab;
        this.nomeEstabelecimento = nome;
        this.enderecoEstabelecimento = endereco;
        this.cidadeEstabelecimento = cidade;
        this.bairroEstabelecimento = bairro;
        this.estadoEstabelecimento = estado;
        this.mediaEstrelasAtendimento = (float) avgEstrelas;
        this.possuiEstacionamento = estacionamento;
        this.possuiBanheiro = possBanheiro;
        this.alturaCerta = altCerta;
        this.possuiRampa = rampa;
        this.larguraSuficiente = largo;
        this.telefoneEstabelecimento = telefone;
        this.latlonEstabelecimento = new LatLng(latitude, longitude);
    }

    public clsEstabelecimentos() {
        super();
    }
    //endregion

    public static boolean estabelecimentoFoiAvaliado(int idUsuario, int idEstabelecimento) {
        JSONObject retorno;
        JSONArray recebido = null;
        clsJSONget executor = new clsJSONget();
        executor.execute("http://scaws.azurewebsites.net/api/clsEstabelecimentos?idUsuario=" + idUsuario + "&idEstabelecimento=" + idEstabelecimento);

        try {
            recebido = executor.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(null, e.getMessage());
        }

        try {

            if (recebido != null) {
                retorno = recebido.getJSONObject(0);
                return retorno.getBoolean("resposta");
            }
        } catch (Exception e) {
            Log.d(null, e.getMessage());
        }
        return false;
    }

    public void addListener(estabelecimentosCarregadosListener listener)
    {
        this.ouvinte= listener;
    }

    /*
    public void estabelecimentosPorCategoria(float raio, LatLng local, int idCategoria,Context contexto) {
        this.carregaEstabelecimentos("http://scaws.azurewebsites.net/api/clsEstabelecimentos?raioLongoKM=" + raio + "&latitude=" + local.latitude + "&longitude=" + local.longitude + "&idCategoria=" + idCategoria,contexto);
    }*/
    public void estabelecimentosPorRaio(float raio, LatLng local,Context contexto) {
        this.carregaEstabelecimentos("http://scaws.azurewebsites.net/api/clsEstabelecimentos?raioLongoKM=" + raio + "&latitude=" + local.latitude + "&longitude=" + local.longitude, contexto);
    }

    private void carregaEstabelecimentos(String URL,Context contexto) {
        clsJSONgetAssincrono executor = new clsJSONgetAssincrono(contexto);


        executor.addListener(new downloadFeitoListener() {
            @Override
            public void downloadConcluido(JSONArray result) {
                ArrayList<clsEstabelecimentos> retorno = new ArrayList<>();
                try {
                    if (result != null) {
                        JSONObject loop;
                        for (int i = 0; i < result.length(); i++) {
                            loop = result.getJSONObject(i);
                            retorno.add(new clsEstabelecimentos(
                                    loop.getInt("idCategoria"),
                                    loop.getInt("idEstabelecimento"),
                                    loop.getString("nomeEstabelecimento"),
                                    loop.getString("enderecoEstabelecimento"),
                                    loop.getString("bairroEstabelecimento"),
                                    loop.getString("cidadeEstabelecimento"),
                                    loop.getString("estadoEstabelecimento"),
                                    loop.getDouble("estrelasEstabelecimento"),
                                    loop.getBoolean("possuiBanheiro"),
                                    loop.getBoolean("possuiEstacionamento"),
                                    loop.getBoolean("alturaCerta"),
                                    loop.getBoolean("possuiRampa"),
                                    loop.getBoolean("larguraSuficiente"),
                                    loop.getString("telefoneEstabelecimento"),
                                    loop.getDouble("latitudeEstabelecimento"),
                                    loop.getDouble("longitudeEstabelecimento")));
                        }
                    }
                } catch (JSONException e) {
                    Log.d(null, e.getMessage());
                }
                ouvinte.estabelecimentosCarregados(retorno);
            }
        });



        executor.execute(URL);


    }

    public clsEstabelecimentos carregaDetalhesEstabelecimento() {
        clsEstabelecimentos retorno = null;
        clsJSONget executor = new clsJSONget();
        JSONArray recebido = null;
        JSONObject loop;
        if (this.idEstabelecimento == 0)
            return null;
        executor.execute("http://scaws.azurewebsites.net/api/clsEstabelecimentos?idEstabelecimento=" + this.idEstabelecimento);

        try {
            recebido = executor.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(null, e.getMessage());
        }

        try {

            if (recebido != null) {
                loop = recebido.getJSONObject(0);
                retorno = new clsEstabelecimentos(
                        loop.getInt("idCategoria"),
                        loop.getInt("idEstabelecimento"),
                        loop.getString("nomeEstabelecimento"),
                        loop.getString("enderecoEstabelecimento"),
                        loop.getString("bairroEstabelecimento"),
                        loop.getString("cidadeEstabelecimento"),
                        loop.getString("estadoEstabelecimento"),
                        loop.getDouble("estrelasEstabelecimento"),
                        loop.getBoolean("possuiBanheiro"),
                        loop.getBoolean("possuiEstacionamento"),
                        loop.getBoolean("alturaCerta"),
                        loop.getBoolean("possuiRampa"),
                        loop.getBoolean("larguraSuficiente"),
                        loop.getString("telefoneEstabelecimento"),
                        loop.getDouble("latitudeEstabelecimento"),
                        loop.getDouble("longitudeEstabelecimento"));
            }
        } catch (JSONException e) {
            Log.d(null, e.getMessage());
        }
        return retorno;
    }

    public void avaliaEstabelecimento(int notaAvaliacao, int idUsuario, Context context) {
        clsJSONpost executor = new clsJSONpost(context);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsEstabelecimentos?idEstabelecimento=" + this.idEstabelecimento + "&idUsuario=" + idUsuario + "&nota=" + notaAvaliacao);
    }

    public void cadastraEstabelecimento(int idUsuario, Context context) {
        clsJSONpost executor = new clsJSONpost(context);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsEstabelecimentos?idCategoria=" + this.idCategoria + "&nomeEstabelecimento=" + Uri.encode(this.nomeEstabelecimento) + "&enderecoEstabelecimento=" + Uri.encode(this.enderecoEstabelecimento) + "&bairro="+Uri.encode(this.bairroEstabelecimento)+  "&cidadeEstabelecimento=" + Uri.encode(this.cidadeEstabelecimento) + "&estado="+this.estadoEstabelecimento+"&possuiBanheiro=" + this.possuiBanheiro + "&possuiEstacionamento=" + this.possuiEstacionamento + "&alturaCerta=" + this.alturaCerta + "&possuiRampa=" + this.possuiRampa + "&larguraSuficiente=" + this.larguraSuficiente + "&telefoneEstabelecimento=" + Uri.encode(this.telefoneEstabelecimento) + "&latitudeEstabelecimento=" + this.latlonEstabelecimento.latitude + "&longitudeEstabelecimento=" + this.latlonEstabelecimento.longitude + "&idUsuario=" + idUsuario + "&nota=" + (int) this.mediaEstrelasAtendimento);
    }

    public void editaEstabelecimento(int idUsuario, Context context) {
        clsJSONpost executor = new clsJSONpost(context);
        executor.executaPost("http://scaws.azurewebsites.net/api/clsEstabelecimentos?idEstabelecimento="+this.idEstabelecimento+"&idCategoria=" + this.idCategoria + "&nomeEstabelecimento=" + Uri.encode(this.nomeEstabelecimento) + "&enderecoEstabelecimento=" + Uri.encode(this.enderecoEstabelecimento) + "&bairro="+Uri.encode(this.bairroEstabelecimento)+  "&cidadeEstabelecimento=" + Uri.encode(this.cidadeEstabelecimento) + "&estado="+this.estadoEstabelecimento+"&possuiBanheiro=" + this.possuiBanheiro + "&possuiEstacionamento=" + this.possuiEstacionamento + "&alturaCerta=" + this.alturaCerta + "&possuiRampa=" + this.possuiRampa + "&larguraSuficiente=" + this.larguraSuficiente + "&telefoneEstabelecimento=" + Uri.encode(this.telefoneEstabelecimento) + "&latitudeEstabelecimento=" + this.latlonEstabelecimento.latitude + "&longitudeEstabelecimento=" + this.latlonEstabelecimento.longitude + "&idUsuario=" + idUsuario + "&nota=" + (int) this.mediaEstrelasAtendimento);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
