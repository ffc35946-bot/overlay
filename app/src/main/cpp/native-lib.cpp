#include <jni.h>
#include "w2s.h"

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_overlay_OverlayView_nativeDraw(JNIEnv* env, jobject /*thiz*/,
                                                jobject canvas, jobject paint,
                                                jfloatArray jBoxes, jintArray jBoxColors,
                                                jfloatArray jLines, jintArray jLineColors) {
    jclass canvasCls = env->GetObjectClass(canvas);
    jclass paintCls  = env->GetObjectClass(paint);
    jmethodID drawRect = env->GetMethodID(canvasCls, "drawRect", "(FFFFLandroid/graphics/Paint;)V");
    jmethodID drawLine = env->GetMethodID(canvasCls, "drawLine", "(FFFFLandroid/graphics/Paint;)V");
    jmethodID setColor = env->GetMethodID(paintCls, "setColor", "(I)V");
    if (!drawRect || !drawLine || !setColor) return;

    jsize nBoxes = env->GetArrayLength(jBoxColors);
    jsize nLines = env->GetArrayLength(jLineColors);
    jfloat* boxes      = env->GetFloatArrayElements(jBoxes, nullptr);
    jint*   boxColors  = env->GetIntArrayElements(jBoxColors, nullptr);
    jfloat* lines      = env->GetFloatArrayElements(jLines, nullptr);
    jint*   lineColors = env->GetIntArrayElements(jLineColors, nullptr);

    for (jsize i = 0; i < nBoxes; i++) {
        float x = boxes[i * 4], y = boxes[i * 4 + 1], w = boxes[i * 4 + 2], h = boxes[i * 4 + 3];
        env->CallVoidMethod(paint, setColor, boxColors[i]);
        env->CallVoidMethod(canvas, drawRect, x, y, x + w, y + h, paint);
    }
    for (jsize i = 0; i < nLines; i++) {
        const float* s = &lines[i * 4];
        env->CallVoidMethod(paint, setColor, lineColors[i]);
        env->CallVoidMethod(canvas, drawLine, s[0], s[1], s[2], s[3], paint);
    }

    env->ReleaseFloatArrayElements(jBoxes, boxes, JNI_ABORT);
    env->ReleaseIntArrayElements(jBoxColors, boxColors, JNI_ABORT);
    env->ReleaseFloatArrayElements(jLines, lines, JNI_ABORT);
    env->ReleaseIntArrayElements(jLineColors, lineColors, JNI_ABORT);
    env->DeleteLocalRef(canvasCls);
    env->DeleteLocalRef(paintCls);
}

JNIEXPORT jfloatArray JNICALL
Java_com_example_overlay_OverlayView_nativeWorldToScreen(JNIEnv* env, jclass /*cls*/,
                                                         jfloatArray jm, jfloat x, jfloat y, jfloat z,
                                                         jint width, jint height) {
    if (env->GetArrayLength(jm) < 16) return nullptr;
    jfloat m[16];
    env->GetFloatArrayRegion(jm, 0, 16, m);

    Vector3 p = { x, y, z };
    Vector2 s;
    if (!WorldToScreen(m, p, width, height, &s)) return nullptr;

    jfloatArray out = env->NewFloatArray(2);
    jfloat v[2] = { s.x, s.y };
    env->SetFloatArrayRegion(out, 0, 2, v);
    return out;
}

} // extern "C"
