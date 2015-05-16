package ulbra.bms.sca.controllers;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import ulbra.bms.sca.R;
import ulbra.bms.sca.models.clsCategorias;
import ulbra.bms.sca.models.clsEstabelecimentos;


public class DetalhesEstabelecimentoActivity extends ActionBarActivity {

    private clsEstabelecimentos estabCarregado;
    private RatingBar rb;
    private boolean jaAvaliado;

    //carrega todas as informações do estabelecimento a partir do objeto global
    private void atualizaTela() {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(clsCategorias.getNomeCategoria(estabCarregado.idCategoria));

        TextView tvTitulo = (TextView) findViewById(R.id.tv_estabelecimento_nome);
        tvTitulo.setText(estabCarregado.nomeEstabelecimento);
        TextView tvEndereco = (TextView) findViewById(R.id.tv_estabelecimento_endereco);
        tvEndereco.setText(estabCarregado.enderecoEstabelecimento);
        TextView bairro = (TextView) findViewById(R.id.tv_estabelecimento_bairro);
        bairro.setText(estabCarregado.bairroEstabelecimento);
        TextView tvCidade = (TextView) findViewById(R.id.tv_estabelecimento_cidade);
        tvCidade.setText(estabCarregado.cidadeEstabelecimento);
        TextView estado = (TextView) findViewById(R.id.tv_estabelecimento_estado);
        estado.setText(estabCarregado.estadoEstabelecimento);
        TextView tvFone = (TextView) findViewById(R.id.tv_estabelecimento_telefone);
        tvFone.setText(estabCarregado.telefoneEstabelecimento);

        rb = (RatingBar) findViewById(R.id.rb_estabelecimento_classificacao);
        rb.setRating(estabCarregado.mediaEstrelasAtendimento);


        CheckBox cbxBanheiro = (CheckBox) findViewById(R.id.cbx_estabelecimento_possui_banheiro);

        cbxBanheiro.setChecked(estabCarregado.possuiBanheiro);
        CheckBox cbxEstacionamento = (CheckBox) findViewById(R.id.cbx_estabelecimento_possui_estacionamento);
        cbxEstacionamento.setChecked(estabCarregado.possuiEstacionamento);
        CheckBox cbxAltura = (CheckBox) findViewById(R.id.cbx_estabelecimento_altura_certa);
        cbxAltura.setChecked(estabCarregado.alturaCerta);
        CheckBox cbxRampa = (CheckBox) findViewById(R.id.cbx_estabelecimento_possui_rampa);
        cbxRampa.setChecked(estabCarregado.possuiRampa);
        CheckBox cbxLargura = (CheckBox) findViewById(R.id.cbx_estabelecimento_largura_suficiente);
        cbxLargura.setChecked(estabCarregado.larguraSuficiente);
    }

    public void salvaAvaliacao_Click(View a) {
        // envia ao WS a avaliação do estabelecimento
        rb = (RatingBar) findViewById(R.id.rb_estabelecimento_classificacao);

        SharedPreferences id = getSharedPreferences("USUARIO",MODE_PRIVATE);
        estabCarregado.avaliaEstabelecimento(rb.getProgress(), id.getInt("ID_USUARIO",0), this);
        finish();
    }
    @Override
    protected void onResume()
    {
        //tarefa sincrona pois o inicio da activity depende desses dados
        estabCarregado=estabCarregado.carregaDetalhesEstabelecimento();
        atualizaTela();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_estabelecimento);
        //recebe os dados da outra página e carrega o objeto local
        Intent recebido = getIntent();
        estabCarregado = new clsEstabelecimentos(recebido.getIntExtra("ID_ESTABELECIMENTO", 0));


        rb = (RatingBar) findViewById(R.id.rb_estabelecimento_classificacao);
        //escala até 5
        rb.setMax(5);

        //listener de pressionamento das estrelas de avaliação, quando acionado, altera altura da buttonBar e exibe botão "avaliar"
        rb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final LinearLayout btnBar = (LinearLayout) findViewById(R.id.btnbar_estabelecimento);
                    final ViewGroup.LayoutParams a = btnBar.getLayoutParams();
                    a.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    if ((jaAvaliado) && (btnBar.getHeight() == 0)) {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(DetalhesEstabelecimentoActivity.this);
                        dlgAlert.setTitle("Confirmação");
                        dlgAlert.setMessage("Você já avaliou este Estabelecimento, deseja alterar?");
                        dlgAlert.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //inicia tela de detalhes do estabelecimento, enviado o ID do mesmo via putExtra
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    btnBar.setLayoutParams(a);
                                }
                            }
                        });
                        dlgAlert.setNegativeButton("Não", null);
                        dlgAlert.create().show();
                    } else {
                        btnBar.setLayoutParams(a);
                    }
                }
                return false;
            }
        });
        SharedPreferences id = getSharedPreferences("USUARIO",MODE_PRIVATE);
        //recupera do webservice se usuario ja avaliou o estabelecimento
        jaAvaliado = clsEstabelecimentos.estabelecimentoFoiAvaliado(id.getInt("ID_USUARIO",0), estabCarregado.idEstabelecimento);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // executado ao criar o menu, o código abaixo confere se há algum app que responda a intent, se não houver,
        // remove o botão correspondente a ação
        getMenuInflater().inflate(R.menu.menu_detalhes_estabelecimento, menu);
        //confere se existe algum aplicativo de GPS instalado
        final PackageManager mgr = this.getPackageManager();
        List<ResolveInfo> list =
                mgr.queryIntentActivities(new Intent(Intent.ACTION_VIEW, Uri.parse("geo: -29.331124, -49.751402")),
                        PackageManager.MATCH_DEFAULT_ONLY);
        //se nao houver, remove o botao
        if (list.size() == 0)
            menu.removeItem(R.id.btnEstabelecimentoAbrirMaps);
        //confere se existe algum aplicativo de discador instalado
        list = mgr.queryIntentActivities(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:9999")),
                PackageManager.MATCH_DEFAULT_ONLY);
        //se nao houver, remove o botao
        if (list.size() == 0||estabCarregado.telefoneEstabelecimento.equals(""))
            menu.removeItem(R.id.btnEstabelecimentoTelefone);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //tratamento de botões na activityBar
        switch (item.getItemId()) {
            case R.id.btnEstabelecimentoAbrirMaps:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + estabCarregado.latlonEstabelecimento.latitude + "," + estabCarregado.latlonEstabelecimento.longitude)));
                break;
            case R.id.btnEstabelecimentoTelefone:
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + estabCarregado.telefoneEstabelecimento)));
                break;
            case R.id.btnEstabelecimentoEditar:
                startActivity(new Intent(DetalhesEstabelecimentoActivity.this, CadastraEstabelecimentoActivity.class).putExtra("ID_ESTABELECIMENTO", estabCarregado.idEstabelecimento));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
