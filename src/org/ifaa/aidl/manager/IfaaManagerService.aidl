package org.ifaa.aidl.manager;

interface IfaaManagerService {
    //指纹类型
    const int  TYPE_FINGERPRINT            = 1;
    //人脸类型
    const int  TYPE_FACE                   = 4;
    //keyExtInfo值，用于全面屏手机校验前调用。
    const String KEY_FINGERPRINT_FULLVIEW       = "org.ifaa.ext.key.CUSTOM_VIEW";
    //keyExtInfo值，用于获取特定校验类型的。
    const String KEY_GET_SENSOR_LOCATION        = "org.ifaa.ext.key.GET_SENSOR_LOCATION";


    int getSupportBIOTypes();

    int startBIOManager(int authType);

    String getDeviceModel();

    byte[] processCmd(inout byte[] param);

    int getVersion();

    String getExtInfo(int authType, String keyExtInfo);

    void setExtInfo(int authType, String keyExtInfo, String valExtInfo);

    int getEnabled(int bioType);

    int[] getIDList(int bioType);
}
