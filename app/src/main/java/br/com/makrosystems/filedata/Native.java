package br.com.makrosystems.filedata;

class Native {

    static {
        System.loadLibrary("native-lib");
    }

    static native boolean isMagiskPresentNative();

}