package com.dji.importSDKDemo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import androidx.annotation.Nullable;

import com.dji.importSDKDemo.R;
import com.dji.importSDKDemo.utils.OnScreenJoystickListener;
/**
 * Una clase que representa un joystick en pantalla.
 */
public class OnScreenJoystick extends SurfaceView implements
        SurfaceHolder.Callback, OnTouchListener {
    /**
     * Bitmap que representa el joystick visual en la interfaz de usuario.
     */
    private Bitmap mJoystick;
    /**
     * Holder que maneja el acceso a la superficie en la que se dibuja el joystick.
     */
    private SurfaceHolder mHolder;
    /**
     * Rectángulo que define los límites del área donde se dibujará el joystick.
     */
    private Rect mKnobBounds;
    /**
     * Hilo que gestiona el dibujo del joystick y la actualización de su posición.
     */
    private JoystickThread mThread;
    /**
     * Coordenadas X e Y donde se dibujará el joystick.
     */
    private int mKnobX, mKnobY;
    /**
     * Tamaño del joystick en píxeles.
     */
    private int mKnobSize;
    /**
     * Tamaño del área de dibujo para el joystick.
     */
    private int mBackgroundSize;
    /**
     * Radio del área circular en la que se puede mover el joystick.
     */
    private float mRadius;
    /**
     * Listener que manejará los eventos de movimiento del joystick.
     */
    private OnScreenJoystickListener mJoystickListener;
    /**
     * Bandera para determinar si el joystick debe volver automáticamente al centro.
     */
    private boolean mAutoCentering = true;
    /**
     * Inicializa el joystick en pantalla.
     *
     * @param context Contexto de la aplicación.
     * @param attrs Atributos XML del joystick.
     */
    public OnScreenJoystick(Context context, AttributeSet attrs) {
        super(context, attrs);

        initGraphics(attrs);
        init();
    }
    /**
     * Inicializa las gráficas del joystick.
     *
     * @param attrs Atributos XML del joystick.
     */
    private void initGraphics(AttributeSet attrs) {
        Resources res = getContext().getResources();
        mJoystick = BitmapFactory
                .decodeResource(
                        res, R.mipmap.joystick);

    }
    /**
     * Inicializa los límites del joystick.
     *
     * @param pCanvas Canvas donde se dibujará el joystick.
     */
    private void initBounds(final Canvas pCanvas) {
        mBackgroundSize = pCanvas.getHeight();
        mKnobSize = Math.round(mBackgroundSize * 0.6f);

        mKnobBounds = new Rect();

        mRadius = mBackgroundSize * 0.5f;
        mKnobX = Math.round((mBackgroundSize - mKnobSize) * 0.5f);
        mKnobY = Math.round((mBackgroundSize - mKnobSize) * 0.5f);

    }
    /**
     * Realiza inicializaciones adicionales.
     */
    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        mThread = new JoystickThread();

        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        setOnTouchListener(this);
        setEnabled(true);
        setAutoCentering(true);
    }
    /**
     * Establece el comportamiento de auto-centrado del joystick.
     *
     * @param pAutoCentering Verdadero si el joystick debe centrarse automáticamente.
     */
    public void setAutoCentering(final boolean pAutoCentering) {
        mAutoCentering = pAutoCentering;
    }
    /**
     * Retorna si el joystick tiene auto-centrado activado.
     *
     * @return Verdadero si el joystick se auto-centra.
     */
    public boolean isAutoCentering() {
        return mAutoCentering;
    }
    /**
     * Establece el oyente que recibirá eventos del joystick.
     *
     * @param pJoystickListener El oyente.
     */
    public void setJoystickListener(@Nullable final OnScreenJoystickListener pJoystickListener) {
        mJoystickListener = pJoystickListener;
    }
    /**
     * Método de devolución de llamada invocado cuando la superficie cambia.
     *
     * @param arg0 El objeto SurfaceHolder que contiene la superficie.
     * @param arg1 Formato de píxeles de la superficie.
     * @param arg2 Ancho de la superficie.
     * @param arg3 Altura de la superficie.
     */
    @Override
    public void surfaceChanged(final SurfaceHolder arg0, final int arg1,
                               final int arg2, final int arg3) {

//		mThread.setRunning(false);
    }
    /**
     * Método de devolución de llamada invocado cuando se crea la superficie.
     *
     * @param arg0 El objeto SurfaceHolder que contiene la superficie.
     */
    @Override
    public void surfaceCreated(final SurfaceHolder arg0) {
        mThread.start();

    }
    /**
     * Método de devolución de llamada invocado cuando se destruye la superficie.
     *
     * @param arg0 El objeto SurfaceHolder que contiene la superficie.
     */
    @Override
    public void surfaceDestroyed(final SurfaceHolder arg0) {
        boolean retry = true;
        mThread.setRunning(false);

        while (retry) {
            try {
                // code to kill Thread
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }

    }
    /**
     * Dibuja el joystick en el canvas.
     *
     * @param pCanvas Canvas donde se dibujará el joystick.
     */
    public void doDraw(final Canvas pCanvas) {
        if (mKnobBounds == null) {
            initBounds(pCanvas);
        }

        // pCanvas.drawBitmap(mJoystickBg, null, mBgBounds, null);

        mKnobBounds.set(mKnobX, mKnobY, mKnobX + mKnobSize, mKnobY + mKnobSize);
        pCanvas.drawBitmap(mJoystick, null, mKnobBounds, null);
    }
    /**
     * Maneja los eventos táctiles en el joystick.
     *
     * @param arg0 La vista que recibió el evento táctil.
     * @param pEvent El evento táctil.
     * @return Verdadero si el evento fue manejado.
     */
    @Override
    public boolean onTouch(final View arg0, final MotionEvent pEvent) {
        final float x = pEvent.getX();
        final float y = pEvent.getY();

        switch (pEvent.getAction()) {

            case MotionEvent.ACTION_UP:
                if (isAutoCentering()) {
                    mKnobX = Math.round((mBackgroundSize - mKnobSize) * 0.5f);
                    mKnobY = Math.round((mBackgroundSize - mKnobSize) * 0.5f);
                }
                break;
            default:
                // Check if coordinates are in bounds. If they aren't move the knob
                // to the closest coordinate inbounds.
                if (checkBounds(x, y)) {
                    mKnobX = Math.round(x - mKnobSize * 0.5f);
                    mKnobY = Math.round(y - mKnobSize * 0.5f);
                } else {
                    final double angle = Math.atan2(y - mRadius, x - mRadius);
                    mKnobX = (int) (Math.round(mRadius
                            + (mRadius - mKnobSize * 0.5f) * Math.cos(angle)) - mKnobSize * 0.5f);
                    mKnobY = (int) (Math.round(mRadius
                            + (mRadius - mKnobSize * 0.5f) * Math.sin(angle)) - mKnobSize * 0.5f);
                }
        }

        if (mJoystickListener != null) {
            mJoystickListener.onTouch(this,
                    (0.5f - (mKnobX / (mRadius * 2 - mKnobSize))) * -2,
                    (0.5f - (mKnobY / (mRadius * 2 - mKnobSize))) * 2);

        }

        return true;
    }
    /**
     * Verifica si las coordenadas están dentro de los límites del joystick.
     *
     * @param pX Coordenada X.
     * @param pY Coordenada Y.
     * @return Verdadero si las coordenadas están dentro de los límites.
     */
    private boolean checkBounds(final float pX, final float pY) {
        return Math.pow(mRadius - pX, 2) + Math.pow(mRadius - pY, 2) <= Math
                .pow(mRadius - mKnobSize * 0.5f, 2);
    }
    /**
     * Envía un evento táctil.
     */
    private void pushTouchEvent(){

        if (mJoystickListener != null) {
            mJoystickListener.onTouch(this,
                    (0.5f - (mKnobX / (mRadius * 2 - mKnobSize))) * -2,
                    (0.5f - (mKnobY / (mRadius * 2 - mKnobSize))) * 2);

        }
    }
    /**
     * Clase interna para manejar el dibujo del joystick en un hilo separado.
     */
    private class JoystickThread extends Thread {
        /**
         * Indica si el hilo está en ejecución.
         */
        private boolean running = false;
        /**
         * Inicia la ejecución del hilo.
         */
        @Override
        public synchronized void start() {
            if (!running && this.getState() == Thread.State.NEW) {
                running = true;
                super.start();
            }
        }
        /**
         * Establece el estado de ejecución del hilo.
         *
         * @param pRunning Verdadero si el hilo debe continuar ejecutándose.
         */
        public void setRunning(final boolean pRunning) {
            running = pRunning;
        }
        /**
         * Método principal de ejecución del hilo.
         */
        @Override
        public void run() {
            while (running) {
                // draw everything to the canvas
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas(null);
                    synchronized (mHolder) {
                        // reset canvas
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        doDraw(canvas);
                    }
                }
                catch(Exception e){}
                finally {
                    if (canvas != null) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }

                pushTouchEvent();

                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException ignored)
                {
                }
            }
        }
    }

}