package ulbra.bms.sca.interfaces;

import java.util.ArrayList;

import ulbra.bms.sca.models.clsEstabelecimentos;

/**
 * Criado por Bruno on 27/04/2015.
 */
public interface estabelecimentosCarregadosListener {
    void estabelecimentosCarregados(ArrayList<clsEstabelecimentos> estabelecimentos);
}
