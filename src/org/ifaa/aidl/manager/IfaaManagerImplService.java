/**
 * Copyright (c) 2018, Spreadtrum Communications Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.ifaa.aidl.manager;

import android.app.Service;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;

import vendor.sprd.hardware.ifaa.V1_0.IIfaa;

import java.util.ArrayList;
import java.util.List;

public class IfaaManagerImplService extends Service {

    private static final String TAG = "IfaaManagerImplService";
    private static final String ACTION_SPRD_FINGER_BIOMANAGER = "com.sprd.fingerprint.startBIOManager";
    private static final int COMMAND_OK = 0;
    private static final int COMMAND_FAIL = -1;
    private static final int BIOMETRICS_AVAILABLE = 1000;
    private static final int SYSTEM_LOCKED = 1001;
    private static final int BIOMETRICS_REG_NONE = 1002;
    private static final int SCREEN_LOCK_NONE = 1003;
    private IIfaa mService;

    private enum BIO_TYPE {
        NONE,
        FINGERPRINT,
        IRIS,
        FACE
    }

    private enum VERSION {
        ZERO,
        ONE,
        TWO,
        THREE,
        FOUR
    }

    public IfaaManagerImplService() {
        Log.v(TAG, "IfaaManagerImplService...");
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "OnCreate...");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "OnBind...");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy...");
    }

    private IIfaa getIFAAService() {
        if (mService == null) {
            try {
                mService = IIfaa.getService();

            } catch (RemoteException e) {
                Log.e(TAG, "Failed to get ifaa service!", e);
                mService = null;
            }
            if (mService != null) {
                Log.d(TAG, "getIFAAService success!");
            } else {
                Log.w(TAG, "ifaa service not available");
            }
        }
        return mService;
    }

    private ArrayList toByteArrayList(byte[] data) {
        if (data == null) {
            return null;
        }
        ArrayList<Byte> result = new ArrayList<Byte>(data.length);
        for (final byte b : data) {
            result.add(b);
        }
        return result;
    }

    private byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

    private final IfaaManagerService.Stub mBinder = new IfaaManagerService.Stub() {

        @Override
        public int getSupportBIOTypes() {
            Log.d(TAG, "getSupportBIOTypes:(" + BIO_TYPE.FINGERPRINT.ordinal() + ")");
            return BIO_TYPE.FINGERPRINT.ordinal();
        }

        @Override
        public int startBIOManager(int authType) {
            Log.d(TAG, "startBIOManager...");

            try {
                String broadcastIntent = ACTION_SPRD_FINGER_BIOMANAGER;
                Intent intent = new Intent(broadcastIntent);
                intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
                sendBroadcast(intent);
            } catch(Exception e) {
                e.printStackTrace();
                return COMMAND_FAIL;
            }
            return COMMAND_OK;
        }

        @Override
        public String getDeviceModel() {
            String deviceMode = "";
            final String mode = SystemProperties.get("ro.product.model", null);
            if (mode != null && mode.startsWith("sp9850kh")) {
                deviceMode = "sprd-9850kh";
            } else if (mode != null && mode.startsWith("sp9850ka")) {
                deviceMode = "sprd-9850ka";
            } else if (mode != null && mode.startsWith("sp9832e")) {
                deviceMode = "sprd-9832e";
            } else if (mode != null && mode.startsWith("sp9850e")) {
                deviceMode = "sprd-9850e";
            } else if (mode != null && mode.startsWith("s9863a")) {
                deviceMode = "sprd-9863a";
            } else if (mode != null && mode.startsWith("ums312")) {
                deviceMode = "unisoc-ums312";
            } else if (mode != null && mode.startsWith("ums512")) {
                deviceMode = "unisoc-ums512";
            } else if (mode != null && mode.startsWith("ud710")) {
                deviceMode = "unisoc-ud710";
            } else {
                deviceMode = "unsupport";
            }
            Log.d(TAG, "getDeviceModel: " + deviceMode);
            return deviceMode;
        }

        @Override
        public byte[] processCmd(byte[] param) {
            byte[] response = null;
            IIfaa ifaaService = getIFAAService();
            Log.d(TAG, "processCmd...");
            if (ifaaService != null) {
                try {
                    response = toByteArray(ifaaService.processCmdV2(toByteArrayList(param)));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        public int getVersion() {
            //to support quzhiwei must return 4
            Log.d(TAG, "getVersion(" + VERSION.FOUR.ordinal() + ")");
            return VERSION.FOUR.ordinal();
        }

        @Override
        //reserved for fingerprint full screen, not support when getVersion returns less than 3
        public String getExtInfo(int authType, String keyExtInfo) {
            Log.d(TAG, "getExtInfo NOT support!!!");
            return null;
        }

        @Override
        //reserved for fingerprint full screen, not support when getVersion returns less than 3
        public void setExtInfo(int authType, String keyExtInfo, String valExtInfo) {
            Log.d(TAG, "setExtInfo NOT support!!!");
            return;
        }

        @Override
        public int getEnabled(int bioType) {
            int enabled = BIOMETRICS_AVAILABLE;

            final KeyguardManager keyguardManager = (KeyguardManager) getSystemService(
                    Context.KEYGUARD_SERVICE);
            if (keyguardManager == null || !keyguardManager.isDeviceSecure()) {
                enabled = SCREEN_LOCK_NONE;
            }

            FingerprintManager mFp = (FingerprintManager) getSystemService(
                    Context.FINGERPRINT_SERVICE);

            if (mFp == null || !mFp.hasEnrolledTemplates()) {
                enabled = BIOMETRICS_REG_NONE;
            }
            Log.d(TAG, "getEnabled: [" + enabled + "]");
            return enabled;
        }

        @Override
        public int[] getIDList(int bioType) {
            int id = 0;
            List<Integer> ids = new ArrayList<Integer>();

            FingerprintManager mFp = (FingerprintManager) getSystemService(
                    Context.FINGERPRINT_SERVICE);
            final List<Fingerprint> items = mFp.getEnrolledFingerprints();
            if (items != null) {
                for (id = 0; id < items.size(); id++) {
                    ids.add(items.get(id).getBiometricId());
                    Log.d(TAG, "getIDList[" + id + "] = " + items.get(id).getBiometricId());
                }
            }

            return (ids == null || ids.size() < 1) ? null
                    : ids.stream().mapToInt(Integer::valueOf).toArray();
        }
    };
}
