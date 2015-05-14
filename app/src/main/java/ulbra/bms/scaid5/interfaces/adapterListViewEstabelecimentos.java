package ulbra.bms.scaid5.interfaces;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import ulbra.bms.scaid5.R;

/**
 * kibado por Bruno on 07/05/2015. de Arthur Lehdermann https://arthurlehdermann.wordpress.com/2013/03/11/android-criando-um-listview-personalizado/
 */
public class adapterListViewEstabelecimentos extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Map<String, String>> itens;

    public adapterListViewEstabelecimentos (Context context, List<Map<String, String>> itens)
    {
        //Itens que preencheram o listview
        this.itens = itens;
        //responsavel por pegar o Layout do item.
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return itens.size();
    }

    @Override
    public Object getItem(int position) {
        return itens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Pega o item de acordo com a posicao.
        Map<String, String> item = itens.get(position);
        //infla o layout para podermos preencher os dados
        convertView = mInflater.inflate(R.layout.layout_lista_pesquisas, null);
        //atraves do layout pego pelo LayoutInflater, pegamos cada id relacionado
        //ao item e definimos as informacoes.
        ((TextView) convertView.findViewById(R.id.tv_layoutlista_linha1)).setText(item.get("linha2"));
        ((TextView) convertView.findViewById(R.id.tv_layoutlista_estrelas)).setText(item.get("linha3"));
        ((TextView) convertView.findViewById(R.id.tv_layoutlista_linha2)).setText(item.get("linha4"));
        ((TextView) convertView.findViewById(R.id.tv_layoutlista_distancia)).setText(item.get("linha5"));
        //e=estabelecimento
        //c=categoria
        //numeros==endereco
        if(item.get("linha0").equals("e"))
        {
            (convertView.findViewById(R.id.tv_layoutlista_km)).setVisibility(View.VISIBLE);
            (convertView.findViewById(R.id.img_layoutlista_estrela)).setVisibility(View.VISIBLE);
        }
        else if(item.get("linha0").equals("c"))
        {
            (convertView.findViewById(R.id.tv_layoutlista_km)).setVisibility(View.INVISIBLE);
            (convertView.findViewById(R.id.img_layoutlista_estrela)).setVisibility(View.INVISIBLE);
        }
        else
        {
            (convertView.findViewById(R.id.img_layoutlista_estrela)).setVisibility(View.INVISIBLE);
        }
        return convertView;
    }
}
