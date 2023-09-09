package com.dji.importSDKDemo;

import android.os.Bundle;
import android.app.Activity;
/**
 * Actividad principal que inicializa la vista personalizada "vuelo".
 */
public class VueloActivity extends Activity {
    /**
     * Método llamado al crear la actividad.
     *
     * @param savedInstanceState Un objeto Bundle que contiene el estado guardado de la actividad, si es que existe.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vuelo myView = new vuelo(this);
        setContentView(myView);
    }
}

