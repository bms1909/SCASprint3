package ulbra.bms.scaid5.ulbra.bms.scaid5.models;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Criado por Bruno on 24/03/2015.
 * implementa uma instância singleton da classe GoogleApiClient, utilizada para garantir o acesso a localização em todas as
 * activities e permitir que todas registrem seus listeners no mesmo objeto.
 */
public class clsApiClientSingleton {

    private static final clsApiClientSingleton INSTANCE = new clsApiClientSingleton();
    private GoogleApiClient mGoogleApiClient;
    private LocationListener mLocationListener;

    private clsApiClientSingleton() {
    }

    //tornam a classe singleton, ou seja, só uma instância no programa inteiro
    public static clsApiClientSingleton getInstance(Context contexto, LocationListener mLocationListener) {
        if (INSTANCE.mGoogleApiClient == null) {
            criaApiClient(contexto);
            INSTANCE.mLocationListener = mLocationListener;
        } else if (!INSTANCE.mGoogleApiClient.isConnected()) {
            INSTANCE.mLocationListener = mLocationListener;
            INSTANCE.mGoogleApiClient.connect();
        } else if (mLocationListener == INSTANCE.mLocationListener) {
            solicitaLocalizacao(INSTANCE.mLocationListener);
        } else {
            solicitaLocalizacao(mLocationListener);
        }
        return INSTANCE;
    }

    private static clsApiClientSingleton getInstance(Context contexto) {
        if (INSTANCE.mGoogleApiClient == null) {
            criaApiClient(contexto);
        }
        return INSTANCE;
    }

    private static void criaApiClient(Context contexto) {
        //contexto é pai de toda a activity, sendo o mesmo para o aplicativo inteiro
        INSTANCE.mGoogleApiClient = new GoogleApiClient.Builder(contexto)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        //acionado após a conexão, instanciar mLocationListener imediatamente após CriaApiClient
                        if (INSTANCE.mLocationListener != null) {
                            solicitaLocalizacao(INSTANCE.mLocationListener);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d("", "vish");
                    }
                })
                .addApi(LocationServices.API)
                .build();
        //conecta o googleApiClient, provocando o início do método abaixo
        INSTANCE.mGoogleApiClient.connect();
    }

    public static LatLng ultimoLocal(Context contexto) {
        clsApiClientSingleton local = getInstance(contexto);
        Location retorno = LocationServices.FusedLocationApi.getLastLocation(local.mGoogleApiClient);
        return new LatLng(retorno.getLatitude(), retorno.getLongitude());
    }

    private static void solicitaLocalizacao(LocationListener mLocationListener) {
        if (INSTANCE.mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    INSTANCE.mGoogleApiClient, (
                            new LocationRequest()
                                    .setInterval(10000)
                                    .setFastestInterval(5000)
                                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
                    , mLocationListener);
        }
    }

    public void suspendeLocalizacao(LocationListener mLocationListener) {
        if (mGoogleApiClient != null && mLocationListener != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
    }
}
