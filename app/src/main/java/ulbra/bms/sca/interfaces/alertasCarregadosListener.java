package ulbra.bms.sca.interfaces;

import java.util.ArrayList;

import ulbra.bms.sca.models.clsAlertas;

/**
 * Criado por Bruno on 27/04/2015.
 */
public interface alertasCarregadosListener {
    void alertasCarregados(ArrayList<clsAlertas> alertas);
}
