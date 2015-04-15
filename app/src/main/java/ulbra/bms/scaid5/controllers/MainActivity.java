package ulbra.bms.scaid5.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsAlertas;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsApiClientSingleton;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsEstabelecimentos;

/**
 * Criado por Bruno on 19/03/2015.
 * classe padrão que atua como controller da tela activity_main
 */
public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap objMapa;
    private boolean segueUsuario;
    private Location mLocalUltimaCargaMarcadores;
    private Location mlocalAtual;
    private LocationListener mLocationListener;
    private Location mLocalFocoCamera;
    private clsApiClientSingleton mGerenciadorApiClient;
    private ArrayList<clsAlertas> alertasCarregados;
    private ArrayList<clsEstabelecimentos> estabelecimentosCarregados;
    private clsAlertas alertaSelecionado;
    private clsEstabelecimentos estabelecimentoSelecionado;

    //region Mapa
    @Override
    /* ativado quando o mapa estiver instanciado */
    public void onMapReady(GoogleMap map) {
        //passa para um objeto local o googleMap instanciado
        objMapa = map;
        mLocalFocoCamera= new Location("");
        mLocalFocoCamera.setTime(new Date().getTime());
        objMapa.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (!segueUsuario) {
                    mLocalFocoCamera.setLatitude(cameraPosition.target.latitude);
                    mLocalFocoCamera.setLongitude(cameraPosition.target.longitude);
                    if (mLocalFocoCamera.distanceTo(mLocalUltimaCargaMarcadores) > 1000) {
                        carregaMarcadores(mLocalFocoCamera, 1);
                    }
                }
            }
        });

        //ativa botão de localizar minha posição
        objMapa.setMyLocationEnabled(true);
        //listener do botão de minha localização
        objMapa.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                segueUsuario = true;
                return false;
            }
        });
        //amarra evento de clique no marcador
        objMapa.setOnMarkerClickListener(this);
    }

    private void carregaMarcadores(Location localCarga, int raioCargaMarcadores) {
        if (objMapa != null) {
            if (!clsJSONget.temInternet()) {
                Toast.makeText(this, "Sem acesso a internet, as informações podem estar desatualizadas", Toast.LENGTH_LONG).show();
            } else {
                alertasCarregados = clsAlertas.carregaAlertas(raioCargaMarcadores, localCarga);
                estabelecimentosCarregados = clsEstabelecimentos.estabelecimentosPorRaio(raioCargaMarcadores, localCarga);
                //carrega as listas de objetos alertas e estabelecimentos do webService
                //foreach do java
                for (clsAlertas percorre : alertasCarregados) {
                    //TODO fazer ícones
                    // .icon personaliza o ícone,
                    //adiciona o marcador ver https://developers.google.com/maps/documentation/android/marker#customize_the_marker_image
                    objMapa.addMarker(new MarkerOptions().position(percorre.latlonAlerta).title(percorre.descricaoAlerta).icon(BitmapDescriptorFactory.fromResource(R.drawable.common_signin_btn_icon_focus_light)));
                }
                for (clsEstabelecimentos percorre : estabelecimentosCarregados) {
                    objMapa.addMarker(new MarkerOptions().position(percorre.latlonEstabelecimento).title(percorre.nomeEstabelecimento).icon(BitmapDescriptorFactory.fromResource(R.drawable.common_signin_btn_icon_focus_dark)));
                }

                mLocalUltimaCargaMarcadores = localCarga;
            }
        }
    }

    //ativa com o click de um marcador do mapa
    @Override
    public boolean onMarkerClick(Marker marker) {
        alertaSelecionado = null;
        estabelecimentoSelecionado = null;
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

        //busca entre os alertas carregados, o alerta correspondente as coordenadas do selecionado
        for (int x = 0; x < alertasCarregados.size(); x++) {
            if (marker.getPosition().equals(alertasCarregados.get(x).latlonAlerta)) {
                alertaSelecionado = alertasCarregados.get(x);
            }
        }
        if (alertaSelecionado == null) {
            //se não é um alerta, busca entre os estabelecimentos
            for (int x = 0; x < estabelecimentosCarregados.size(); x++) {

                if (marker.getPosition().equals(estabelecimentosCarregados.get(x).latlonEstabelecimento)) {
                    estabelecimentoSelecionado = estabelecimentosCarregados.get(x);
                }
            }

            dlgAlert.setTitle("Estabelecimento");
            dlgAlert.setIcon(R.drawable.common_signin_btn_icon_focus_dark);
            dlgAlert.setMessage(estabelecimentoSelecionado.nomeEstabelecimento);
            dlgAlert.setPositiveButton("Abrir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //inicia tela de detalhes do estabelecimento, enviado o ID do mesmo via putExtra
                    startActivity(new Intent(MainActivity.this, DetalhesEstabelecimentoActivity.class).putExtra("ID_ESTABELECIMENTO", estabelecimentoSelecionado.idEstabelecimento));
                }
            });
            dlgAlert.setNeutralButton("Voltar", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        } else {
            //pop-up marcações
            dlgAlert.setTitle("Alerta");
            dlgAlert.setIcon(R.drawable.common_signin_btn_icon_focus_light);
            dlgAlert.setMessage("Este Marcador é " + alertaSelecionado.descricaoAlerta);
            dlgAlert.setPositiveButton("Denunciar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clsAlertas.denunciaAlerta(alertaSelecionado.idAlerta, MainActivity.this);
                    Toast.makeText(MainActivity.this, "Alerta denunciado, Obrigado!)", Toast.LENGTH_LONG).show();
                }
            });
            dlgAlert.setNeutralButton("Voltar", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }

        //
        return false;
    }

    private void moveCamera(Location localAtual) {
        try {
            //desloca a visualização do mapa para a coordenada informada

            objMapa.animateCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(localAtual.getLatitude(), localAtual.getLongitude())), 17), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                }

                //ativado se o usuário interromper a movimentação da camera
                @Override
                public void onCancel() {
                    segueUsuario = false;
                }
            });
        } catch (NullPointerException e) {
            Log.d("erro ao mover camera", e.getMessage());
        }
    }
//endregion

    //region Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //executa operações de POST pendentes
        if (clsJSONget.temInternet())
            clsJSONpost.executaPendentes(this);

        //configura se o método movecamera deve ser acionado ao mudar a localização
        segueUsuario = true;

        //cria o listener local de localização e implementa o método de monitoramento do mesmo
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location localAtual) {
                //confere se a camera deve ser movida
                if (segueUsuario) {
                    moveCamera(localAtual);
                }
                //se não houver nenhuma carga de dados anterior, executa
                if (mLocalUltimaCargaMarcadores == null) {
                    carregaMarcadores(localAtual, 1);
                }
                //compara a distância da última carga de dados realizada com a atual, em metros
                else if (localAtual.distanceTo(mLocalUltimaCargaMarcadores) > 300) {
                    carregaMarcadores(localAtual, 1);
                }
                mlocalAtual = localAtual;
            }
        };
    }

    //ativado após o retorno da activity ao foco principal
    @Override
    protected void onPostResume() {
        super.onPostResume();

        //confere se o GPS está ligado
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //cria uma caixa de diálogo caso o GPS esteja desligado
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } else {
                        finish();
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Localização Desativada");
            builder.setMessage("Este aplicativo utiliza sua localização com Alta Precisão (GPS), deseja habilitar agora?");
            //se o malandro pressionar fora do AlertDialog, fecha o aplicativo
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });

            builder.setPositiveButton("Sim", dialogClickListener).setNegativeButton("Não", dialogClickListener);
            builder.create().show();
        } else {
            //obtem uma instancia singleton do objeto, registrando seu próprio listener
            mGerenciadorApiClient = clsApiClientSingleton.getInstance(this, mLocationListener);
            if (objMapa == null) {
                //prepara o mapa como objeto, provoca onmapready
                MapFragment mapFragment = (MapFragment) getFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            }
        }
    }

    @Override
    protected void onStop() {
        if (mLocationListener != null && mGerenciadorApiClient != null)
            mGerenciadorApiClient.suspendeLocalizacao(mLocationListener);
        super.onStop();
        if (clsJSONget.temInternet())
            clsJSONpost.executaPendentes(this);
    }

    @Override
    protected void onDestroy() {
        if (mGerenciadorApiClient != null)
            mGerenciadorApiClient.suspendeLocalizacao(mLocationListener);
        mGerenciadorApiClient = null;
        mLocalUltimaCargaMarcadores = null;
        objMapa = null;
        mLocationListener = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "você clicou em configurações, aguarde novas atualizações!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
//endregion

//region Alertas

    public void btnAlertar_Click(View a) {
        //confere precisão do local obtido
        if (mlocalAtual == null || mlocalAtual.getAccuracy() > 20) {
            Toast.makeText(MainActivity.this, "Aguardando local preciso", Toast.LENGTH_LONG).show();
        } else {

            final int[] selecionado = new int[1];
            ArrayList<String> tiposAlerta = new ArrayList<>();
            final AlertDialog.Builder detalhe = new AlertDialog.Builder(this);
            final AlertDialog.Builder alertas = new AlertDialog.Builder(this);

            LayoutInflater inflater = this.getLayoutInflater();
            final View viewDetalhes = inflater.inflate(R.layout.layout_comenta_alerta, null);


            tiposAlerta.add("Buracos");
            tiposAlerta.add("Calçada Estreita");
            tiposAlerta.add("Meio-fio rebaixado");


            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout

            detalhe.setView(viewDetalhes);
            detalhe.setTitle("Detalhes");
            detalhe.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO colocar idUsuario


                    RadioGroup rdGrupo = (RadioGroup) viewDetalhes.findViewById(R.id.rgDetalhesAlerta);
                    EditText txtDescricao = (EditText) viewDetalhes.findViewById(R.id.txt_descricao_alerta);
                    int risco = 0;
                    switch (rdGrupo.getCheckedRadioButtonId()) {
                        case R.id.rbAlto:
                            risco = 0;
                            break;
                        case R.id.rbMedio:
                            risco = 1;
                            break;
                        case R.id.rbBaixo:
                            risco = 2;
                            break;
                    }
                    clsAlertas novo = new clsAlertas(7, mlocalAtual.getLatitude(), mlocalAtual.getLongitude(), txtDescricao.getText().toString(), selecionado[0], risco);
                    novo.cadastraAlerta(MainActivity.this);

                    Toast.makeText(MainActivity.this, "Seu alerta aparecerá em breve, obrigado!", Toast.LENGTH_SHORT).show();

                    carregaMarcadores(mlocalAtual, 1);
                    dialog.cancel();
                }
            });
            detalhe.setNegativeButton("Cancelar", null);
            //click fora do AlertDialog
            detalhe.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.cancel();
                    Toast.makeText(MainActivity.this, "Cancelado", Toast.LENGTH_LONG).show();
                }
            });

            alertas.setTitle("Informar");
            alertas.setNegativeButton("Voltar", null);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_tipos_alerta, tiposAlerta);
            //define o diálogo como uma lista, passa o adapter.
            alertas.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int idSelecionado) {
                    selecionado[0] = idSelecionado;
                    arg0.cancel();
                    detalhe.create().show();
                }
            });
            alertas.create().show();
        }
    }

    public void btnNovoEstabelecimento_Click(View view) {
        startActivity(new Intent(MainActivity.this, CadastraEstabelecimentoActivity.class));
    }
//endregion

}

