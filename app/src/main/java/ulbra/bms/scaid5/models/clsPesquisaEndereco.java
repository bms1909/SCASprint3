package ulbra.bms.scaid5.models;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ulbra.bms.scaid5.interfaces.enderecoEncontradoListener;

/**
 * Criado por Bruno on 08/05/2015.
 */
public class clsPesquisaEndereco extends AsyncTask<Void,Void,List<Map<String, String>>>
{
    private enderecoEncontradoListener ouvinte;
    private Context contexto;
    private float raioBusca;
    private LatLng localAtual;
    private String parametro;

    public clsPesquisaEndereco(Context contexto,float raioBusca,LatLng localAtual,String parametroPesquisa,enderecoEncontradoListener listener)
    {
        this.contexto=contexto;
        this.raioBusca=raioBusca;
        this.localAtual=localAtual;
        this.parametro=parametroPesquisa;
        this.ouvinte=listener;
    }
    @Override
    protected void onPostExecute(List<Map<String, String>> Result)
    {
        ouvinte.enderecosEncontrados(Result);
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        List<Address> listaEnderecos = new ArrayList<>();
        List<Map<String, String>> retorno = new ArrayList<>() ;
        Geocoder localizaEndereco = new Geocoder(this.contexto, Locale.getDefault());
                    /*e possivel fazer uma correspondencia aproximada de coordenadas geograficas com os seguintes valores:
                    * EX: -31.245678
                    * 1=possui precisao de 111.11km
                     * 2=precisao de 11.1km
                     * 4=1.11km
                     * 5=110m
                     * 6=11m
                     * 7=1.1m
                     * 8=0.11m
                     */
            double latMax,latMin,lonMax,lonMin;

            latMin = localAtual.latitude - (raioBusca / 100);
            lonMin = localAtual.longitude -(raioBusca / 100);
            latMax = localAtual.latitude + (raioBusca / 100);
            lonMax = localAtual.longitude +(raioBusca / 100);

        //busca no quadrado virtual de coordenadas o endereco, mas pode retornar fora dele(vai entender)
            try{
                listaEnderecos=localizaEndereco.getFromLocationName(parametro,5,latMin,lonMin,latMax,lonMax);
            //    listaEnderecos=localizaEndereco.getFromLocationName(parametro,10);
            }
            catch (IOException o)
            {
                Toast.makeText(this.contexto,"Pesquisa de Endereços indisponível, sem conexão com o servidor",Toast.LENGTH_SHORT).show();
                Log.d("sem internet",o.getMessage());
            }
            catch (IllegalArgumentException o)
            {
                Log.d("erro de argumentos",o.getMessage());
            }
            float []distanciaEndereco = new float[1];
            for (Address percorre:listaEnderecos)
            {
                //confere se o endereco esta exatamente dentro do raio de busca
                Location.distanceBetween(localAtual.latitude, localAtual.longitude, percorre.getLatitude(), percorre.getLongitude(), distanciaEndereco);
                distanciaEndereco[0] = distanciaEndereco[0]/1000;
                if (distanciaEndereco[0]<=raioBusca) {
                    Map<String, String> m = new HashMap<>();
                    m.put("linha0", "" + percorre.getLatitude());
                    m.put("linha1", "" + percorre.getLongitude());
                    m.put("linha2", percorre.getAddressLine(0));
                    m.put("linha4", percorre.getLocality());
                    DecimalFormat DF = new DecimalFormat("0.0");
                    m.put("linha5", "" + DF.format(distanciaEndereco[0]));
                    retorno.add(m);
                }
            }
        return retorno;
    }
}
