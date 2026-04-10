
package com.example.loramessenger;

import com.google.android.gms.location.Priority;
import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "com.example.loramessenger.USB_PERMISSION";
    private static final int LOCATION_PERMISSION_CODE = 100;

    EditText messageInput;
    Button sendBtn;
    TextView status;

    UsbManager usbManager;
    UsbSerialPort port;
    UsbSerialDriver driver;

    int packetId = 0; // Unique ID to help the relay node

    // 1. BroadcastReceiver to listen for the user clicking "Allow" on the USB prompt
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            openSerialPort(device);
                        }
                    } else {
                        status.setText("USB Permission Denied");
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        status = findViewById(R.id.status);

        usbManager = (UsbManager) getSystemService(USB_SERVICE);

        // Register the USB permission receiver
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        // Using ContextCompat for compatibility with newer Android versions
        ContextCompat.registerReceiver(this, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // Check Location Permissions on startup
        checkLocationPermissions();

        connectUSB();

        sendBtn.setOnClickListener(v -> {
            // Check if we have permission right before trying to get the location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                status.setText("Missing Location Permission");
                checkLocationPermissions();
                return;
            }

            status.setText("Getting live GPS..."); // Let the user know we are waiting for GPS

            // CHANGE HERE: Request a fresh, high-accuracy location right now
            LocationServices.getFusedLocationProviderClient(this)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            String msg = messageInput.getText().toString();

                            packetId++;
                            String finalMsg = "PKT:" + packetId + "|ID:Saif|" + msg +
                                    "|LAT:" + location.getLatitude() +
                                    "|LON:" + location.getLongitude() + "\n";

                            status.setText("Sending...");
                            sendToESP32(finalMsg);

                            messageInput.setText("");

                        } else {
                            status.setText("GPS signal weak. Try stepping outside.");
                        }
                    });
        });
    }

    private void connectUSB() {
        List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

        if (drivers.isEmpty()) {
            status.setText("No USB device found");
            return;
        }

        driver = drivers.get(0);
        UsbDevice device = driver.getDevice();

        // 4. Check if we already have permission. If yes, open. If no, request it.
        if (usbManager.hasPermission(device)) {
            openSerialPort(device);
        } else {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(
                    this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            usbManager.requestPermission(device, permissionIntent);
        }
    }

    private void openSerialPort(UsbDevice device) {
        // 5. Open the device exactly once
        UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection == null) {
            status.setText("Failed to open connection");
            return;
        }

        port = driver.getPorts().get(0);

        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            status.setText("USB Connected");
        } catch (Exception e) {
            status.setText("USB Error: " + e.getMessage());
        }
    }

    private void sendToESP32(String data) {
        try {
            if (port != null && port.isOpen()) {
                port.write(data.getBytes(), 2000);
                status.setText("Sent!");
            } else {
                status.setText("Port is closed or null");
            }
        } catch (Exception e) {
            status.setText("Send Failed: " + e.getMessage());
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the broadcast receiver when the app closes
        unregisterReceiver(usbReceiver);
        try {
            if (port != null) port.close();
        } catch (Exception ignored) {}
    }
}