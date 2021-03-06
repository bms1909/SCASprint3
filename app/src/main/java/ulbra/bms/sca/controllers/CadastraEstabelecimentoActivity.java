package ulbra.bms.sca.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ulbra.bms.sca.R;
import ulbra.bms.sca.models.clsCategorias;
import ulbra.bms.sca.models.clsEstabelecimentos;
import ulbra.bms.sca.utils.clsApiClientSingleton;


public class CadastraEstabelecimentoActivity extends ActionBarActivity {

    private ArrayList<clsCategorias> categoriasCarregadas;
    private clsEstabelecimentos editar;

    private CheckBox cbxLargura;
    private CheckBox cbxRampa;
    private CheckBox cbxAltura;
    private CheckBox cbxEstacionamento;
    private CheckBox cbxBanheiro;
    private EditText txtFone;
    private Spinner spEstado;
    private AutoCompleteTextView txtCidade;
    private EditText txtBairro;
    private EditText txtEndereco;
    private EditText txtTitulo;
    private RatingBar rb;
    private Spinner spCategorias;
    private String[] codigosEstados ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastra_estabelecimento);
        codigosEstados= getResources().getStringArray(R.array.valores_array_estados_codigos);
         spCategorias = (Spinner) findViewById(R.id.sp_novoestabelecimento_categorias);
         rb = (RatingBar) findViewById(R.id.rb_novoestabelecimento_classificacao);
         txtTitulo = (EditText) findViewById(R.id.txt_novoestabelecimento_nome);
         txtEndereco = (EditText) findViewById(R.id.txt_novoestabelecimento_endereco);
         txtBairro = (EditText) findViewById(R.id.txt_novoestabelecimento_bairro);
         txtCidade = (AutoCompleteTextView) findViewById(R.id.txt_novoestabelecimento_cidade);
         spEstado = (Spinner) findViewById(R.id.sp_novoestabelecimento_estado);
         txtFone = (EditText) findViewById(R.id.txt_novoestabelecimento_telefone);
         cbxBanheiro = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_possui_banheiro);
         cbxEstacionamento = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_possui_estacionamento);
        cbxAltura = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_altura_certa);
        cbxRampa = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_possui_rampa);
        cbxLargura = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_largura_suficiente);

        //preenche txt cidades com todas as cidades do array de strings
        String[] countries = getResources().getStringArray(R.array.valores_array_cidades);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries);
        txtCidade.setAdapter(adapter);

        rb.setMax(5);

        //region Spinner das Categorias
        categoriasCarregadas = clsCategorias.carregaCategorias(this);
        clsCategorias percorre;
        String[] nomesCategoria = new String[categoriasCarregadas.size()];
        for (int x = 0; x < categoriasCarregadas.size(); x++) {
            percorre = categoriasCarregadas.get(x);
            nomesCategoria[x] = percorre.getNomeCategoria();
        }
        ArrayAdapter<String> aOpcoes;
        // Declarando variavel do tipo Spinner
        aOpcoes = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nomesCategoria);
        // capturando o spinner do xml pela id

        spCategorias.setAdapter(aOpcoes);
        //endregion

        //recebe dados para edição
        Intent recebido = getIntent();
        editar = new clsEstabelecimentos();
        //se houver retorno
        if(recebido.getIntExtra("ID_ESTABELECIMENTO", 0)>0) {
                    //busca da intent recebida os dados para edicao
                    editar.idEstabelecimento= recebido.getIntExtra("ID_ESTABELECIMENTO",0);
                    editar.idCategoria=       recebido.getIntExtra("ID_CATEGORIA",0);
                    editar.nomeEstabelecimento=          recebido.getStringExtra("NOME");
                    editar.enderecoEstabelecimento  =         recebido.getStringExtra("ENDERECO");
                    editar.bairroEstabelecimento =        recebido.getStringExtra("BAIRRO");
                    editar.cidadeEstabelecimento =       recebido.getStringExtra("CIDADE");
                    editar.estadoEstabelecimento  =      recebido.getStringExtra("ESTADO");
                    editar.mediaEstrelasAtendimento =     recebido.getFloatExtra("ESTRELAS", 0);
                    editar.possuiBanheiro =    recebido.getBooleanExtra("BANHEIRO", false);
                    editar.possuiEstacionamento=   recebido.getBooleanExtra("ESTACIONAMENTO", false);
                    editar.alturaCerta =  recebido.getBooleanExtra("ALTURA", false);
                    editar.possuiRampa =recebido.getBooleanExtra("RAMPA", false);
                    editar.larguraSuficiente=recebido.getBooleanExtra("LARGURA", false);
                    editar.telefoneEstabelecimento=recebido.getStringExtra("TELEFONE");
                    editar.latlonEstabelecimento = new LatLng(recebido.getDoubleExtra("LATITUDE",0), recebido.getDoubleExtra("LONGITUDE",0));

                    //exibe na tela os dados recebidos
                    cbxLargura.setChecked(editar.larguraSuficiente);
                    cbxRampa.setChecked(editar.possuiRampa);
                    cbxAltura.setChecked(editar.alturaCerta);
                    cbxEstacionamento.setChecked(editar.possuiEstacionamento);
                    cbxBanheiro.setChecked(editar.possuiBanheiro);
                    txtFone.setText(editar.telefoneEstabelecimento);
                    String buscaEstado= codigosEstados[0];
                    for(int x=0;x<codigosEstados.length;x++) {
                        if (buscaEstado.equals(editar.estadoEstabelecimento))
                        {
                            spEstado.setSelection(x);
                            break;
                        }
                    }
                    txtCidade.setText(editar.cidadeEstabelecimento);
                    txtBairro.setText(editar.bairroEstabelecimento);
                    txtEndereco.setText(editar.enderecoEstabelecimento);
                    txtTitulo.setText(editar.nomeEstabelecimento);
                    rb.setProgress((int) editar.mediaEstrelasAtendimento);
                    clsCategorias buscaId;
                    for(int x=0;x<categoriasCarregadas.size();x++) {
                        buscaId=categoriasCarregadas.get(x);
                        if(buscaId.getIdCategoria()==editar.idCategoria) {
                            spCategorias.setSelection(x);
                            break;
                        }
                    }


        }
        else
        {
            //region sugere endereço
            Geocoder geocoder = new Geocoder(this);
            List<Address> addressList = null;
            LatLng local = clsApiClientSingleton.ultimoLocal(this);
            if (local == null) {
                gpsDesligado();
            } else {
                try {
                    addressList = geocoder.getFromLocation(local.latitude, local.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    String[] estados = getResources().getStringArray(R.array.valores_array_estados);

                    String percorreEstados;
                    for (int x = 0; x < estados.length; x++) {
                        percorreEstados = estados[x];
                        if (percorreEstados.equalsIgnoreCase(address.getAdminArea()))
                            spEstado.setSelection(x);
                    }
                    txtEndereco.setText(address.getAddressLine(0));
                    txtCidade.setText(address.getLocality());
                    txtBairro.setText(address.getSubLocality());
                }
            }
            //endregion
        }
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
            builder.setTitle(getResources().getString(R.string.localizacao_desativada));
            builder.setMessage(getResources().getString(R.string.mensagem_gps));
            //se o malandro pressionar fora do AlertDialog, fecha o aplicativo
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });

            builder.setPositiveButton(getResources().getString(R.string.sim), dialogClickListener).setNegativeButton(getResources().getString(R.string.nao), dialogClickListener);
            builder.create().show();
        }
    }

    public void salvaAvaliacao_Click(View view) {
        txtBairro.setError(null);
        txtCidade.setError(null);
        txtTitulo.setError(null);
        txtEndereco.setError(null);
        txtCidade.setError(null);

        if (txtTitulo.getText().toString().isEmpty()) {
            txtTitulo.setError(getResources().getString(R.string.campo_obrigatorio));
            txtTitulo.requestFocus();
        } else if (txtEndereco.getText().toString().isEmpty()) {
            txtEndereco.setError(getResources().getString(R.string.campo_obrigatorio));
            txtEndereco.requestFocus();
        } else if (txtCidade.getText().toString().isEmpty()) {
            txtCidade.setError(getResources().getString(R.string.campo_obrigatorio));
            txtCidade.requestFocus();
        } else if (txtBairro.getText().toString().isEmpty()) {
            txtBairro.setError(getResources().getString(R.string.campo_obrigatorio));
            txtBairro.requestFocus();
        } else if (txtCidade.getText().toString().matches("[0-9]*")) {
            txtCidade.setError(getResources().getString(R.string.campo_nao_permite_numeros));
            txtCidade.requestFocus();
        } else if (txtBairro.getText().toString().matches("[0-9]*")) {
            txtBairro.setError(getResources().getString(R.string.campo_nao_permite_numeros));
            txtBairro.requestFocus();
        }
        else {
            String selecionado = spCategorias.getSelectedItem().toString();
            int idCategoria = 0;
            for (clsCategorias percorre : categoriasCarregadas) {
                if (selecionado.equals(percorre.getNomeCategoria())) {
                    idCategoria = percorre.getIdCategoria();
                    break;
                }
            }
            //se a categoria não for encontrada, força o fechamento da activity e avisa o usuário
            if (idCategoria == 0) {
                Toast.makeText(this, getResources().getString(R.string.mensagem_erro_recuperar_categorias), Toast.LENGTH_LONG).show();
                finish();
            }
            SharedPreferences idUsuario = getSharedPreferences("USUARIO", MODE_PRIVATE);
            if(editar.idEstabelecimento>0)
            {
                editar.idCategoria=idCategoria;
                editar.nomeEstabelecimento=txtTitulo.getText().toString();
                editar.enderecoEstabelecimento=txtEndereco.getText().toString();
                editar.bairroEstabelecimento=txtBairro.getText().toString();
                editar.cidadeEstabelecimento = txtCidade.getText().toString();
                editar.estadoEstabelecimento=codigosEstados[spEstado.getSelectedItemPosition()];
                editar.possuiBanheiro=cbxBanheiro.isChecked();
                editar.alturaCerta=cbxAltura.isChecked();
                editar.possuiRampa=cbxRampa.isChecked();
                editar.larguraSuficiente=cbxLargura.isChecked();
                editar.possuiEstacionamento=cbxEstacionamento.isChecked();
                editar.telefoneEstabelecimento=txtFone.getText().toString();
                editar.mediaEstrelasAtendimento = rb.getProgress();
                editar.editaEstabelecimento(idUsuario.getInt("ID_USUARIO", 0), this);
            }
            else {
                clsEstabelecimentos novo;
                LatLng local = clsApiClientSingleton.ultimoLocal(this);
                if (local == null) {
                    gpsDesligado();
                } else {
                    novo = new clsEstabelecimentos(idCategoria, txtTitulo.getText().toString(), txtEndereco.getText().toString(), txtBairro.getText().toString(), txtCidade.getText().toString(), codigosEstados[spEstado.getSelectedItemPosition()], cbxBanheiro.isChecked(), cbxAltura.isChecked(), cbxRampa.isChecked(), cbxLargura.isChecked(), cbxEstacionamento.isChecked(), txtFone.getText().toString(), local, rb.getProgress());

                    novo.cadastraEstabelecimento(idUsuario.getInt("ID_USUARIO", 0), this);
                }
            }
            finish();
        }
    }
}
