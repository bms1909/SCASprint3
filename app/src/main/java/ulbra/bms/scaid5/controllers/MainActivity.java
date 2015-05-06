package ulbra.bms.scaid5.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Spinner;
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
import java.util.List;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.interfaces.alertasCarregadosListener;
import ulbra.bms.scaid5.interfaces.estabelecimentosCarregadosListener;
import ulbra.bms.scaid5.models.clsAlertas;
import ulbra.bms.scaid5.models.clsApiClientSingleton;
import ulbra.bms.scaid5.models.clsEstabelecimentos;

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
    private clsAlertas alertasListener = new clsAlertas();
    private clsEstabelecimentos estabelecimentosListener = new clsEstabelecimentos();
    private SharedPreferences spIdUsuario;

    //region Mapa
    @Override
    /* ativado quando o mapa estiver instanciado */
    public void onMapReady(GoogleMap map) {
        //passa para um objeto local o googleMap instanciado
        objMapa = map;
        mLocalFocoCamera = new Location("");
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

        //desativa botões de direcionamento diretamente no mapa
        objMapa.getUiSettings().setMapToolbarEnabled(false);
        //ativa botão de localizar minha posição e icone azul no meu local
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
                LatLng local = new LatLng(localCarga.getLatitude(), localCarga.getLongitude());
                alertasListener.carregaAlertas(raioCargaMarcadores,local,this);
                estabelecimentosListener.estabelecimentosPorRaio(raioCargaMarcadores,local,this);
                mLocalUltimaCargaMarcadores = localCarga;
        }
    }

    //ativa com o click de um marcador do mapa
    @Override
    public boolean onMarkerClick(Marker marker) {
        alertaSelecionado = null;
        estabelecimentoSelecionado = null;
        final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

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

            dlgAlert.setTitle(estabelecimentoSelecionado.nomeEstabelecimento);
            dlgAlert.setIcon(R.drawable.ic_estabelecimento);
            dlgAlert.setMessage(estabelecimentoSelecionado.mediaEstrelasAtendimento + " Estrelas \n" + estabelecimentoSelecionado.enderecoEstabelecimento);
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
            //0= buraco
            //1=largura calçada
            //2=rampa
            if (alertaSelecionado.tipoAlerta == 0) {
                dlgAlert.setTitle("Buraco");
                if(alertaSelecionado.riscoAlerta==0)
                    dlgAlert.setIcon(R.drawable.ic_buraco_alto);
                else if(alertaSelecionado.riscoAlerta==1)
                    dlgAlert.setIcon(R.drawable.ic_buraco_medio);
                else
                    dlgAlert.setIcon(R.drawable.ic_buraco_baixo);
            } else if (alertaSelecionado.tipoAlerta == 1) {
                dlgAlert.setTitle("Calçada Estreita");
                if(alertaSelecionado.riscoAlerta==0)
                    dlgAlert.setIcon(R.drawable.ic_largura_alto);
                else if(alertaSelecionado.riscoAlerta==1)
                    dlgAlert.setIcon(R.drawable.ic_largura_medio);
                else
                    dlgAlert.setIcon(R.drawable.ic_largura_baixo);
            } else {
                dlgAlert.setTitle("Rampa com Defeito");
                if(alertaSelecionado.riscoAlerta==0)
                    dlgAlert.setIcon(R.drawable.ic_rampa_alto);
                else if(alertaSelecionado.riscoAlerta==1)
                    dlgAlert.setIcon(R.drawable.ic_rampa_medio);
                else
                    dlgAlert.setIcon(R.drawable.ic_rampa_baixo);
            }
            dlgAlert.setMessage("Comentário:\n" + alertaSelecionado.descricaoAlerta);
            dlgAlert.setPositiveButton("Denunciar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                        clsAlertas.denunciaAlerta(alertaSelecionado.idAlerta, spIdUsuario.getInt("ID_USUARIO", 0), MainActivity.this);
                        //a classe clsJSONpost garante o envio de dados com o armazenamento em banco até a confirmação de envio
                        Toast.makeText(MainActivity.this, "Alerta denunciado, Obrigado!", Toast.LENGTH_LONG).show();
                }
            });
            if(alertaSelecionado.idUsuario==spIdUsuario.getInt("ID_USUARIO", 0)) {
                dlgAlert.setNegativeButton("Editar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {

                            AlertDialog.Builder dlgEdita = new AlertDialog.Builder(MainActivity.this);
                            dlgEdita.setTitle("Edita Alerta");
                            LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                            final View viewDetalhes = inflater.inflate(R.layout.layout_edita_alerta,null);
                            final Spinner spTipo = (Spinner) viewDetalhes.findViewById(R.id.sp_editaalerta_tipo);
                            final RadioGroup rgRisco = (RadioGroup) viewDetalhes.findViewById(R.id.rg_editaalerta_risco);
                            final EditText txtDescricao = (EditText) viewDetalhes.findViewById(R.id.txt_editaalerta_descricao);

                            spTipo.setSelection(alertaSelecionado.tipoAlerta);
                            switch (alertaSelecionado.riscoAlerta) {
                                case 0: rgRisco.check(R.id.rbAlto);
                                    break;
                                case 1: rgRisco.check(R.id.rbMedio);
                                    break;
                                case 2: rgRisco.check(R.id.rbBaixo);
                                    break;
                            }
                            txtDescricao.setText(alertaSelecionado.descricaoAlerta);
                            dlgEdita.setView(viewDetalhes);

                            dlgEdita.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    int valorRisco=0;
                                    switch (rgRisco.getCheckedRadioButtonId())
                                    {
                                        case R.id.rbBaixo:valorRisco=2;
                                            break;
                                        case R.id.rbMedio:valorRisco=1;
                                            break;
                                        case R.id.rbAlto:valorRisco=0;
                                            break;
                                    }
                                    clsAlertas alterar = alertaSelecionado;
                                    alterar.editaAlerta(valorRisco,spTipo.getSelectedItemPosition(),txtDescricao.getText().toString(),MainActivity.this);
                                }
                            });
                            dlgEdita.setNeutralButton("Excluir", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertaSelecionado.excluiAlerta(MainActivity.this);
                                }
                            });
                            dlgEdita.setNegativeButton("Cancelar",null);
                            dlgEdita.create().show();

                        }
                    }
                });
            }
            dlgAlert.setNeutralButton("Voltar",null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }

        return false;
    }
//endregion

    //region Activity

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //objeto shared preferences, armazena o id do usuário logado
        spIdUsuario = getSharedPreferences("USUARIO", MODE_PRIVATE);
        //executa operações de POST pendentes
        if (clsJSONget.temInternet())
            clsJSONpost.executaPendentes(this);

        //listener disparado quando a carga de dados do webservice é concluída
        alertasListener.addListener(new alertasCarregadosListener() {
            @Override
            public void alertasCarregados(ArrayList<clsAlertas> alertas) {
                    alertasCarregados = alertas;
                    for (clsAlertas percorre : alertas) {
                        //TODO fazer ícones
                        // .icon personaliza o ícone,
                        //adiciona o marcador ver https://developers.google.com/maps/documentation/android/marker#customize_the_marker_image
                        //0= buraco
                        //1=largura calçada
                        //2=rampa
                        //risco alto = 0
                        //risco medio =1
                        //risco baixo =2
                        MarkerOptions icone = new MarkerOptions().position(percorre.latlonAlerta);
                        if (percorre.tipoAlerta == 0) {
                            icone.title("Buraco");
                            if(percorre.riscoAlerta==0)
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_buraco_alto));
                            else if(percorre.riscoAlerta==1)
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_buraco_medio));
                            else
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_buraco_baixo));
                        } else if (percorre.tipoAlerta == 1) {
                            icone.title("Calçada Estreita");
                            if(percorre.riscoAlerta==0)
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_largura_alto));
                            else if(percorre.riscoAlerta==1)
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_largura_medio));
                            else
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_largura_baixo));
                        } else {
                            icone.title("Rampa com defeito");
                            if(percorre.riscoAlerta==0)
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_rampa_alto));
                            else if(percorre.riscoAlerta==1)
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_rampa_medio));
                            else
                                icone.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_rampa_baixo));
                        }
                        objMapa.addMarker(icone);
                    }
            }
        });
        estabelecimentosListener.addListener(new estabelecimentosCarregadosListener() {
            @Override
            public void estabelecimentosCarregados(ArrayList<clsEstabelecimentos> estabelecimentos) {
                estabelecimentosCarregados = estabelecimentos;
                //carrega as listas de objetos alertas e estabelecimentos do webService
                //foreach do java
                for (clsEstabelecimentos percorre : estabelecimentosCarregados) {
                    objMapa.addMarker(new MarkerOptions().position(percorre.latlonEstabelecimento).title(percorre.nomeEstabelecimento).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_estabelecimento)));
                }
            }
        });


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
            else if(mlocalAtual!=null)
            {
                carregaMarcadores(mlocalAtual,1);
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
        if (id == R.id.btn_main_busca) {
            startActivity(new Intent(MainActivity.this, PesquisaLocaisActivity.class));
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
            String[] tiposAlerta = getResources().getStringArray(R.array.valores_array_tipos_alerta);

            final AlertDialog.Builder detalhe = new AlertDialog.Builder(this);
            final AlertDialog.Builder alertas = new AlertDialog.Builder(this);

            LayoutInflater inflater = this.getLayoutInflater();
            final View viewDetalhes = inflater.inflate(R.layout.layout_comenta_alerta,null);

            //adiciona o layout viewdetalhes como fonte para visual da view
            detalhe.setView(viewDetalhes);
            detalhe.setTitle("Detalhes");
            detalhe.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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

                    clsAlertas novo = new clsAlertas(spIdUsuario.getInt("ID_USUARIO",0), mlocalAtual.getLatitude(), mlocalAtual.getLongitude(), txtDescricao.getText().toString(), selecionado[0], risco);
                    novo.cadastraAlerta(MainActivity.this);

                    Toast.makeText(MainActivity.this, "Seu alerta aparecerá em breve, obrigado!", Toast.LENGTH_SHORT).show();
                    carregaMarcadores(mlocalAtual, 1);
                    dialog.cancel();
                }
            });
            detalhe.setNegativeButton("Cancelar", null);
            //click fora do AlertDialog
     /*       detalhe.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.cancel();
                    Toast.makeText(MainActivity.this, "Cancelado", Toast.LENGTH_LONG).show();
                }
            });*/

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
        //estabelecimentos já vem ordenados do webservice
        if(estabelecimentosCarregados==null)
        {
            Toast.makeText(this,"inicializando aplicativo, se a mensagem persistir, confira sua internet",Toast.LENGTH_SHORT);
        }
        else {
            ArrayList<String> tiposAlerta = new ArrayList<>();

            final ArrayList<clsEstabelecimentos> estabelecimentoSugeridos= new ArrayList<>();
            if(estabelecimentosCarregados.size()>15) {
                List<clsEstabelecimentos> temp = estabelecimentosCarregados.subList(0, 15);
                estabelecimentoSugeridos.addAll(temp);
            }
            else
            {
                estabelecimentoSugeridos.addAll(estabelecimentosCarregados);
            }

            for (clsEstabelecimentos percorre : estabelecimentoSugeridos) {

                tiposAlerta.add(percorre.nomeEstabelecimento);
            }
            AlertDialog.Builder alertas = new AlertDialog.Builder(this);
            alertas.setTitle("Avaliar Estabelecimento");
            alertas.setPositiveButton("Cadastrar Novo", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(MainActivity.this, CadastraEstabelecimentoActivity.class));
                }
            });
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_tipos_alerta, tiposAlerta);
            //define o diálogo como uma lista, passa o adapter.
            alertas.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int idSelecionado) {
                    arg0.cancel();
                    startActivity(new Intent(MainActivity.this, DetalhesEstabelecimentoActivity.class).putExtra("ID_ESTABELECIMENTO", estabelecimentoSugeridos.get(idSelecionado).idEstabelecimento));
                }
            });
            alertas.create().show();
        }
    }





//endregion

}

