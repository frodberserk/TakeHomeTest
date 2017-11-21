package com.demo.takehometest.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Util class which contains methods for common use inside app.
 */

public class AppMethods {

    /**
     * Check if location is enabled in app.
     *
     * @param context Context of calling class.
     * @return true if location enabled, false otherwise
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    /**
     * Check if location permission is granted.
     *
     * @return True if granted, false otherwise.
     */
    public static boolean isLocationPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Zooms a Route (given a List of LatLng) at the greatest possible zoom level.
     *
     * @param googleMap:      instance of GoogleMap
     * @param lstLatLngRoute: list of LatLng forming Route
     * @param padding         padding in pixels around journey box
     */
    public static void zoomRoute(GoogleMap googleMap, List<LatLng> lstLatLngRoute, int padding) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding));
    }

    /**
     * Generate a random key
     *
     * @return key generated as String
     * @throws NoSuchAlgorithmException
     */
    public static String generateKey() throws NoSuchAlgorithmException {
        // Generate a 256-bit key
        final int outputKeyLength = 256;

        SecureRandom secureRandom = new SecureRandom();
        // Do *not* seed secureRandom! Automatically seeded from system entropy.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(outputKeyLength, secureRandom);
        SecretKey key = keyGenerator.generateKey();
        return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
    }

    /**
     * Generate a random string
     *
     * @param sizeOfRandomString length of string
     * @return string generated
     */
    public static String getRandomString(final int sizeOfRandomString) {
        final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}