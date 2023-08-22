package br.com.makrosystems.filedata;

import static android.app.appsearch.SetSchemaRequest.READ_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

import br.com.makrosystems.Property;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        System.out.println("PROCESSOS: "+ responseAppList(this));
//        String ESSAPORRATAAQUI = read("ro.build.magisk.hide=1");
//        System.out.println("PROPERties:"+ESSAPORRATAAQUI);
//        if(ContextCompat.checkSelfPermission(this,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
//            //ask for permission
//            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
//        }
//        System.out.println("TESTE GUI: "+check2());


        //Atraves de uma listagem de aplicativos instalados, verifica se o magisk esta instalado no celular
        AppList appList = new AppList(this);
        String listaDApp = appList.responseAppList();
//        if (listaDApp.contains("com.topjohnwu.magisk")) {
        if (listaDApp.contains("com.topjohnwu.magisk")) {
            System.out.println("MAGISK INSTALADO!");
        } else {
            System.out.println("MAGISK NÃO ESTÁ INSTALADO!");
        }

        //Pega o processo do magisk
        if (FridaTeste.isProcess("magisk")) {
            System.out.println("PROCESSO DO MAGISK HIDE DETECTADO");
        } else {
            System.out.println("NÃO EXISTE PROCESSO DO MAGISK HIDE");
        }



// Verifica se o Magisk Hide está ativado
//        boolean isMagiskHideEnabled = isMagiskHideEnabled();
//        // Exibe uma mensagem com o resultado
//        if (isMagiskHideEnabled) {
//            System.out.println("RESULTADO: \nMagisk Hide está ativado");
//            Toast.makeText(this, "Magisk Hide está ativado", Toast.LENGTH_SHORT).show();
//        } else {
//            System.out.println("RESULTADO: \nMagisk Hide não está ativado");
//            Toast.makeText(this, "Magisk Hide não está ativado", Toast.LENGTH_SHORT).show();
//        }


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MagiskManagerDetected");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message"); // Message shown to the user
                System.out.println("MESSAGE: "+message);
                String reasonData = intent.getStringExtra("reasonData"); // Threat detection cause
                String reasonCode = intent.getStringExtra("reasonCode"); // Event reason code
                String currentThreatEventScore = intent.getStringExtra("currentThreatEventScore"); // Current threat event score
                String threatEventsScore = intent.getStringExtra("threatEventsScore"); // Total threat events score
                String variable = intent.getStringExtra("<Context Key>"); // Any other event specific context key

                // Your logic goes here (Send data to Splunk/Dynatrace/Show Popup...)
            }
        }, intentFilter);
    }
    //    private boolean isMagiskHideEnabled() {
//        PackageManager packageManager = getPackageManager();
//        try {
//            packageManager.getPackageInfo("com.topjohnwu.magisk", PackageManager.GET_ACTIVITIES);
//            return true; // O Magisk Manager está instalado
//        } catch (PackageManager.NameNotFoundException e) {
//            return false; // O Magisk Manager não está instalado
//        }
//    }

//teste de detectar processos
    public static String responseAppList(Context ctx){

        StringBuilder stringBuilder = new StringBuilder();
        PackageManager packageManager = ctx.getPackageManager();
        List<ApplicationInfo> applications = packageManager.getInstalledApplications(0);
        for (ApplicationInfo appInfo : applications) {
            stringBuilder.append(appInfo.packageName).append("\n");
        }
        return stringBuilder.toString();
    }

    public static boolean checkQEmuDrivers() {
        for (File drivers_file : new File[]{new File("ro.build.magisk.hide=1"), new File("ro.build.prop")}) {
            if (drivers_file.exists() && drivers_file.canRead()) {
                byte[] data = new byte[1024];
                try {
                    InputStream is = new FileInputStream(drivers_file);
                    is.read(data);
                    is.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                String driver_data = new String(data);
                for (String known_qemu_driver : QEMU_DRIVERS) {
                    if (driver_data.contains(known_qemu_driver)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    public static final String[] QEMU_DRIVERS = {"goldfish"};

    private static String getProp(Context context, String property) {
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class<?> systemProperties = classLoader.loadClass("android.os.SystemProperties");

            Method get = systemProperties.getMethod("get", String.class);

            Object[] params = new Object[1];
            params[0] = property;

            return (String) get.invoke(systemProperties, params);
        } catch (Exception exception) {
            // empty catch
        }
        return null;
    }

    private static final Property[] PROPERTIES = {
            new Property("ro.build.magisk.hide=1", null),
//            new Property("ro.build.prop", null)
    };

    private static boolean checkQEmuProps(Context context) {
        int found_props = 0;

        for (Property property : PROPERTIES) {
            String property_value = getProp(context, property.name);
            if ((property.seek_value == null) && (property_value != null)) {
                found_props++;
            }
            if ((property.seek_value != null)
                    && (property_value.contains(property.seek_value))) {
                found_props++;
            }

        }

        if (found_props >= 1) {
            return true;
        }
        return false;
    }
    private static String GETPROP_EXECUTABLE_PATH = "/system/bin/getprop";
    private static String TAG = "MyApp";

//    public static String read(String propName) {
//        Process process = null;
//        BufferedReader bufferedReader = null;
//
//        try {
//            process = new ProcessBuilder().command(GETPROP_EXECUTABLE_PATH, propName).redirectErrorStream(true).start();
//            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line = bufferedReader.readLine();
//            if (line == null){
//                line = ""; //prop not set
//            }
//            Log.i(TAG,"read System Property: " + propName + "=" + line);
//            return line;
//        } catch (Exception e) {
//            Log.e(TAG,"Failed to read System Property " + propName,e);
//            return "";
//        } finally{
//            if (bufferedReader != null){
//                try {
//                    bufferedReader.close();
//                } catch (IOException e) {}
//            }
//            if (process != null){
//                process.destroy();
//            }
//        }
//    }

//    public static String check2() {
//        String path = Environment.getExternalStorageDirectory().toString()+"/data";
//        Log.d("Files", "Path: " + path);
//        File directory = new File(path);
//        File[] files = directory.listFiles();
//        Log.d("Files", "Size: "+ files.length);
//        for (int i = 0; i < files.length; i++)
//        {
//            Log.d("Files", "FileName:" + files[i].getName());
//        }
//        return files.toString();
//    }

}
