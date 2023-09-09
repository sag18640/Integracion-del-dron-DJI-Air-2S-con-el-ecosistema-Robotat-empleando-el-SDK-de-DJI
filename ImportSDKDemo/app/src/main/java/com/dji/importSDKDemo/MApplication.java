package com.dji.importSDKDemo;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import androidx.multidex.MultiDex;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.BluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * La clase MApplication extiende la clase Application de Android y se utiliza para
 * mantener referencias globales a objetos importantes como productos DJI, conectores Bluetooth y metodos de verificacion
 */
public class MApplication extends Application {
    /**
     * TAG se utiliza para registrar información en LogCat.
     */
    public static final String TAG = MApplication.class.getName();
    /**
     * Representa el producto DJI que está conectado (si lo hay).
     */
    private static BaseProduct product;
    /**
     * Conector para productos DJI con Bluetooth.
     */
    private static BluetoothProductConnector bluetoothConnector = null;
    /**
     * Bus de eventos para comunicación entre componentes.
     */
    private static Bus bus = new Bus(ThreadEnforcer.ANY);
    /**
     * Referencia a la instancia de la aplicación.
     */
    private static Application app = null;
    /**
     * Este metodo obtiene el producto DJI, es decir, que dron esta conectado
     * @return Devuelve la instancia del producto DJI conectado.
     */
    public static synchronized BaseProduct getProductInstance() {
        product = DJISDKManager.getInstance().getProduct();
        return product;
    }
    /**
     * Este metodo obtiene el tipo de conector bluetooth del producto DJI
     * @return Devuelve el conector Bluetooth del producto DJI.
     */
    public static synchronized BluetoothProductConnector getBluetoothProductConnector() {
        bluetoothConnector = DJISDKManager.getInstance().getBluetoothProductConnector();
        return bluetoothConnector;
    }
    /**
     * Metodo de verificacion para coneccion entre dron-control-app
     * @return Verdadero si un Aircraft (dron) está conectado, falso en caso contrario.
     */
    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }
    /**
     * Metodo de verificacion para coneccion entre handled-app
     * @return Verdadero si un dispositivo HandHeld está conectado, falso en caso contrario.
     */
    public static boolean isHandHeldConnected() {
        return getProductInstance() != null && getProductInstance() instanceof HandHeld;
    }
    /**
     * Metodo para obtener la instancia del dron
     * @return Devuelve la instancia de Aircraft si está conectada, o null en caso contrario.
     */
    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }
    /**
     * Metodo para obtener la instancia del dispositivo handled
     * @return Devuelve la instancia de HandHeld si está conectada, o null en caso contrario.
     */
    public static synchronized HandHeld getHandHeldInstance() {
        if (!isHandHeldConnected()) {
            return null;
        }
        return (HandHeld) getProductInstance();
    }
    /**
     * Metodo para obtener la instancia de la aplicacion
     * @return Devuelve la instancia de la aplicación.
     */
    public static Application getInstance() {
        return MApplication.app;
    }
    /**
     * @return Devuelve el bus de eventos.
     */
    public static Bus getEventBus() {
        return bus;
    }
    /**
     * Método llamado cuando se crea el contexto de la aplicación.
     *
     * @param paramContext El contexto de la aplicación.
     */
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        MultiDex.install(this);
        com.secneo.sdk.Helper.install(this);
        app = this;
    }
}
