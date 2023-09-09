package com.dji.importSDKDemo;
import com.dji.importSDKDemo.MApplication;
import com.dji.importSDKDemo.utils.OnScreenJoystick;
import com.dji.importSDKDemo.utils.OnScreenJoystickListener;
import com.dji.importSDKDemo.ToastUtils;

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
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;

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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
    /**
     * Joystick derecho en pantalla para controlar el dron.
     */
    private OnScreenJoystick screenJoystickRight;
    /**
     * Joystick izquierdo en pantalla para controlar el dron.
     */
    private OnScreenJoystick screenJoystickLeft;

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
            String response = "";

            try {
                String serverIP = "172.20.10.3";
                int serverPort = 10000;

                Socket socket = new Socket(serverIP, serverPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


                String paramsStr = param1 + "," + param2 + "," + param3 + "," + param4;
                out.println(paramsStr);

                response = in.readLine();

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }


        @Override
        protected void onPostExecute(String response) {
            Log.e(TAG, "COMUNICACION TCP: " + response);
            showToast("Respuesta del servidor: " + response);
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
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
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
        screenJoystickRight = (OnScreenJoystick) findViewById(R.id.directionJoystickRight);
        screenJoystickLeft = (OnScreenJoystick) findViewById(R.id.directionJoystickLeft);

        btnSimulator = (ToggleButton) findViewById(R.id.btn_start_simulator);
        btnEnableVirtualStick = (Button) findViewById(R.id.btn_enable_virtual_stick);
        btnDisableVirtualStick = (Button) findViewById(R.id.btn_disable_virtual_stick);

        textView = (TextView) findViewById(R.id.textview_simulator);

        btnEnableVirtualStick.setOnClickListener(this);
        btnDisableVirtualStick.setOnClickListener(this);
        btnSimulator.setOnCheckedChangeListener(vuelo.this);

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
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
                    isVirtualStickDataTaskScheduled = true;
                }

            }
        });

        screenJoystickRight.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {

                Attitude attitude = flightState.getAttitude();
                float altitud2= altitud.getAltitude();
                //connectToServer(attitude.pitch,attitude.yaw,attitude.roll,altitud2);
                new TcpCommunicationTask(attitude.pitch, attitude.yaw, attitude.roll, (double) altitud2).execute();

                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float verticalJoyControlMaxSpeed = 4;
                float yawJoyControlMaxSpeed = 20;
                Log.e(TAG,"PITCH: "+attitude.pitch+" YAW: "+attitude.yaw+" ROLL: "+attitude.roll+" Altura: " +altitud2);

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
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
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
}
