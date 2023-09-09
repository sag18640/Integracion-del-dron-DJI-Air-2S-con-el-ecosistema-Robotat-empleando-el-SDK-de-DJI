package com.dji.importSDKDemo;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.dji.importSDKDemo.R;
import dji.common.error.DJIError;

/**
 * @author DJI
 * Created by dji on 2/3/16.
 */
/**
 * Esta es la clase Dialogos
 * <p>
 * utilizada para mostrar ventanas de dialogo en la app.
 * </p>
 */
public class Dialogos {
    /**
     * Metodo showDialog
     * TODO: Este es el método utilizado para mostrar mensajes en la aplicación como una ventana emergente
     *
     * @param ctx Context
     * @param str Mensaje mostrado en la ventana emergente
     */
    public static void showDialog(Context ctx, String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.set_dialog);
        builder.setMessage(str);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            /**
             * Este es el método onClick.
             * <p>
             * TODO: Este método tiene como funcionamiento que una vez se presione el boton ok se cierre el dialogo.
             * </p>
             *
             * @param dialog Interface de dialogo
             */
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Metodo showDialog
     * TODO: Este es el método utilizado para mostrar mensajes en la aplicación como una ventana emergente
     *
     * @param ctx   Context
     * @param strId Este parametro es un ID de recurso que apunta a una cadena localizada
     */
    public static void showDialog(Context ctx, int strId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.set_dialog);
        builder.setMessage(strId);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            /**
             * Este es el método onClick.
             * <p>
             * TODO: Este método tiene como funcionamiento que una vez se presione el boton ok se cierre el dialogo.
             * </p>
             *
             * @param dialog Interface de dialogo
             */
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Metodo showConfirmationDialog
     * TODO: Este es el método para mostrar mensajes en la aplicación como una ventana emergente
     * @param ctx contexto
     * @param strId Este parametro es un ID de recurso que apunta a una cadena localizada
     * @param onClickListener Objeto que implementa la interfaz DialogInterface.OnClickListener. Esta interfaz tiene un método abstracto llamado onClick que se debe implementar para definir lo que sucede cuando se hace clic en un botón del cuadro de diálogo.
     */
    public static void showConfirmationDialog(Context ctx, int strId,
                                              DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.set_dialog);
        builder.setMessage(strId);
        builder.setPositiveButton(android.R.string.ok, onClickListener);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            /**
             * Este es el método onClick.
             * <p>
             * TODO: Este método tiene como funcionamiento que una vez se presione el boton cancel se cierre el dialogo.
             * </p>
             *
             * @param dialog Interface de dialogo
             */
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * showConfirmationDialog
     * TODO: Este metodo es utilizado para mostrar en una ventana emergente el mensaje de error ingresado
     * @param ctx Contexto
     * @param djiError Mensaje de error a mostrar
     */
    public static void showDialogBasedOnError(Context ctx, DJIError djiError) {
        if (null == djiError) {
            showDialog(ctx, R.string.success);
        } else {
            showDialog(ctx, djiError.getDescription());
        }
    }
}
