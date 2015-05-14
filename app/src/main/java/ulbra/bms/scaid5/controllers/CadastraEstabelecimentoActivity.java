package ulbra.bms.scaid5.controllers;

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

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.models.clsApiClientSingleton;
import ulbra.bms.scaid5.models.clsCategorias;
import ulbra.bms.scaid5.models.clsEstabelecimentos;


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
        categoriasCarregadas = clsCategorias.carregaCategorias();
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
        editar = new clsEstabelecimentos(recebido.getIntExtra("ID_ESTABELECIMENTO", 0));
        if(editar.idEstabelecimento>0) {
            editar=editar.carregaDetalhesEstabelecimento();
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

    public void salvaAvaliacao_Click(View view) {

        if (txtTitulo.getText().toString().isEmpty())
            txtTitulo.setError("Campo Obrigatório!");
        else if (txtEndereco.getText().toString().isEmpty())
            txtEndereco.setError("Campo Obrigatório!");
        else if (txtCidade.getText().toString().isEmpty())
            txtCidade.setError("Campo Obrigatório!");
        else if(txtBairro.getText().toString().isEmpty())
            txtBairro.setError("Campo Obrigatório!");
        else if (txtCidade.getText().toString().matches("[0-9]*"))
            txtCidade.setError("Campo não permite números!");
        else if (txtBairro.getText().toString().matches("[0-9]*"))
            txtBairro.setError("Campo não permite números!");
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
                Toast.makeText(this, "Erro ao recuperar Categorias, por favor, refaça a operação", Toast.LENGTH_LONG).show();
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
