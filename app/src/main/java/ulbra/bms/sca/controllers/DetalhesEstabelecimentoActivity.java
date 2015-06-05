package ulbra.bms.sca.controllers;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import ulbra.bms.sca.interfaces.booleanRetornadoListener;
import ulbra.bms.sca.interfaces.detalhesEstabelecimentoCarregadoListener;
import ulbra.bms.sca.models.clsCategorias;
import ulbra.bms.sca.models.clsEstabelecimentos;


public class DetalhesEstabelecimentoActivity extends ActionBarActivity {

    private clsEstabelecimentos estabCarregado;
    private RatingBar rb;
    private boolean jaAvaliado;
    private Menu actionBarMenu;

    //carrega todas as informações do estabelecimento a partir do objeto global
    private void atualizaTela() {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(clsCategorias.getNomeCategoria(estabCarregado.idCategoria, this));

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

        SharedPreferences id = getSharedPreferences("USUARIO", MODE_PRIVATE);
        estabCarregado.avaliaEstabelecimento(rb.getProgress(), id.getInt("ID_USUARIO", 0), this);
        finish();
    }

    private void showProgress(final boolean show) {
        //mesmo sistema de animacao da loginActivity, fornecido automaticamente ao criar tela de login
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        final View progresso = findViewById(R.id.pb_estabelecimento);
        progresso.setVisibility(show ? View.VISIBLE : View.GONE);
        progresso.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progresso.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
        View scrollView = findViewById(R.id.sv_estabelecimento);
        if (show)
            scrollView.setVisibility(View.GONE);
        else
            scrollView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume()
    {
        showProgress(true);
        estabCarregado.carregaDetalhesEstabelecimento(new detalhesEstabelecimentoCarregadoListener() {
            @Override
            public void estabelecimentoCarregado(clsEstabelecimentos estabelecimento) {
                estabCarregado = estabelecimento;
                //remove botao de telefone se nao houver telefone cadastrado
                if ((estabCarregado.telefoneEstabelecimento.equals("")) && actionBarMenu != null) {
                    actionBarMenu.removeItem(R.id.btnEstabelecimentoTelefone);
                }
                atualizaTela();
                showProgress(false);
            }
        }, this);
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_estabelecimento);

        //recebe os dados da outra pagina e carrega o objeto local
        Intent recebido = getIntent();
        estabCarregado = new clsEstabelecimentos(recebido.getIntExtra("ID_ESTABELECIMENTO", 0));

        SharedPreferences id = getSharedPreferences("USUARIO", MODE_PRIVATE);
        //recupera do webservice se usuario ja avaliou o estabelecimento
        clsEstabelecimentos.estabelecimentoFoiAvaliado(new booleanRetornadoListener() {
            @Override
            public void booleanRetornado(boolean retorno) {
                jaAvaliado = retorno;
            }
        }, id.getInt("ID_USUARIO", 0), estabCarregado.idEstabelecimento, this);


        rb = (RatingBar) findViewById(R.id.rb_estabelecimento_classificacao);
        //escala até 5
        rb.setMax(5);

        //listener de pressionamento das estrelas de avaliacao, quando acionado, altera altura da buttonBar e exibe botao "avaliar"
        rb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final LinearLayout btnBar = (LinearLayout) findViewById(R.id.btnbar_estabelecimento);
                    final ViewGroup.LayoutParams a = btnBar.getLayoutParams();
                    a.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    if ((jaAvaliado) && (btnBar.getHeight() == 0)) {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(DetalhesEstabelecimentoActivity.this);
                        dlgAlert.setTitle(getResources().getString(R.string.confirmacao));
                        dlgAlert.setMessage(getResources().getString(R.string.detalhes_ja_avaliou_confirma));
                        dlgAlert.setPositiveButton(getResources().getString(R.string.sim), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //inicia tela de detalhes do estabelecimento, enviado o ID do mesmo via putExtra
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    btnBar.setLayoutParams(a);
                                }
                            }
                        });
                        dlgAlert.setNegativeButton(getResources().getString(R.string.nao), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_NEGATIVE) {
                                    rb.setRating(estabCarregado.mediaEstrelasAtendimento);
                                }
                            }
                        });
                        dlgAlert.create().show();
                    } else {
                        btnBar.setLayoutParams(a);
                    }
                }
                return false;
            }
        });
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
        if (list.size() == 0)
            menu.removeItem(R.id.btnEstabelecimentoTelefone);
        actionBarMenu = menu;
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
                startActivity(new Intent(DetalhesEstabelecimentoActivity.this, CadastraEstabelecimentoActivity.class)
                                .putExtra("ID_ESTABELECIMENTO", estabCarregado.idEstabelecimento)
                                .putExtra("ID_CATEGORIA", estabCarregado.idCategoria)
                                .putExtra("NOME", estabCarregado.nomeEstabelecimento)
                                .putExtra("ENDERECO", estabCarregado.enderecoEstabelecimento)
                                .putExtra("BAIRRO", estabCarregado.bairroEstabelecimento)
                                .putExtra("CIDADE", estabCarregado.cidadeEstabelecimento)
                                .putExtra("ESTADO", estabCarregado.estadoEstabelecimento)
                                .putExtra("ESTRELAS", estabCarregado.mediaEstrelasAtendimento)
                                .putExtra("BANHEIRO", estabCarregado.possuiBanheiro)
                                .putExtra("ESTACIONAMENTO", estabCarregado.possuiEstacionamento)
                                .putExtra("ALTURA", estabCarregado.alturaCerta)
                                .putExtra("RAMPA", estabCarregado.possuiRampa)
                                .putExtra("LARGURA", estabCarregado.larguraSuficiente)
                                .putExtra("TELEFONE", estabCarregado.telefoneEstabelecimento)
                                .putExtra("LATITUDE", estabCarregado.latlonEstabelecimento.latitude)
                                .putExtra("LONGITUDE", estabCarregado.latlonEstabelecimento.longitude)
                );

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
