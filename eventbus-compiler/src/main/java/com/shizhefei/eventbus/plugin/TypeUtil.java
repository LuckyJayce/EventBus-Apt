package com.shizhefei.eventbus.plugin;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Created by LuckyJayce on 2017/3/21.
 */

public class TypeUtil {
    public static final ClassName eventHandler = ClassName.get("com.shizhefei.eventbus", "EventHandler");
    public static final ClassName ieventHandler = ClassName.get("com.shizhefei.eventbus", "IEventHandler");
    public static final ClassName Subscribe = ClassName.get("com.shizhefei.eventbus", "Subscribe");
    public static final ClassName Util = ClassName.get("com.shizhefei.eventbus", "Util");
    public static final ClassName activity = ClassName.get("android.app", "Activity");
    public static final ClassName EventProxy = ClassName.get("com.shizhefei.eventbus", "EventProxy");
    public static final ClassName EventProxyFactory = ClassName.get("com.shizhefei.eventbus", "EventHandler.EventImpFactory");
    public static final ClassName ievent = ClassName.get("com.shizhefei.eventbus", "IEvent");
    public static final ClassName NonNull = ClassName.get("android.support.annotation", "NonNull");
    public static final ClassName iRemoteEvent = ClassName.get("com.shizhefei.eventbus", "IRemoteEvent");
    public static final ClassName bundle = ClassName.get("android.os", "Bundle");
    public static final ClassName TextUtils = ClassName.get("android.text", "TextUtils");
    public static final ClassName Parcelable = ClassName.get("android.os", "Parcelable");
    public static final TypeName ParcelableArray = ArrayTypeName.of(Parcelable);
    public static final ClassName serializable = ClassName.get(Serializable.class);
    public static final ArrayTypeName serializableArray = ArrayTypeName.of(serializable);
    private static ProcessingEnvironment staticProcessingEnvironment;
    private static TypeMirror parcelableType;
    private static TypeMirror serializableType;

    public static String getBundlePutMethodName(TypeMirror typeMirror) {
        TypeName typeName = ClassName.get(typeMirror);
        String method = bundleMethodNames.get(typeName);
        if (method != null) {
            return method;
        }
        if (staticProcessingEnvironment.getTypeUtils().isSubtype(typeMirror, parcelableType)) {
            return bundleMethodNames.get(Parcelable);
        }
        if (TypeKind.ARRAY == typeMirror.getKind() && typeMirror instanceof ArrayType) {
            TypeMirror componentType = ((ArrayType) typeMirror).getComponentType();
            if (staticProcessingEnvironment.getTypeUtils().isSubtype(componentType, parcelableType)) {
                return bundleMethodNames.get(ParcelableArray);
            }
        }
        if (isSerializableType(typeMirror)) {
            return bundleMethodNames.get(serializable);
        }
        return null;
    }

    public static boolean isNeedCast(TypeMirror typeMirror) {
        TypeName typeName = ClassName.get(typeMirror);
        return !bundleMethodNames.containsKey(typeName);
    }

    public static String getBundleGetMethodName(TypeMirror typeMirror) {
        String method = getBundlePutMethodName(typeMirror);
        if (method != null) {
            return method.replaceFirst("put", "get");
        }
        return method;
    }

    private static boolean isSerializableType(TypeMirror typeMirror) {
        //由于非序列化的类数组也是serializable，但是又序列化不了，所有要判断数组的单个类的类型
        if (TypeKind.ARRAY == typeMirror.getKind() && typeMirror instanceof ArrayType) {
            TypeMirror componentType = ((ArrayType) typeMirror).getComponentType();
            return isSerializableType(componentType);
        }
        return staticProcessingEnvironment.getTypeUtils().isSubtype(typeMirror, serializableType);
    }

    private static Map<TypeName, String> bundleMethodNames = new HashMap<>();

    static {
        bundleMethodNames.put(TypeName.INT, "putInt");
        bundleMethodNames.put(ClassName.get(Integer.class), "putInt");
        bundleMethodNames.put(ClassName.get(String.class), "putString");
        bundleMethodNames.put(TypeName.FLOAT, "putFloat");
        bundleMethodNames.put(ClassName.get(Float.class), "putFloat");
        bundleMethodNames.put(TypeName.DOUBLE, "putDouble");
        bundleMethodNames.put(ClassName.get(Double.class), "putDouble");
        bundleMethodNames.put(TypeName.SHORT, "putShort");
        bundleMethodNames.put(ClassName.get(Short.class), "putShort");
        bundleMethodNames.put(TypeName.CHAR, "putChar");
        bundleMethodNames.put(ClassName.get(Character.class), "putChar");
        bundleMethodNames.put(TypeName.LONG, "putLong");
        bundleMethodNames.put(ClassName.get(Long.class), "putLong");
        bundleMethodNames.put(TypeName.BYTE, "putByte");
        bundleMethodNames.put(ClassName.get(Byte.class), "putByte");
        bundleMethodNames.put(TypeName.BOOLEAN, "putBoolean");
        bundleMethodNames.put(ClassName.get(Boolean.class), "putBoolean");
        bundleMethodNames.put(ArrayTypeName.of(TypeName.INT), "putIntArray");
        bundleMethodNames.put(ArrayTypeName.of(String.class), "putStringArray");
        bundleMethodNames.put(ArrayTypeName.of(TypeName.FLOAT), "putFloatArray");
        bundleMethodNames.put(ArrayTypeName.of(TypeName.DOUBLE), "putDoubleArray");
        bundleMethodNames.put(ArrayTypeName.of(TypeName.SHORT), "putShortArray");
        bundleMethodNames.put(ArrayTypeName.of(TypeName.CHAR), "putCharArray");
        bundleMethodNames.put(ArrayTypeName.of(TypeName.LONG), "putLongArray");
        bundleMethodNames.put(ArrayTypeName.of(TypeName.BYTE), "putByteArray");
        bundleMethodNames.put(ArrayTypeName.of(TypeName.BOOLEAN), "putBooleanArray");
        bundleMethodNames.put(ClassName.get(CharSequence.class), "putCharSequence");
        bundleMethodNames.put(ArrayTypeName.of(CharSequence.class), "putCharSequenceArray");
        bundleMethodNames.put(Parcelable, "putParcelable");
        bundleMethodNames.put(ParcelableArray, "putParcelableArray");
        bundleMethodNames.put(bundle, "putBundle");
        bundleMethodNames.put(serializable, "putSerializable");
        bundleMethodNames.put(serializableArray, "putSerializableArray");
    }

    public static void init(ProcessingEnvironment processingEnvironment) {
        staticProcessingEnvironment = processingEnvironment;
        Elements elementUtils = staticProcessingEnvironment.getElementUtils();
        parcelableType = elementUtils.getTypeElement("android.os.Parcelable").asType();
        serializableType = elementUtils.getTypeElement(Serializable.class.getName()).asType();
        //        DeclaredType wildcardMap = typeUtils.getDeclaredType(
//                elementUtils.getTypeElement("java.util.Map"),
//                typeUtils.getWildcardType(null, null),
//                typeUtils.getWildcardType(null, null));
    }
}
