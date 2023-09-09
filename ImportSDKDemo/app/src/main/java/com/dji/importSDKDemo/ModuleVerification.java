package com.dji.importSDKDemo;

import com.dji.importSDKDemo.MApplication;

import androidx.annotation.Nullable;
import dji.common.product.Model;
import dji.sdk.accessory.AccessoryAggregation;
import dji.sdk.accessory.beacon.Beacon;
import dji.sdk.accessory.speaker.Speaker;
import dji.sdk.accessory.spotlight.Spotlight;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;

/**
 * Clase ModuleVerification: Verifica la disponibilidad de varios módulos y funcionalidades en productos DJI.
 * Basado en documentacion de dji.
 */
public class ModuleVerification {
    /**
     * Verifica si algún producto de DJI está disponible.
     * @return true si hay un producto disponible, false de lo contrario.
     */
    public static boolean isProductModuleAvailable() {
        return (null != MApplication.getProductInstance());
    }
    /**
     * Verifica si el producto es una aeronave.
     * @return true si es una aeronave, false de lo contrario.
     */
    public static boolean isAircraft() {
        return MApplication.getProductInstance() instanceof Aircraft;
    }

    /**
     * Verifica si el producto disponible es un dispositivo portátil.
     * @return Devuelve true si es así y false de lo contrario.
     */
    public static boolean isHandHeld() {
        return MApplication.getProductInstance() instanceof HandHeld;
    }

    /**
     * Verifica si hay una cámara disponible en el producto.
     * @return Devuelve true si hay una cámara y false si no.
     */
    public static boolean isCameraModuleAvailable() {
        return isProductModuleAvailable() && (null != MApplication.getProductInstance().getCamera());
    }

    /**
     * Verifica si la funcionalidad de reproducción está disponible en la cámara del producto.
     * @return Devuelve true si es así y false de lo contrario.
     */
    public static boolean isPlaybackAvailable() {
        return isCameraModuleAvailable() && (null != MApplication.getProductInstance()
                .getCamera()
                .getPlaybackManager());
    }

    /**
     * Verifica si el gestor de medios está disponible en la cámara del producto.
     * @return Devuelve true si es así y false de lo contrario.
     */
    public static boolean isMediaManagerAvailable() {
        return isCameraModuleAvailable() && (null != MApplication.getProductInstance()
                .getCamera()
                .getMediaManager());
    }

    /**
     * Verifica si hay un control remoto disponible para el producto.
     * @return Devuelve true si hay un control remoto y false si no.
     */
    public static boolean isRemoteControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != MApplication.getAircraftInstance()
                .getRemoteController());
    }

    /**
     * Verifica si hay un controlador de vuelo disponible para la aeronave.
     * @return Devuelve true si hay un controlador de vuelo y false si no.
     */
    public static boolean isFlightControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != MApplication.getAircraftInstance()
                .getFlightController());
    }

    /**
     * Verifica si hay una brújula disponible en el controlador de vuelo de la aeronave.
     * @return Devuelve true si hay una brújula y false si no.
     */
    public static boolean isCompassAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != MApplication.getAircraftInstance()
                .getFlightController()
                .getCompass());
    }

    /**
     * Verifica si hay alguna limitación de vuelo para la aeronave.
     * @return Devuelve true si hay limitaciones y false si no.
     */
    public static boolean isFlightLimitationAvailable() {
        return isFlightControllerAvailable() && isAircraft();
    }

    /**
     * Verifica si hay un cardán (gimbal) disponible en el producto.
     * @return Devuelve true si hay un cardán y false si no.
     */
    public static boolean isGimbalModuleAvailable() {
        return isProductModuleAvailable() && (null != MApplication.getProductInstance().getGimbal());
    }

    /**
     * Verifica si hay un enlace de aire (Airlink) disponible en el producto.
     * @return Devuelve true si hay un Airlink y false si no.
     */
    public static boolean isAirlinkAvailable() {
        return isProductModuleAvailable() && (null != MApplication.getProductInstance().getAirLink());
    }

    /**
     * Verifica si hay un enlace WiFi disponible en el Airlink del producto.
     * @return  Devuelve true si hay un enlace WiFi y false si no.
     */
    public static boolean isWiFiLinkAvailable() {
        return isAirlinkAvailable() && (null != MApplication.getProductInstance().getAirLink().getWiFiLink());
    }

    /**
     * Verifica si hay un enlace Lightbridge disponible en el Airlink del producto.
     * @return Devuelve true si hay un enlace Lightbridge y false si no.
     */
    public static boolean isLightbridgeLinkAvailable() {
        return isAirlinkAvailable() && (null != MApplication.getProductInstance()
                .getAirLink()
                .getLightbridgeLink());
    }

    /**
     * Verifica si hay un enlace OcuSync disponible en el Airlink del producto.
     * @return Devuelve true si hay un enlace OcuSync y false si no.
     */
    public static boolean isOcuSyncLinkAvailable() {
        return isAirlinkAvailable() && (null != MApplication.getProductInstance()
                .getAirLink()
                .getOcuSyncLink());
    }

    /**
     * Verifica si hay una carga útil disponible en la aeronave.
     * @return Devuelve true si hay una carga útil y false si no.
     */
    public static boolean isPayloadAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != MApplication.getAircraftInstance()
                .getPayload());
    }

    /**
     * Verifica si hay un módulo RTK disponible en la aeronave.
     * @return Devuelve true si hay un módulo RTK y false si no.
     */
    public static boolean isRTKAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != MApplication.getAircraftInstance()
                .getFlightController().getRTK());
    }
    /**
     * Obtiene la agregación de accesorios para el producto actual.
     * @return Accesorio agregado si está disponible, null de lo contrario.
     */
    public static AccessoryAggregation getAccessoryAggregation() {
        Aircraft aircraft = (Aircraft) MApplication.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation()) {
            return aircraft.getAccessoryAggregation();
        }
        return null;
    }
    /**
     * Obtiene el altavoz del producto actual.
     * @return Altavoz si está disponible, null de lo contrario.
     */
    public static Speaker getSpeaker() {
        Aircraft aircraft = (Aircraft) MApplication.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getSpeaker()) {
            return aircraft.getAccessoryAggregation().getSpeaker();
        }
        return null;
    }
    /**
     * Obtiene el faro (Beacon) del producto actual.
     * @return Faro si está disponible, null de lo contrario.
     */
    public static Beacon getBeacon() {
        Aircraft aircraft = (Aircraft) MApplication.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getBeacon()) {
            return aircraft.getAccessoryAggregation().getBeacon();
        }
        return null;
    }
    /**
     * Obtiene el reflector (Spotlight) del producto actual.
     * @return Reflector si está disponible, null de lo contrario.
     */
    public static Spotlight getSpotlight() {
        Aircraft aircraft = (Aircraft) MApplication.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getSpotlight()) {
            return aircraft.getAccessoryAggregation().getSpotlight();
        }
        return null;
    }
    /**
     * Obtiene el simulador para la aeronave actual.
     * @return Simulador si está disponible, null de lo contrario.
     */
    @Nullable
    public static Simulator getSimulator() {
        Aircraft aircraft = MApplication.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                return flightController.getSimulator();
            }
        }
        return null;
    }
    /**
     * Obtiene el controlador de vuelo para la aeronave actual.
     * @return Controlador de vuelo si está disponible, null de lo contrario.
     */
    @Nullable
    public static FlightController getFlightController() {
        Aircraft aircraft = MApplication.getAircraftInstance();
        if (aircraft != null) {
            return aircraft.getFlightController();
        }
        return null;
    }
    /**
     * Obtiene el controlador de vuelo para la aeronave actual.
     * @return Controlador de vuelo si está disponible, null de lo contrario.
     */
    @Nullable
    public static boolean isMavic2Product() {
        BaseProduct baseProduct = MApplication.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MAVIC_2_PRO || baseProduct.getModel() == Model.MAVIC_2_ZOOM;
        }
        return false;
    }

    /**
     * Verifica si el producto es un Matrice 300 RTK.
     * @return Devuelve true si es este modelo y false de lo contrario.
     */
    public static boolean isMatrice300RTK(){
        BaseProduct baseProduct = MApplication.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MATRICE_300_RTK;
        }
        return false;
    }

    /**
     * Verifica si el producto es un Mavic Air 2.
     * @return Devuelve true si es este modelo y false de lo contrario.
     */
    public static boolean isMavicAir2(){
        BaseProduct baseProduct = MApplication.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MAVIC_AIR_2;
        }
        return false;
    }

}