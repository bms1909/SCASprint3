package ulbra.bms.scaid5.controllers;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsApiClientSingleton;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsCategorias;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsEstabelecimentos;


public class CadastraEstabelecimentoActivity extends ActionBarActivity {

    private ArrayList<clsCategorias> categoriasCarregadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastra_estabelecimento);

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
        Spinner spCategorias;
        aOpcoes = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nomesCategoria);
// capturando o spinner do xml pela id
        spCategorias = (Spinner) findViewById(R.id.sp_novoestabelecimento_categorias);
        spCategorias.setAdapter(aOpcoes);

        //endregion
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cadastra_estabelecimento, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void salvaAvaliacao_Click(View view) {

        Spinner spCategorias = (Spinner) findViewById(R.id.sp_novoestabelecimento_categorias);
        RatingBar rb = (RatingBar) findViewById(R.id.rb_novoestabelecimento_classificacao);
        EditText txtTitulo = (EditText) findViewById(R.id.txt_novoestabelecimento_nome);
        EditText txtEndereco = (EditText) findViewById(R.id.txt_novoestabelecimento_endereco);
        EditText txtCidade = (EditText) findViewById(R.id.txt_novoestabelecimento_cidade);
        EditText txtFone = (EditText) findViewById(R.id.txt_novoestabelecimento_telefone);
        CheckBox cbxBanheiro = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_possui_banheiro);
        CheckBox cbxEstacionamento = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_possui_estacionamento);
        CheckBox cbxAltura = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_altura_certa);
        CheckBox cbxRampa = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_possui_rampa);
        CheckBox cbxLargura = (CheckBox) findViewById(R.id.cbx_novoestabelecimento_largura_suficiente);
        if (txtTitulo.getText().toString().isEmpty())
            txtTitulo.setError("Campo Obrigatório!");
        else if (txtEndereco.getText().toString().isEmpty())
            txtEndereco.setError("Campo Obrigatório!");
        else if (txtCidade.getText().toString().isEmpty())
            txtCidade.setError("Campo Obrigatório!");
        else if (txtCidade.getText().toString().matches("[0-9]*"))
            txtCidade.setError("Campo não permite números!");
        else if (txtFone.getText().toString().isEmpty())
            txtFone.setError("Campo Obrigatório!");
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
            clsEstabelecimentos novo;
            novo = new clsEstabelecimentos(idCategoria, txtTitulo.getText().toString(), txtEndereco.getText().toString(), txtCidade.getText().toString(), cbxBanheiro.isChecked(), cbxAltura.isChecked(), cbxRampa.isChecked(), cbxLargura.isChecked(), cbxEstacionamento.isChecked(), txtFone.getText().toString(), clsApiClientSingleton.ultimoLocal(this));
            novo.cadastraEstabelecimento(rb.getNumStars(), this);
            Toast.makeText(this, "Estabelecimento Salvo, Obrigado!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
