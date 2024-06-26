package com.dji.importSDKDemo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;
import android.widget.TextView;
import android.widget.Toast;
import com.dji.importSDKDemo.MApplication;
/**
 * Clase de utilidad para mostrar mensajes Toast y actualizar TextViews.
 */
public class ToastUtils {
    /**
     * Código de mensaje para actualizar un TextView.
     */
    private static final int MESSAGE_UPDATE = 1;
    /**
     * Código de mensaje para mostrar un Toast.
     */
    private static final int MESSAGE_TOAST = 2;
    /**
     * Manejador para ejecutar tareas en el hilo principal de la interfaz de usuario.
     */
    private static Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //Get the message string
            switch ((msg.what)) {
                case MESSAGE_UPDATE:
                    showMessage((Pair<TextView, String>) msg.obj);
                    break;
                case MESSAGE_TOAST:
                    showToast((String) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    /**
     * Actualiza un TextView con un mensaje dado.
     *
     * @param msg Un Pair que contiene el TextView a actualizar y el mensaje a mostrar.
     */
    private static void showMessage(Pair<TextView, String> msg) {
        if (msg != null) {
            if (msg.first == null) {
                Toast.makeText(MApplication.getInstance(), "tv is null", Toast.LENGTH_SHORT).show();
            } else {
                msg.first.setText(msg.second);
            }
        }
    }
    /**
     * Muestra un Toast con un mensaje dado.
     *
     * @param msg El mensaje a mostrar en el Toast.
     */
    public static void showToast(String msg) {
        Toast.makeText(MApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }
    /**
     * Toma un mensaje Toast para ser mostrado.
     *
     * @param string El mensaje a mostrar en el Toast.
     */
    public static void setResultToToast(final String string) {
        Message msg = new Message();
        msg.what = MESSAGE_TOAST;
        msg.obj = string;
        mUIHandler.sendMessage(msg);
    }
    /**
     * Toma un TextView para ser actualizado con un mensaje dado.
     *
     * @param tv El TextView a actualizar.
     * @param s El mensaje a mostrar en el TextView.
     */
    public static void setResultToText(final TextView tv, final String s) {
        Message msg = new Message();
        msg.what = MESSAGE_UPDATE;
        msg.obj = new Pair<>(tv, s);
        mUIHandler.sendMessage(msg);
    }
}
