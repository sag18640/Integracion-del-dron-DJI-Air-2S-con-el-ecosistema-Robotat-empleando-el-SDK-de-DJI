package com.dji.importSDKDemo;
import com.dji.importSDKDemo.MApplication;
import com.dji.importSDKDemo.utils.OnScreenJoystick;
import com.dji.importSDKDemo.utils.OnScreenJoystickListener;
import com.dji.importSDKDemo.ToastUtils;
import com.dji.importSDKDemo.loggerr;

import dji.common.error.DJIError;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.Attitude;
import dji.common.model.LocationCoordinate2D;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;
//import dji.sdk.mission.timeline.actions;


import dji.sdk.flightcontroller.Simulator;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.Activity;
import android.os.AsyncTask;



import androidx.annotation.NonNull;

//Comunicacion TCP
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import org.json.JSONObject;

/**
 * La clase vuelo representa el panel de control para operar el dron integrando distintas verificaciones de componentes visuales como, botones, joysticks,etc
 * tambien proporciona una interfaz de usuario para controlar el vuelo, incluido el despegue, el aterrizaje y el control direccional
 *
 * @author Cristopher Sagastume
 * @version 1.0
 * @since 2023-09-09
 */
public class vuelo extends RelativeLayout implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * Boton para elevacion del dron
     */
    private Button buttonElev;
    private Button buttonSubirAuto;
    private Button buttonBajarAuto;
    /**
     * Boton para aterrizaje del dron
     */
    private Button buttonBajar;
    /**
     * Boton para habilitar los joysticks virtuales
     */
    private Button btnEnableVirtualStick;
    /**
     * Boton para deshabilitar los joysticks virtuales
     */
    private Button btnDisableVirtualStick;
    /**
     * Boton para habilitar el simulador
     */
    private ToggleButton btnSimulator;
    /**
     * Controlador de vuelo para interactuar con el dron.
     */
    private FlightController flightController = null;

    /**
     * Estado actual del controlador de vuelo.
     */
    private FlightControllerState flightState=null;
    /**
     * Coordenadas de altitud del dron.
     */
    private LocationCoordinate3D altitud=null;

    private loggerr logi;
    /**
     * Simulador para simular el vuelo del dron.
     */
    private Simulator simulator = null;
    /**
     * Indicador que muestra si el simulador está activo.
     */
    private boolean isSimulatorActived = false;
    /**
     * Etiqueta para registros de depuración.
     */
    private static final String TAG = "MyActivity";
    private static final String TAG2 = "MyActivity2";
    /**
     * Joystick derecho en pantalla para controlar el dron.
     */
    private OnScreenJoystick screenJoystickRight;
    /**
     * Joystick izquierdo en pantalla para controlar el dron.
     */
    private OnScreenJoystick screenJoystickLeft;

    private ServerSocket serverSocket;

    /**
     * Vista de texto para mostrar información.
     */
    private TextView textView;
    /**
     * Valor para el control de pitch del dron.
     */
    private float pitch;
    /**
     * Valor para el control de roll del dron.
     */
    private float roll;
    /**
     * Valor para el control de yaw del dron.
     */
    private float yaw;
    /**
     * Valor para el control de throttle del dron.
     */
    private float throttle;

    /**
     * Temporizador para enviar datos de control virtual al dron.
     */
    private Timer sendVirtualStickDataTimer;
    /**
     * Tarea para enviar datos de control virtual.
     */
    private SendVirtualStickDataTask sendVirtualStickDataTask;
    /**
     * Indicador que muestra si la tarea de datos del stick virtual está programada.
     */
    private boolean isVirtualStickDataTaskScheduled = false;
    private enum DesiredAction {
        NONE,
        ASCEND,
        DESCEND
    }
    private DesiredAction currentDesiredAction = DesiredAction.NONE;
    /**
     * Constructor que inicializa el contexto y configura la interfaz de usuario.
     * @param context Contexto de la aplicación.
     */
    public vuelo(Context context){
        super(context);
        init(context);
    }
    /**
     * Tarea asincrónica para manejar la comunicación TCP con el servidor.
     */
    private class TcpCommunicationTask extends AsyncTask<Void, Void, String> {
        private double param1;
        private double param2;
        private double param3;
        private double param4;

        /**
         * Constructor para inicializar los parámetros a enviar.
         * @param param1 Primer parámetro.
         * @param param2 Segundo parámetro.
         * @param param3 Tercer parámetro.
         * @param param4 Cuarto parámetro.
         */
        public TcpCommunicationTask(double param1, double param2, double param3, double param4) {
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
            this.param4 = param4;
        }

        protected String doInBackground(Void... voids) {
            String clientMessage = "";

            try {
                Log.e(TAG2, "Esperando conexión entrante...");
                Socket clientSocket = serverSocket.accept();
                Log.e(TAG2, "Conexión aceptada de: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);

                clientMessage = in.readLine();
                if (clientMessage.equals("si")) {
                    Log.e(TAG2, "El dron se movera hacia adelante");
                    //LocationCoordinate3D currentLocation = flightControllerState.getAircraftLocation();
                    //setAltitude(currentLocation,-119.0,2.0);
                }else if (clientMessage.equals("no")){
                    Log.e(TAG2, "El dron dejará de moverse hacia adelante");
                    //setAltitude(currentLocation,0,2.0)
                }else if (clientMessage.equals("rotate")){
                    Log.e(TAG2, "El dron iniciará a rotar");
                    //yaw = 20;
                    //setSafeAltitude(2.0);
                }else if (clientMessage.equals("stop_rotate")) {
                    //yaw = 0; // Detiene el giro
                    Log.e(TAG2,"El dron dejará de rotar");
                }


                JSONObject jsonObject = new JSONObject();
                jsonObject.put("PITCH", param1);
                jsonObject.put("ROLL", param2);
                jsonObject.put("YAW", param3);
                jsonObject.put("ALTURA", param4);
                //out.println(jsonObject);
                out.println("Hello, MATLAB!");
                Log.e(TAG2, "Mensaje enviado: " + "Hello, MATLAB!");
                //clientMessage = in.readLine();
                Log.e(TAG2, "Respuesta del servidor: " + clientMessage);



                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                clientSocket.close();
                Log.e(TAG2, "Conexión con el cliente cerrada.");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return clientMessage;
        }


        @Override
        protected void onPostExecute(String response) {
            if (response.equals("si")) {
                showToast("El dron se movera hacia adelante");
            }else if (response.equals("no")){
                showToast("El dron dejará de moverse hacia adelante");
            }else if (response.equals("rotate")){
                yaw = 20;
                showToast("El dron iniciará a rotar");
                //if (!isVirtualStickDataTaskScheduled) {
                    //sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    //sendVirtualStickDataTimer = new Timer();
                    //sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
//                    isVirtualStickDataTaskScheduled = true;
                //}
            }else if (response.equals("stop_rotate")){
                yaw = 0;
                showToast("El dron dejará de rotar");
                //if (!isVirtualStickDataTaskScheduled) {
                    //sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    //sendVirtualStickDataTimer = new Timer();
                    //sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
                    //isVirtualStickDataTaskScheduled = true;
                //}
            }else if (response.equals("elevate")){
                throttle=(float) 0.6;
                showToast("El dron iniciará a elevarse");
                //if (!isVirtualStickDataTaskScheduled) {
                    //sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    //sendVirtualStickDataTimer = new Timer();
                    //sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 20);
                    //isVirtualStickDataTaskScheduled = true;
                //}
            }else if (response.equals("stop_elevate")){
                throttle=(float)0;
                showToast("El dron dejará de elevarse");
                //if (!isVirtualStickDataTaskScheduled) {
                    //sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    //sendVirtualStickDataTimer = new Timer();
                    //sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 20);
                    //isVirtualStickDataTaskScheduled = true;
                //}
            }
            Log.e(TAG2, "COMUNICACION TCP: " + response);
//            showToast(jsonObject.toString());

        }
    }
    /**
     * Método llamado cuando la vista se adjunta a una ventana.
     * Establece los oyentes para los eventos.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUpListeners();
    }
    /**
     * Método llamado cuando la vista se desvincula de una ventana.
     * Limpia los recursos y anula los temporizadores y tareas programadas.
     */
    @Override
    protected void onDetachedFromWindow() {
        if (null != sendVirtualStickDataTimer) {
            if (sendVirtualStickDataTask != null) {
                sendVirtualStickDataTask.cancel();
            }
            sendVirtualStickDataTimer.cancel();
            sendVirtualStickDataTimer.purge();
            sendVirtualStickDataTimer = null;
            sendVirtualStickDataTask = null;

            isVirtualStickDataTaskScheduled = false;
        }
        tearDownListeners();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al cerrar ServerSocket: " + e.getMessage());
        }
        super.onDetachedFromWindow();
    }
    /**
     * Inicializa la vista y configura la interfaz de usuario.
     * @param context Contexto de la aplicación.
     */
    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.vuelo, this, true);
        initParams();
        initUI();
        try {
            logi = new loggerr("miLogDeVuelo.txt");
        } catch (IOException e) {
            e.printStackTrace();
            // Manejo de excepciones
        }
        flightController.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(FlightControllerState flightControllerState) {
                // Obtener la ubicación actual del dron
                LocationCoordinate3D currentLocation = flightControllerState.getAircraftLocation();

                switch (currentDesiredAction) {
                    case ASCEND:
                        yaw = 10;
                        // Cambiar la altitud a 2 metros y mover 5 metros hacia adelante
                        //setAltitude(currentLocation, 4.0);
                        //moveForward(currentLocation, 5.0);
                        break;
                    case DESCEND:
                        yaw = 0;
                        // Cambiar la altitud a 1 metro
                        //setAltitude(currentLocation, 1.0);
                        break;
                    case NONE:
                        // No hacer nada
                        break;
                }
            }
        });
        try {
            serverSocket = new ServerSocket(50000); // Inicializa el ServerSocket aquí
        } catch (IOException e) {
            Log.e(TAG, "Error al inicializar ServerSocket: " + e.getMessage());
        }
    }
    /**
     * Inicializa los parámetros para el control de vuelo y el simulador.
     */
    private void initParams() {
        // We recommand you use the below settings, a standard american hand style.

        if (flightController == null) {
            if (ModuleVerification.isFlightControllerAvailable()) {
                flightController = MApplication.getAircraftInstance().getFlightController();
            }
        }
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

        if (flightState == null) {
            if (ModuleVerification.isFlightControllerAvailable()) {
                flightState = flightController.getState();
            }
        }

        if (altitud == null) {
            if (ModuleVerification.isFlightControllerAvailable()) {
                altitud = flightState.getAircraftLocation();
            }
        }



        // Check if the simulator is activated.
        if (simulator == null) {
            simulator = ModuleVerification.getSimulator();
        }
        isSimulatorActived = simulator.isSimulatorActive();

    }
    /**
     * Inicializa la interfaz de usuario, configurando botones, joysticks y otros elementos de la UI.
     */
    private void initUI() {
        buttonElev=(Button) findViewById(R.id.buttonElev);
        buttonElev.setOnClickListener(this);
        buttonBajar=(Button) findViewById(R.id.buttonBajar);
        buttonBajar.setOnClickListener(this);
        buttonSubirAuto=(Button) findViewById(R.id.buttonSubirAuto);
        buttonSubirAuto.setOnClickListener(this);
        buttonBajarAuto=(Button) findViewById(R.id.buttonBajarAuto);
        buttonBajarAuto.setOnClickListener(this);
        screenJoystickRight = (OnScreenJoystick) findViewById(R.id.directionJoystickRight);
        screenJoystickLeft = (OnScreenJoystick) findViewById(R.id.directionJoystickLeft);

        btnSimulator = (ToggleButton) findViewById(R.id.btn_start_simulator);
        btnEnableVirtualStick = (Button) findViewById(R.id.btn_enable_virtual_stick);
        btnDisableVirtualStick = (Button) findViewById(R.id.btn_disable_virtual_stick);

        textView = (TextView) findViewById(R.id.textview_simulator);

        btnEnableVirtualStick.setOnClickListener(this);
        btnDisableVirtualStick.setOnClickListener(this);
        btnSimulator.setOnCheckedChangeListener(vuelo.this);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);

        if (isSimulatorActived) {
            btnSimulator.setChecked(true);
            textView.setText("Simulator is On.");
        }

    }
    /**
     * Configura los oyentes para el simulador y los joysticks en pantalla.
     */
    private void setUpListeners() {

        if (simulator != null) {
            simulator.setStateCallback(new SimulatorState.Callback() {
                @Override
                public void onUpdate(@NonNull final SimulatorState simulatorState) {
                    ToastUtils.setResultToText(textView,
                            "Yaw : "
                                    + simulatorState.getYaw()
                                    + ","
                                    + "X : "
                                    + simulatorState.getPositionX()
                                    + "\n"
                                    + "Y : "
                                    + simulatorState.getPositionY()
                                    + ","
                                    + "Z : "
                                    + simulatorState.getPositionZ());
                    //connectToServer(simulatorState.getYaw(),simulatorState.getPositionX(),simulatorState.getPositionY(),simulatorState.getPositionZ());
                }

                //connectToServer();
            });
        } else {
            ToastUtils.setResultToToast("Simulator disconnected!");
        }

        screenJoystickLeft.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                Attitude attitude = flightState.getAttitude();

                float altitud2= altitud.getAltitude();

                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float pitchJoyControlMaxSpeed = 10;
                float rollJoyControlMaxSpeed = 10;

                pitch = pitchJoyControlMaxSpeed * pY;
                roll = rollJoyControlMaxSpeed * pX;


                if (!isVirtualStickDataTaskScheduled) {
                    sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    sendVirtualStickDataTimer = new Timer();
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 10);
                    isVirtualStickDataTaskScheduled = true;
                }

            }
        });

        screenJoystickRight.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {

                Attitude attitude = flightState.getAttitude();
                LocationCoordinate3D prob=flightState.getAircraftLocation();
                float altitud2= prob.getAltitude();
                //connectToServer(attitude.pitch,attitude.yaw,attitude.roll,altitud2);
                new TcpCommunicationTask(attitude.pitch, attitude.yaw, attitude.roll, (double) altitud2).execute();

                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float verticalJoyControlMaxSpeed = 1;
                float yawJoyControlMaxSpeed = 20;
                Log.e(TAG,"PITCH: "+attitude.pitch+" YAW: "+attitude.yaw+" ROLL: "+attitude.roll+" Altura: " +altitud2);
                logi.logData(altitud2, (float) attitude.yaw);
                ToastUtils.setResultToText(textView,
                        "PITCH : "
                                + attitude.pitch
                                + ","
                                + " YAW : "
                                + attitude.yaw
                                + "\n"
                                + " ROLL : "
                                + attitude.roll
                                + ","
                                + " ALTITUD : "
                                + altitud2);
                yaw = yawJoyControlMaxSpeed * pX;
                throttle = verticalJoyControlMaxSpeed * pY;

                if (!isVirtualStickDataTaskScheduled) {
                    sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    sendVirtualStickDataTimer = new Timer();
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 10);
                    isVirtualStickDataTaskScheduled = true;
                }

            }
        });

    }
    /**
     * Elimina los oyentes cuando ya no son necesarios.
     */
    private void tearDownListeners() {
        Simulator simulator = ModuleVerification.getSimulator();
        if (simulator != null) {
            simulator.setStateCallback(null);
        }
        screenJoystickLeft.setJoystickListener(null);
        screenJoystickRight.setJoystickListener(null);
    }
    /**
     * Método onClick que maneja los clics en varios botones.
     * @param v La vista que fue clickeada.
     */
    @Override
    public void onClick(View v) {
        FlightController flightController = ModuleVerification.getFlightController();
        if (flightController == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.buttonElev:
                flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        Dialogos.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                break;
            case R.id.buttonBajar:
                flightController.startLanding(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        Dialogos.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                if (logi != null) {
                    logi.close();
                }
                break;
            case R.id.btn_enable_virtual_stick:
                flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        flightController.setVirtualStickAdvancedModeEnabled(true);
                        Dialogos.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                break;
            case R.id.btn_disable_virtual_stick:
                flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        Dialogos.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                break;
            case R.id.buttonSubirAuto:
                currentDesiredAction = DesiredAction.ASCEND;
                break;
            case R.id.buttonBajarAuto:
                currentDesiredAction = DesiredAction.DESCEND;
                break;
            default:
                break;
        }
    }

    /**
     * Método que se llama cuando el estado del botón del simulador cambia.
     * @param compoundButton El botón que cambió.
     * @param b El nuevo estado del botón.
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == btnSimulator) {
            onClickSimulator(b);
        }
    }
    /**
     * Controla el estado del simulador cuando se activa/desactiva.
     * @param isChecked El estado del simulador.
     */
    private void onClickSimulator(boolean isChecked) {
        if (simulator == null) {
            return;
        }
        if (isChecked) {
            textView.setVisibility(VISIBLE);
            simulator.start(InitializationData.createInstance(new LocationCoordinate2D(23, 113), 10, 10), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtils.setResultToToast(djiError.getDescription());
                    }
                }
            });
        } else {
            textView.setVisibility(INVISIBLE);
            simulator.stop(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtils.setResultToToast(djiError.getDescription());
                    }
                }
            });
        }
    }

    /**
     * Tarea programada para enviar datos de control de vuelo al controlador de vuelo.
     */
    private class SendVirtualStickDataTask extends TimerTask {
        @Override
        public void run() {
            if (flightController != null) {
                //La interfaz está escrita al revés. setPitch() debe pasar el valor de balanceo y setRoll() debe pasar el valor de paso.

                flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            ToastUtils.setResultToToast(djiError.getDescription());
                        }
                    }
                });
                //isVirtualStickDataTaskScheduled = false;
            }
        }
    }
    /**
     * Muestra un mensaje de toast en la interfaz de usuario.
     * @param toastMsg El mensaje a mostrar.
     */
    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getContext(), toastMsg, Toast.LENGTH_LONG).show());

    }
    private void setAltitude(LocationCoordinate3D currentLocation, double yaw ,double newAltitude) {
        // Use Virtual Stick to control altitude
        // You might want to ensure that Virtual Stick is enabled before reaching this point
        FlightControlData controlData = new FlightControlData(0, 0, (float) yaw, (float) newAltitude);
        flightController.sendVirtualStickFlightControlData(controlData, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    // Successfully set altitude
                } else {
                    // Handle error
                }
            }
        });
    }


    private void moveForward(LocationCoordinate3D currentLocation, double distanceInMeters) {
        // Use Virtual Stick to control movement
        // You might want to ensure that Virtual Stick is enabled before reaching this point
        float pitch = (float) (distanceInMeters / 100); // Assume 100ms command interval, this is just for demonstration
        FlightControlData controlData = new FlightControlData(0, 0, pitch, currentLocation.getAltitude());
        flightController.sendVirtualStickFlightControlData(controlData, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    // Successfully moved
                } else {
                    // Handle error
                }
            }
        });
    }
    private void setSafeAltitude(double altitude) {
        if (flightController != null) {
            FlightControlData controlData = new FlightControlData(0, 0, 0, (float) altitude);
            flightController.sendVirtualStickFlightControlData(controlData, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        // Maneja el error
                    }
                }
            });
        }
    }
}
