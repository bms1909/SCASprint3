package ulbra.bms.scaid5.controllers;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsEstabelecimentos;


public class DetalhesEstabelecimentoActivity extends ActionBarActivity {

    private clsEstabelecimentos estabCarregado;

    private void atualizaTela()
    {
        TextView txtTitulo = (TextView) findViewById(R.id.txt_estabelecimento_nome);
        txtTitulo.setText(estabCarregado.nomeEstabelecimento);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_estabelecimento);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Intent recebido = getIntent();
        estabCarregado = new clsEstabelecimentos(recebido.getIntExtra("ID_ESTABELECIMENTO",0));

        estabCarregado.carregaDetalhesEstabelecimento();
        atualizaTela();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detalhes_estabelecimento, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.btnEstabelecimentoAbrirMaps:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+estabCarregado.latlonEstabelecimento.latitude+","+estabCarregado.latlonEstabelecimento.longitude)));
                break;
            case R.id.btnEstabelecimentoTelefone:
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+estabCarregado.telefoneEstabelecimento)));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detalhes_estabelecimento, container, false);
            return rootView;
        }
    }
}
