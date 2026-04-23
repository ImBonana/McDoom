#include "doomgeneric/doomgeneric/doomgeneric.h"
#include "doomgeneric/doomgeneric/doomkeys.h"
 
#include <jni.h>
#include <stdint.h>
#include <string.h>
#include <stdio.h>

#ifdef _WIN32
    #include <windows.h>
    #define SLEEP_MS(ms) Sleep(ms)
    static uint32_t get_ticks_ms() { return (uint32_t)GetTickCount(); }
#else
    #include <unistd.h>
    #include <sys/time.h>
    #define SLEEP_MS(ms) usleep((ms) * 1000)
    static uint32_t get_ticks_ms() {
        struct timeval tv;
        gettimeofday(&tv, NULL);
        return (uint32_t)((tv.tv_sec * 1000) + (tv.tv_usec / 1000));
    }
#endif
 
static JavaVM *g_jvm = NULL;
static jobject g_bridge_obj = NULL;
static jmethodID g_get_keys = NULL;
static jbyteArray g_frame_array = NULL;
static int g_frame_size = DOOMGENERIC_RESX * DOOMGENERIC_RESY * 4; // RGBA 32-bit
static int g_rungame = 0;

static JNIEnv *get_env() {
    JNIEnv *env = NULL;
    if ((*g_jvm)->GetEnv(g_jvm, (void **)&env, JNI_VERSION_1_8) == JNI_EDETACHED) {
        (*g_jvm)->AttachCurrentThread(g_jvm, (void **)&env, NULL);
    }
    return env;
} 
 
void DG_Init() {
    JNIEnv *env = get_env();
    if (!env) return;
    jbyteArray local = (*env)->NewByteArray(env, g_frame_size);
    g_frame_array = (*env)->NewGlobalRef(env, local);
    (*env)->DeleteLocalRef(env, local);
}
 
void DG_DrawFrame() {
    JNIEnv *env = get_env();
    if (!env) return;
 
    if (!g_frame_array) return;
    (*env)->SetByteArrayRegion(env, g_frame_array, 0, g_frame_size, (const jbyte *)DG_ScreenBuffer);
}
 
void DG_SleepMs(uint32_t ms) {
    SLEEP_MS(ms);
}
 
uint32_t DG_GetTicksMs() {
    return get_ticks_ms();
}
 
int DG_GetKey(int *pressed, unsigned char *doomKey) {
    JNIEnv *env = get_env();
    if (!env || !g_bridge_obj || !g_get_keys) {
        *pressed = 0;
        return 0;
    }
 
    jintArray result = (jintArray)(*env)->CallObjectMethod(env, g_bridge_obj, g_get_keys);
    if (!result) {
        *pressed = 0;
        return 0;
    }
 
    jint buf[2];
    (*env)->GetIntArrayRegion(env, result, 0, 2, buf);
    (*env)->DeleteLocalRef(env, result);
 
    *pressed = (int)buf[0];
    *doomKey = (unsigned char)buf[1];

    return 1;
}
 
void DG_SetWindowTitle(const char *title) { }
 
JNIEXPORT jbyteArray JNICALL 
Java_me_imbanana_mcdoom_doom_DOOM_getFrame(JNIEnv *env, jobject obj) {
    return g_frame_array;
}

JNIEXPORT jint JNICALL 
Java_me_imbanana_mcdoom_doom_DOOM_getWidth(JNIEnv *env, jobject obj) {
    return (jint)DOOMGENERIC_RESX;
}
 

JNIEXPORT jint JNICALL 
Java_me_imbanana_mcdoom_doom_DOOM_getHeight(JNIEnv *env, jobject obj) {
    return (jint)DOOMGENERIC_RESY;
}
 
 
JNIEXPORT void JNICALL
Java_me_imbanana_mcdoom_doom_DOOM_nativeStart(JNIEnv *env, jobject obj, jstring wadPath) {
    setbuf(stdout, NULL);

    (*env)->GetJavaVM(env, &g_jvm);
    g_bridge_obj = (*env)->NewGlobalRef(env, obj);
 
    jclass cls = (*env)->GetObjectClass(env, obj);
    g_get_keys = (*env)->GetMethodID(env, cls, "pollKey", "()[I");
 
    if (!g_get_keys) {
        fprintf(stderr, "[doom-jni] ERROR: could not find Java callback methods\n");
        return;
    }
 
    const char *wad_str = (*env)->GetStringUTFChars(env, wadPath, NULL);
    char wad_copy[1024];
    strncpy(wad_copy, wad_str, sizeof(wad_copy) - 1);
    (*env)->ReleaseStringUTFChars(env, wadPath, wad_str);
 
    char *argv[] = { "doom", "-iwad", wad_copy };
    int argc = 3;
 
    doomgeneric_Create(argc, argv);
    
    g_rungame = 1;

    while (g_rungame) {
        doomgeneric_Tick();
        DG_SleepMs(16);
    }
}

JNIEXPORT void JNICALL
Java_me_imbanana_mcdoom_doom_DOOM_nativeStop(JNIEnv *env, jobject obj) {
    g_rungame = 0;
}