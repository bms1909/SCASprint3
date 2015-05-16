package ulbra.bms.sca.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ulbra.bms.sca.R;
import ulbra.bms.sca.interfaces.adapterListViewEstabelecimentos;
import ulbra.bms.sca.interfaces.enderecoEncontradoListener;
import ulbra.bms.sca.interfaces.estabelecimentosCarregadosListener;
import ulbra.bms.sca.models.clsCategorias;
import ulbra.bms.sca.models.clsEstabelecimentos;
import ulbra.bms.sca.utils.clsApiClientSingleton;
import ulbra.bms.sca.utils.clsPesquisaEndereco;

public class PesquisaLocaisActivity extends ActionBarActivity {

    //busca local de emissão do alerta
    private final LatLng localAtual = clsApiClientSingleton.ultimoLocal(this);
    private ListView lista;
    private EditText txtPesquisa;
    private ArrayList<clsEstabelecimentos> estabelecimentosCarregados;
    private ArrayList<clsCategorias> categoriasCarregadas;
    private List<Map<String, String>> elementosLista = new ArrayList<>();
    private float raioBusca;
    private boolean ordenarPorClassificacao;
    private int idCategoriaPesquisa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesquisa_locais);
        txtPesquisa = (EditText) findViewById(R.id.txt_busca);
        Spinner spRaio = (Spinner) findViewById(R.id.sp_busca_raio);
        spRaio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        raioBusca = 0.5f;
                        break;
                    case 1:
                        raioBusca = 2;
                        break;
                    case 2:
                        raioBusca = 5;
                        break;
                    case 3:
                        raioBusca = 10;
                        break;
                    case 4:
                        raioBusca = 25;
                        break;
                }
                if (localAtual == null) {
                    gpsDesligado();
                } else {
                    clsEstabelecimentos listener = new clsEstabelecimentos();
                    listener.addListener(new estabelecimentosCarregadosListener() {
                        @Override
                        public void estabelecimentosCarregados(ArrayList<clsEstabelecimentos> estabelecimentos) {
                            if (estabelecimentos != null) {
                                estabelecimentosCarregados = estabelecimentos;
                                if (txtPesquisa.getText().length() > 0) {
                                    buscaTexto(txtPesquisa.getText());
                                }
                                populaLista();
                            } else
                                Toast.makeText(PesquisaLocaisActivity.this, "Problema de conexão com o servidor, pesquisa de estabelecimentos indisponível", Toast.LENGTH_LONG).show();
                        }
                    });
                    listener.estabelecimentosPorRaio(raioBusca, localAtual, PesquisaLocaisActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Spinner spOrdena = (Spinner) findViewById(R.id.sp_busca_ordena);
        spOrdena.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ordenarPorClassificacao = position == 0;
                populaLista();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        txtPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscaTexto(s);
                populaLista();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        lista = (ListView) findViewById(R.id.list_busca);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map selecionado = elementosLista.get(position);
                if (selecionado.get("linha0").equals("e")) {
                    startActivity(new Intent(PesquisaLocaisActivity.this, DetalhesEstabelecimentoActivity.class).putExtra("ID_ESTABELECIMENTO", Integer.parseInt(selecionado.get("linha1").toString())));
                } else if (selecionado.get("linha0").equals("c")) {
                    somenteCategoria(Integer.parseInt(selecionado.get("linha1").toString()));
                    //startActivity(new Intent(PesquisaLocaisActivity.this, PesquisaCategoriaActivity.class).putExtra("ID_CATEGORIA", Integer.parseInt(selecionado.get("linha1").toString())));
                } else {
                    //volta a main activity com foco nas coordenadas passadas
                    startActivity(new Intent(PesquisaLocaisActivity.this, MainActivity.class).putExtra("LATITUDE", Double.parseDouble(selecionado.get("linha0").toString())).putExtra("LONGITUDE", Double.parseDouble(selecionado.get("linha1").toString())));
                }
            }
        });
        //configura activity para pesquisar tudo
        somenteCategoria(0);
        //carrega tela com todas as categorias mesmo sem pesquisa
        buscaCategoria("");
        populaLista();
    }

    @Override
    protected void onSaveInstanceState(Bundle Save)
    {
        Save.putInt("idCategoria",idCategoriaPesquisa);
        Save.putSerializable("Estabelecimentos",estabelecimentosCarregados);
        Save.putSerializable("Categorias",categoriasCarregadas);
        super.onSaveInstanceState(Save);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        idCategoriaPesquisa = savedInstanceState.getInt("idCategoria");
        if (idCategoriaPesquisa>0)
            somenteCategoria(idCategoriaPesquisa);
        //noinspection unchecked
        estabelecimentosCarregados = (ArrayList<clsEstabelecimentos>) savedInstanceState.getSerializable("Estabelecimentos");
        //noinspection unchecked
        categoriasCarregadas = (ArrayList<clsCategorias>) savedInstanceState.getSerializable("Categorias");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (idCategoriaPesquisa>0)
            somenteCategoria(0);
        else
            finish();
    }


    /**
     * @param idCategoria recebe 0 para ativar pesquisa global e o id da categoria para busca na categoria
     */
    private void somenteCategoria(int idCategoria)
    {
        ActionBar ab = getSupportActionBar();
        idCategoriaPesquisa=idCategoria;
        if (idCategoria>0) {
            String nomeCategoria = clsCategorias.getNomeCategoria(idCategoria, this);
            ab.setTitle(nomeCategoria);
            categoriasCarregadas.clear();
            txtPesquisa.setHint("Pesquise por "+ nomeCategoria );
            elementosLista.clear();
        }
        else {
            txtPesquisa.setHint(R.string.pesquisalocais_dica);
            ab.setTitle(R.string.title_activity_pesquisa_locais);
            categoriasCarregadas = clsCategorias.carregaCategorias(this);
        }
        populaLista();
    }

    private void gpsDesligado() {
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
        }
    }

    private void buscaCategoria(CharSequence parametro)
    {
        for (clsCategorias busca : categoriasCarregadas) {
            if (busca.getNomeCategoria().toLowerCase().contains(parametro)||parametro.length()==0) {
                Map<String, String> m = new HashMap<>();
                m.put("linha0", "c");
                m.put("linha1", "" + busca.getIdCategoria());
                m.put("linha2", busca.getNomeCategoria());
                m.put("linha4", "Categoria");
                elementosLista.add(m);
            }
        }
    }
    private void buscaTexto(CharSequence parametro) {
        elementosLista.clear();
        if(!parametro.equals("")) {
            parametro = parametro.toString().toLowerCase();
            if (idCategoriaPesquisa==0)
                buscaCategoria(parametro);
            for (clsEstabelecimentos busca : estabelecimentosCarregados) {
                if ((busca.nomeEstabelecimento.toLowerCase().contains(parametro)&&((idCategoriaPesquisa ==0||busca.idCategoria== idCategoriaPesquisa)))) {
                        Map<String, String> m = new HashMap<>();
                        DecimalFormat DF = new DecimalFormat("0.0");
                        m.put("linha0", "e");
                        m.put("linha1", "" + busca.idEstabelecimento);
                        m.put("linha2", busca.nomeEstabelecimento);
                        m.put("linha3", "" + DF.format(busca.mediaEstrelasAtendimento));
                        for (clsCategorias nome : categoriasCarregadas) {
                            if (nome.getIdCategoria() == busca.idCategoria) {
                                m.put("linha4", nome.getNomeCategoria());
                                break;
                            }
                        }
                        float[] distancia = new float[1];
                        Location.distanceBetween(localAtual.latitude, localAtual.longitude, busca.latlonEstabelecimento.latitude, busca.latlonEstabelecimento.longitude, distancia);
                        m.put("linha5", "" + DF.format(distancia[0] / 1000));
                        elementosLista.add(m);
                    }
                }
            }
            if(((parametro.toString().startsWith("rua"))||(parametro.toString().startsWith("av"))||(parametro.length()>4))&& idCategoriaPesquisa ==0)
            {
                clsPesquisaEndereco pesquisaEndereco = new clsPesquisaEndereco(this, raioBusca, localAtual, parametro.toString(), new enderecoEncontradoListener() {
                    @Override
                    public void enderecosEncontrados(List<Map<String, String>> Enderecos) {
                        elementosLista.addAll(Enderecos);
                        populaLista();
                    }
                });
                pesquisaEndereco.execute();

            }
        }

    private void populaLista() {

        //se = 0 eh classificacao
        final String qualLinha;
        //informa a linha a ser comparada
        if(ordenarPorClassificacao)
            qualLinha="linha3";
        else
            qualLinha = "linha5";
        //tipo comparator utilizado como condição na ordenação é implementado de forma anônima
        Collections.sort(elementosLista, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                int retorno = 0;
                if (lhs == null || rhs == null)
                    return 0;
                else if (!lhs.get("linha0").equals("e"))
                    return -1;
                else if (!rhs.get("linha0").equals("e"))
                    return 1;
                else if (Float.parseFloat(lhs.get(qualLinha).replace(",", ".")) < Float.parseFloat(rhs.get(qualLinha).replace(",", ".")))
                    retorno = 1;
                else if (Float.parseFloat(lhs.get(qualLinha).replace(",", ".")) > Float.parseFloat(rhs.get(qualLinha).replace(",", ".")))
                    retorno = -1;
                //se não é classificação, inverte o sinal do retorno para classificar por menor distância
                if (!ordenarPorClassificacao)
                    retorno = retorno * -1;
                return retorno;
            }
        });
        adapterListViewEstabelecimentos adapterLista = new adapterListViewEstabelecimentos(this, elementosLista);
        //define a fonte de dados da lista
        lista.setAdapter(adapterLista);
    }
}
