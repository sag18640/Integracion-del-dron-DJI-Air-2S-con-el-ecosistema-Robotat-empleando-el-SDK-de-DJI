package com.dji.importSDKDemo;

import android.content.SharedPreferences;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.realname.AppActivationState;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.keysdk.DJIKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.KeyListener;
import dji.log.DJILog;
import dji.log.GlobalConfig;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.realname.AppActivationManager;
import dji.sdk.sdkmanager.BluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LDMModule;
import dji.sdk.sdkmanager.LDMModuleType;
import dji.sdk.useraccount.UserAccountManager;


import android.widget.Button;

/**
 * MainActivity es la clase que sirve como punto de entrada para la aplicación.
 * Esta clase se encarga de la inicialización de la SDK de DJI, solicitar permisos y gestionar la actividad principal de la aplicación.
 *
 * @author Cristopher Sagastume
 * @version 1.0
 * @since 2023-09-09
 */
public class MainActivity extends AppCompatActivity {
    /**
     * TAG se utiliza para registrar información en LogCat.
     */
    private static final String TAG = MainActivity.class.getName();
    /**
     * FLAG_CONNECTION_CHANGE se utiliza para guardar un texto informativo.
     */
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    /**
     * mProduct representa el producto DJI que está conectado (si lo hay).
     */
    private static BaseProduct mProduct;
    /**
     * mHandler es un manejador para enviar y procesar objetos Message y Runnable.
     */
    private Handler mHandler;
    /**
     * Botón que lleva al menú de la aplicación.
     */
    private Button menu;
    /**
     * Variable booleana que indica si el registro de la SDK fue exitoso.
     */
    private boolean registrationSuccess = false;
    /**
     * Lista de permisos requeridos para la aplicación.
     */
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.FOREGROUND_SERVICE,
    };

    /**
     * Lista de permisos que faltan y que la aplicación solicitará.
     */
    private final List<String> missingPermission = new ArrayList<>();

    /**
     * Variable para controlar el estado del proceso de registro.
     */
    private final AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);

    /**
     * Código de solicitud para solicitar permisos.
     */
    private static final int REQUEST_PERMISSION_CODE = 12345;
    /**
     * Manejador para enviar y procesar objetos Message y Runnable.
     */
    private Handler mHander = new Handler();

    /**
     * Variable para almacenar el último estado del proceso de descarga de la base de datos.
     */
    private int lastProcess = -1;

    /**
     * Botón para iniciar el proceso de registro.
     */
    private Button Register;
    /**
     * mDJIComponentListener es un oyente que se activa cuando cambia la conectividad de un componente de DJI.
     */
    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {
        /**
         * Este método se llama cuando cambia el estado de la conectividad de un componente.
         *
         * @param isConnected Un valor booleano que indica si el componente está conectado.
         */
        @Override
        public void onConnectivityChange(boolean isConnected) {
            Log.e(TAG, "onComponentConnectivityChanged: " + isConnected);
            notifyStatusChange();
        }
    };

    /**
     * El método connectToServer se encarga de establecer una conexión TCP con un servidor.
     * Este método crea un nuevo hilo para manejar la conexión de red, intenta conectar al servidor con una dirección IP y puerto específicos,
     * envía un mensaje al servidor y luego espera una respuesta. Finalmente, muestra la respuesta del servidor.
     */
    private void connectToServer() {
        new Thread(new Runnable() {
            /**
             * La lógica para establecer la conexión TCP y comunicarse con el servidor se coloca dentro del método run del nuevo hilo.
             */
            @Override
            public void run() {
                try {
                    // Cambia a la dirección IP de tu servidor
                    String serverIP = "192.168.0.3";
                    // Cambia al puerto que estés utilizando en tu servidor
                    int serverPort = 10000;

                    // Iniciando el socket
                    Socket socket = new Socket(serverIP, serverPort);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    //out.println("Hola, servidor!");

                    // Recibiendo la respuesta del servidor
                    final String response = in.readLine();
                    Log.w(TAG,"COMUNICACION TCP: "+response);
                    // Mostrando la respuesta en la interfaz de usuario
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Respuesta del servidor: " + response);
                        }

                    });

                    // Cerrando el socket
                    socket.close();
                } catch (Exception e) {
                    // Manejo de excepciones
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * Este método se ejecuta cuando se crea la actividad.
     * Inicializa la interfaz de usuario y la lógica de negocio de la aplicación.
     *
     * @param savedInstanceState Estado guardado de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button register = (Button) findViewById(R.id.buttonAct);
        menu = (Button) findViewById(R.id.buttonMenu);
        menu.setEnabled(false);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndRequestPermissions();
                Log.e("MyApp","PASABLEEEEE " + registrationSuccess);
                //connectToServer();

                if (registrationSuccess) {
                    menu.setEnabled(true);
                } else {
                    menu.setEnabled(false);
                }

            }

        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VueloActivity.class);
                startActivity(intent);

            }
        });


        //Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Este método revisa y solicita los permisos necesarios para la aplicación.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        List<String> missingPermission = new ArrayList<>();
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Este método se llama cuando se obtienen los resultados de las solicitudes de permisos.
     *
     * @param requestCode Código de solicitud de permiso.
     * @param permissions Array de permisos solicitados.
     * @param grantResults Resultados de las solicitudes de permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }
    /**
     * Este método inicia el proceso de registro de la SDK.
     */
    private void startSDKRegistration() {

        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.setResultToToast(MainActivity.this.getString(R.string.sdk_registration_doing_message));


                    //if we hope the Firmware Upgrade module could access the network under LDM mode, we need call the setModuleNetworkServiceEnabled()
                    //method before the registerAppForLDM() method

                    DJISDKManager.getInstance().getLDMManager().setModuleNetworkServiceEnabled(new LDMModule.Builder().moduleType(
                                LDMModuleType.FIRMWARE_UPGRADE).enabled(false).build());


                    DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                            /**
                             * Se llama cuando se completa el registro de la aplicación.
                             *
                             * @param djiError El resultado del proceso de registro.
                             */
                            @Override
                            public void onRegister(DJIError djiError) {
                                if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                    DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                                    DJISDKManager.getInstance().startConnectionToProduct();
                                    ToastUtils.setResultToToast(MainActivity.this.getString(R.string.sdk_registration_success_message));

                                    registrationSuccess = true;
                                    registrationSuccess = true;
                                    menu.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            menu.setEnabled(true);
                                        }
                                    });

                                } else {
                                    ToastUtils.setResultToToast(MainActivity.this.getString(R.string.sdk_registration_message) + djiError.getDescription());

                                    registrationSuccess = false;
                                    menu.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            menu.setEnabled(false);
                                        }
                                    });
                                }
                                Log.v(TAG, djiError.getDescription());

                            }
                            /**
                             * Se llama cuando el producto se desconecta.
                             */
                            @Override
                            public void onProductDisconnect() {
                                Log.d(TAG, "onProductDisconnect");
                                notifyStatusChange();
                            }
                            /**
                             * Se llama cuando un nuevo producto se conecta.
                             *
                             * @param baseProduct El producto que se ha conectado.
                             */
                            @Override
                            public void onProductConnect(BaseProduct baseProduct) {
                                Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                                notifyStatusChange();
                            }
                            /**
                             * Se llama cuando un producto cambia (por ejemplo, un componente se añade o se elimina).
                             *
                             * @param baseProduct El producto que ha cambiado.
                             */
                            @Override
                            public void onProductChanged(BaseProduct baseProduct) {
                                notifyStatusChange();
                            }
                            /**
                             * Se llama cuando un componente del producto cambia.
                             *
                             * @param componentKey La clave del componente que ha cambiado.
                             * @param oldComponent El componente antiguo.
                             * @param newComponent El nuevo componente.
                             */
                            @Override
                            public void onComponentChange(BaseProduct.ComponentKey componentKey,
                                                          BaseComponent oldComponent,
                                                          BaseComponent newComponent) {
                                if (newComponent != null) {
                                    newComponent.setComponentListener(mDJIComponentListener);

                                    if(componentKey == BaseProduct.ComponentKey.FLIGHT_CONTROLLER)
                                    {
                                        showDBVersion();
                                    }
                                }
                                Log.d(TAG,
                                        String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                                componentKey,
                                                oldComponent,
                                                newComponent));

                                notifyStatusChange();
                            }
                            /**
                             * Se llama durante el proceso de inicialización de la SDK.
                             *
                             * @param djisdkInitEvent El evento de inicialización.
                             * @param i Información adicional sobre el proceso.
                             */
                            @Override
                            public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                            }
                            /**
                             * Se llama para actualizar el progreso de la descarga de la base de datos.
                             *
                             * @param current El número actual de bytes descargados.
                             * @param total El número total de bytes a descargar.
                             */
                            @Override
                            public void onDatabaseDownloadProgress(long current, long total) {
                                int process = (int) (100 * current / total);
                                if (process == lastProcess) {
                                    return;
                                }
                                lastProcess = process;

                                if (process % 25 == 0){
                                    ToastUtils.setResultToToast("DB load process : " + process);
                                }else if (process == 0){
                                    ToastUtils.setResultToToast("DB load begin");
                                }
                            }
                        });

                    }

            });
        }
    }
    /**
     * Este método notifica cambios de estado en la conexión del producto.
     */
    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }
    /**
     * updateRunnable es un objeto Runnable que se encarga de enviar un broadcast con un Intent.
     * El Intent lleva una bandera FLAG_CONNECTION_CHANGE para notificar cambios en la conexión.
     */
    private final Runnable updateRunnable = () -> {
        Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
        sendBroadcast(intent);
    };
    /**
     * Este método muestra un mensaje de "toast" en la pantalla.
     *
     * @param toastMsg El mensaje a mostrar.
     */
    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show());

    }
    /**
     * Este método muestra la versión de la base de datos.
     */
    private void showDBVersion(){
        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                DJISDKManager.getInstance().getFlyZoneManager().getPreciseDatabaseVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String s) {
                        ToastUtils.setResultToToast("db load success ! version : " + s);
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        ToastUtils.setResultToToast("db load failure ! get version error : " + djiError.getDescription());

                    }
                });
            }
        },3000);
    }

}