package com.shizhefei.eventbus.plugin;

import com.shizhefei.eventbus.EventProxyNameBuilder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Created by LuckyJayce on 2017/4/29.
 */

public class EventProxyBuilder {
    private Messager messager;

    public EventProxyBuilder(Messager messager) {
        this.messager = messager;
    }

    public JavaFile build(boolean isRemoteEvent, TypeElement typeElement) {
        PackageElement packageElement = getPackage(typeElement);

        String eventImpPackageName = packageElement.getQualifiedName().toString();
        String proxySimpleClassName = EventProxyNameBuilder.getProxySimpleClassName(eventImpPackageName, typeElement.getQualifiedName().toString());

        TypeName eventClass = TypeName.get(typeElement.asType());
        ParameterizedTypeName eventImpTypeName = ParameterizedTypeName.get(TypeUtil.EventProxy, eventClass);
        TypeSpec eventImp = createEventImp(isRemoteEvent, typeElement, eventImpPackageName, proxySimpleClassName, eventImpTypeName, eventClass);
        JavaFile javaFile = JavaFile.builder(eventImpPackageName, eventImp).indent("\t").build();
        return javaFile;
    }

    private PackageElement getPackage(Element cla) {
        if (cla instanceof PackageElement) {
            return (PackageElement) cla;
        }
        return getPackage(cla.getEnclosingElement());
    }


    /**
     * <pre>
     * private static class MessageEventImp extends EventHandler.EventProxy<IMessageEvent> implements IMessageEvent {
     *    @Override
     *    public void onReceiverMessage(int messageId, String message) {
     *        for (IMessageEvent iEvent : iEvents) {
     *            iEvent.onReceiverMessage(messageId, message);
     *        }
     *    }
     * }<pre/>
     */
    private TypeSpec createEventImp(boolean isRemoteEvent, TypeElement typeElement, String eventImpPackageName, String proxySimpleClassName, ParameterizedTypeName eventImpTypeName, TypeName eventClass) {
        TypeSpec.Builder eventImp = TypeSpec.classBuilder(proxySimpleClassName).addModifiers(PUBLIC);
        eventImp.addSuperinterface(eventClass);
        for (TypeParameterElement typeParameterElement : typeElement.getTypeParameters()) {
            eventImp.addTypeVariable(toTypeVariableName(typeParameterElement));
        }
        eventImp.superclass(eventImpTypeName);
        List<ExecutableElement> executableElementList = new ArrayList<>();
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        for (int i = 0; i < enclosedElements.size(); i++) {
            Element element = enclosedElements.get(i);
            if (element instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) element;
                MethodSpec methodSpec = createEventImpMethod(isRemoteEvent, i, eventImpPackageName, proxySimpleClassName, eventClass, executableElement);
                eventImp.addMethod(methodSpec);

                executableElementList.add(executableElement);
            }
        }
        if (isRemoteEvent) {
            eventImp.addMethod(createOnRemoteEventMethod(executableElementList));
        }
        return eventImp.build();
    }

    /**
     * 将TypeParameterElement 转换为TypeVariableName
     *
     * @param typeParameterElement
     * @return
     */
    private TypeVariableName toTypeVariableName(TypeParameterElement typeParameterElement) {
        List<? extends TypeMirror> bounds1 = typeParameterElement.getBounds();
        TypeName[] bounds = new TypeName[bounds1.size()];
        for (int i = 0; i < bounds1.size(); i++) {
            bounds[i] = TypeName.get(bounds1.get(i));
        }
        return TypeVariableName.get(typeParameterElement.getSimpleName().toString(), bounds);
    }

    //    @Override
//    public void onReceiverMessage(final int messageId, final String message) {
//        if (!TextUtils.isEmpty(processName)) {
//            Bundle eventRemoteData = new Bundle();
//            eventRemoteData.putString("eventProxyClassName", getClass().getName());
//            eventRemoteData.putInt("methodIndex", 0);
//            eventRemoteData.putInt("paramValue1", messageId);
//            eventRemoteData.putString("paramValue2", message);
//            Util.postRemote(processName, eventRemoteData);
//        } else {
//            for (final Register<IMessageEvent> register : registers) {
//                Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                       IMessageEvent eventReceiver = register.getReceiver();
//                       if (eventReceiver != null) {
//                           eventReceiver.onReceiveMessage(message);
//                       }
//                    }
//                };
//                Util.post(register.getReceiver(), isPostMainThread, runnable);
//            }
//        }
//    }
    private MethodSpec createEventImpMethod(boolean isRemoteEvent, int methodIndex, String eventImpPackageName, String eventProxyName, TypeName eventClass, ExecutableElement executableElement) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString());
        methodBuilder.addAnnotation(Override.class);
        Set<Modifier> modifiers = new HashSet<>(executableElement.getModifiers());
        modifiers.remove(Modifier.ABSTRACT);
        methodBuilder.addModifiers(modifiers);
        methodBuilder.returns(TypeName.get(executableElement.getReturnType()));
        for (TypeMirror typeMirror : executableElement.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(typeMirror));
        }
        for (TypeParameterElement typeParameterElement : executableElement.getTypeParameters()) {
            methodBuilder.addTypeVariable(toTypeVariableName(typeParameterElement));
        }

        List<? extends VariableElement> typeParameters = executableElement.getParameters();
        StringBuilder invokeStringBuilder = new StringBuilder("eventReceiver.").append(executableElement.getSimpleName().toString()).append("(");
        for (VariableElement typeParameter : typeParameters) {
            String paramsName = typeParameter.getSimpleName().toString();
            methodBuilder.addParameter(TypeName.get(typeParameter.asType()), paramsName, Modifier.FINAL);
            invokeStringBuilder.append(paramsName).append(", ");
        }
        if (invokeStringBuilder.charAt(invokeStringBuilder.length() - 2) == ',' && invokeStringBuilder.charAt(invokeStringBuilder.length() - 1) == ' ') {
            invokeStringBuilder.deleteCharAt(invokeStringBuilder.length() - 1);
            invokeStringBuilder.deleteCharAt(invokeStringBuilder.length() - 1);
        }
        invokeStringBuilder.append(")");
        messager.printMessage(Diagnostic.Kind.NOTE, " createEventImpMethod stringBuilder:" + invokeStringBuilder + "  executableElement:" + executableElement);
        if (isRemoteEvent) {
            String eventClassName = EventProxyNameBuilder.getRemoteClassParamName();
            methodBuilder.beginControlFlow("if (!$T.isEmpty(processName))", TypeUtil.TextUtils);
            methodBuilder.addStatement("$T eventRemoteData = new $T()", TypeUtil.bundle, TypeUtil.bundle);
            methodBuilder.addStatement("eventRemoteData.putString(\"" + eventClassName + "\", $T.class.getName())", eventClass);
            methodBuilder.addStatement("eventRemoteData.putInt(\"methodIndex\", $L)", methodIndex);
            for (int i = 0; i < typeParameters.size(); i++) {
                VariableElement variableElement = typeParameters.get(i);
//                TypeName typeName = TypeName.get(variableElement.asType());
                TypeMirror typeMirror = variableElement.asType();
                String paramsName = variableElement.getSimpleName().toString();
                if (TypeUtil.isIInterfaceType(typeMirror)) {
                    methodBuilder.addStatement("$T.putBinder(eventRemoteData, \"paramValue$L\", ($T)$L)", TypeUtil.Util, i, TypeUtil.IBinder, paramsName);
                } else {
                    String putMethodName = TypeUtil.getBundlePutMethodName(variableElement.asType());
                    if (putMethodName == null) {
                        methodBuilder.addStatement("//not support this type, only support java baseType,baseType's array,bundle,implements Parcelable,Parcelable[],implements Serializable");
                    }
                    methodBuilder.addStatement("eventRemoteData.$L(\"paramValue$L\", $L)", putMethodName, i, paramsName);
                }
            }
            methodBuilder.addStatement("$T.postRemote(processName, eventRemoteData)", TypeUtil.Util);

            methodBuilder.nextControlFlow("else");
        }

        ParameterizedTypeName registerWithEventTypeName = ParameterizedTypeName.get(TypeUtil.Register, eventClass);

        VariableElement fisrtParamIsFilter = null;
        if (!typeParameters.isEmpty()) {
            VariableElement variableElement = typeParameters.get(0);
            if (variableElement != null && TypeUtil.isFilterType(variableElement.asType())) {
                fisrtParamIsFilter = variableElement;
            }
        }

        methodBuilder.beginControlFlow("for (final $T register : registers)", registerWithEventTypeName);
        if (fisrtParamIsFilter != null) {
//            if (filter == null || filter.accept(register.getReceiver())) {
            String paramsName = fisrtParamIsFilter.getSimpleName().toString();
            methodBuilder.beginControlFlow("if ($L == null || $L.accept(register.getReceiver()))", paramsName, paramsName);
        }

        methodBuilder.beginControlFlow("$T runnable = new $T()", Runnable.class, Runnable.class);
        methodBuilder.addCode("@Override\n");
        methodBuilder.beginControlFlow("public void run()");
        methodBuilder.addStatement("$T eventReceiver = register.getReceiver()", eventClass);
        methodBuilder.beginControlFlow("if (eventReceiver != null)");
        methodBuilder.addStatement(invokeStringBuilder.toString());
        methodBuilder.endControlFlow();
        methodBuilder.endControlFlow();
        methodBuilder.endControlFlow("");

        methodBuilder.addStatement("$T.post(register.getReceiver(), isPostMainThread, runnable)", TypeUtil.Util);

        if (fisrtParamIsFilter != null) {
            methodBuilder.endControlFlow();
        }

        methodBuilder.endControlFlow();

        if (isRemoteEvent) {
            methodBuilder.endControlFlow();
        }
        return methodBuilder.build();
    }

//====================== 旧版createEventImpMethod start  ========
    //    @Override
//    public void onReceiverMessage(final int messageId, final String message) {
//        if (!TextUtils.isEmpty(processName)) {
//            Bundle eventRemoteData = new Bundle();
//            eventRemoteData.putString("eventProxyClassName", getClass().getName());
//            eventRemoteData.putInt("methodIndex", 0);
//            eventRemoteData.putInt("paramValue1", messageId);
//            eventRemoteData.putString("paramValue2", message);
//            Util.postRemote(processName, eventRemoteData);
//        } else {
//            for (final IMessageEvent iEvent : iEvents) {
//                Subscribe subscribe = iEvent.getClass().getAnnotation(Subscribe.class);
//                int receiveThreadMode = subscribe == null ? Subscribe.POSTING : subscribe.receiveThreadMode();
//                if (Util.isSyncInvoke(isPostMainThread, receiveThreadMode)) {
//                    iEvent.onReceiverMessage(messageId, message);
//                } else {
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            iEvent.onReceiverMessage(messageId, message);
//                        }
//                    };
//                    if ((receiveThreadMode == Subscribe.MAIN) || (receiveThreadMode == Subscribe.POSTING && isPostMainThread)) {
//                        Util.postMain(runnable);
//                    } else {
//                        Util.postThread(runnable);
//                    }
//                }
//            }
//        }
//    }
//    private MethodSpec createEventImpMethod(boolean isRemoteEvent, int methodIndex, String eventImpPackageName, String eventProxyName, TypeName eventClass, ExecutableElement executableElement) {
//        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString());
//        methodBuilder.addAnnotation(Override.class);
//        Set<Modifier> modifiers = new HashSet<>(executableElement.getModifiers());
//        modifiers.remove(Modifier.ABSTRACT);
//        methodBuilder.addModifiers(modifiers);
//        methodBuilder.returns(TypeName.get(executableElement.getReturnType()));
//        for (TypeMirror typeMirror : executableElement.getThrownTypes()) {
//            methodBuilder.addException(TypeName.get(typeMirror));
//        }
//        for (TypeParameterElement typeParameterElement : executableElement.getTypeParameters()) {
//            methodBuilder.addTypeVariable(toTypeVariableName(typeParameterElement));
//        }
//
//        List<? extends VariableElement> typeParameters = executableElement.getParameters();
//        StringBuilder invokeStringBuilder = new StringBuilder("iEvent.").append(executableElement.getSimpleName().toString()).append("(");
//        for (VariableElement typeParameter : typeParameters) {
//            String paramsName = typeParameter.getSimpleName().toString();
//            methodBuilder.addParameter(TypeName.get(typeParameter.asType()), paramsName, Modifier.FINAL);
//            invokeStringBuilder.append(paramsName).append(", ");
//        }
//        if (invokeStringBuilder.charAt(invokeStringBuilder.length() - 2) == ',' && invokeStringBuilder.charAt(invokeStringBuilder.length() - 1) == ' ') {
//            invokeStringBuilder.deleteCharAt(invokeStringBuilder.length() - 1);
//            invokeStringBuilder.deleteCharAt(invokeStringBuilder.length() - 1);
//        }
//        invokeStringBuilder.append(")");
//        messager.printMessage(Diagnostic.Kind.NOTE, " createEventImpMethod stringBuilder:" + invokeStringBuilder + "  executableElement:" + executableElement);
//        if (isRemoteEvent) {
//            methodBuilder.beginControlFlow("if(!$T.isEmpty(processName))", TypeUtil.TextUtils);
//            methodBuilder.addStatement("$T eventRemoteData = new $T()", TypeUtil.bundle, TypeUtil.bundle);
//            methodBuilder.addStatement("eventRemoteData.putString(\"eventProxyClassName\", getClass().getName())");
//            methodBuilder.addStatement("eventRemoteData.putInt(\"methodIndex\", $L)", methodIndex);
//            for (int i = 0; i < typeParameters.size(); i++) {
//                VariableElement variableElement = typeParameters.get(i);
////                TypeName typeName = TypeName.get(variableElement.asType());
//                String putMethodName = TypeUtil.getBundlePutMethodName(variableElement.asType());
//                String paramsName = variableElement.getSimpleName().toString();
//                if (putMethodName == null) {
//                    methodBuilder.addStatement("//not support this type, only support java baseType,baseType's array,bundle,implements Parcelable,Parcelable[],implements Serializable");
//                }
//                methodBuilder.addStatement("eventRemoteData.$L(\"paramValue$L\", $L)", putMethodName, i, paramsName);
//            }
//            methodBuilder.addStatement("$T.postRemote(processName, eventRemoteData)", TypeUtil.Util);
//
//            methodBuilder.nextControlFlow("else");
//        }
//
//        methodBuilder.beginControlFlow("for(final $T iEvent : iEvents)", eventClass);
//        methodBuilder.addStatement("$T subscribe = iEvent.getClass().getAnnotation($T.class)", TypeUtil.Subscribe, TypeUtil.Subscribe);
//        methodBuilder.addStatement("int receiveThreadMode = subscribe == null ? Subscribe.POSTING : subscribe.receiveThreadMode()");
//        methodBuilder.beginControlFlow("if($T.isSyncInvoke(isPostMainThread, receiveThreadMode))", TypeUtil.Util);
//        methodBuilder.addStatement(invokeStringBuilder.toString());
//        methodBuilder.nextControlFlow("else");
//        methodBuilder.beginControlFlow("$T runnable = new $T()", Runnable.class, Runnable.class);
//        methodBuilder.addCode("@Override\n");
//        methodBuilder.beginControlFlow("public void run()");
//        methodBuilder.addStatement(invokeStringBuilder.toString());
//        methodBuilder.endControlFlow();
//        methodBuilder.endControlFlow("");
//
//        methodBuilder.beginControlFlow("if ((receiveThreadMode == Subscribe.MAIN) || (receiveThreadMode == Subscribe.POSTING && isPostMainThread))");
//        methodBuilder.addStatement("$T.postMain(runnable)", TypeUtil.Util);
//        methodBuilder.nextControlFlow("else");
//        methodBuilder.addStatement("$T.postThread(runnable)", TypeUtil.Util);
//        methodBuilder.endControlFlow();
//
//        methodBuilder.endControlFlow();
//        methodBuilder.endControlFlow();
//
//        if (isRemoteEvent) {
//            methodBuilder.endControlFlow();
//        }
//        return methodBuilder.build();
//    }
    //====================== 旧版createEventImpMethod end  ========


    //    @Override
//    public void onRemoteEvent(Bundle eventRemoteData) {
//        int methodIndex = eventRemoteData.getInt("methodIndex");
//        switch (methodIndex) {
//            case 0:
//                onReceiverMessage(eventRemoteData.getInt("paramValue1"), eventRemoteData.getString("paramValue2"));
//                break;
//            case 1:
//                onReceiverMessage(eventRemoteData.getString("paramValue1"), eventRemoteData.getString("paramValue2"));
//                break;
//        }
//    }
    private MethodSpec createOnRemoteEventMethod(List<ExecutableElement> executableElements) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("onRemoteEvent");
        methodBuilder.addParameter(TypeUtil.bundle, "data");
        methodBuilder.addModifiers(Modifier.PUBLIC);
        methodBuilder.addAnnotation(Override.class);
        methodBuilder.addStatement("int methodIndex = data.getInt(\"methodIndex\")");
        methodBuilder.beginControlFlow("switch (methodIndex)");
        for (int i = 0; i < executableElements.size(); i++) {
            ExecutableElement executableElement = executableElements.get(i);
            methodBuilder.addCode("case $L:\n", i);
            List<Object> args = new ArrayList<>();
            StringBuilder invokeMethod = new StringBuilder("\t");
            invokeMethod.append(executableElement.getSimpleName().toString());
            invokeMethod.append("(");
            List<? extends VariableElement> typeParameters = executableElement.getParameters();
            for (int paramIndex = 0; paramIndex < typeParameters.size(); paramIndex++) {
                VariableElement typeParameter = typeParameters.get(paramIndex);
                TypeMirror typeMirror = typeParameter.asType();
                String methodName = TypeUtil.getBundleGetMethodName(typeMirror);
                if (TypeUtil.isIInterfaceType(typeMirror)) {
                    invokeMethod.append("$T.Stub.asInterface(($T)data.$L(\"paramValue$L\"))");
                    args.add(typeParameter.asType());
                    args.add(TypeUtil.IBinder);
                    args.add(methodName);
                    args.add(paramIndex);
                } else if (TypeUtil.isNeedCast(typeMirror)) {
                    invokeMethod.append("($T)data.$L(\"paramValue$L\")");
                    if (paramIndex != typeParameters.size() - 1) {
                        invokeMethod.append(", ");
                    }
                    args.add(TypeName.get(typeParameter.asType()));
                    args.add(methodName);
                    args.add(paramIndex);
                } else {
                    invokeMethod.append("data.$L(\"paramValue$L\")");
                    if (paramIndex != typeParameters.size() - 1) {
                        invokeMethod.append(", ");
                    }
                    args.add(methodName);
                    args.add(paramIndex);
                }
            }
            invokeMethod.append(")");
            methodBuilder.addStatement(invokeMethod.toString(), args.toArray(new Object[args.size()]));
            methodBuilder.addStatement("\tbreak");
        }
        methodBuilder.endControlFlow();
        return methodBuilder.build();
    }
}

//public class IMessageEventProxy extends EventProxy<IMessageEvent> implements IMessageEvent {
//    @Override
//    public void onReceiverMessage(final int messageId, final String message) {
//        if (!TextUtils.isEmpty(processName)) {
//            Bundle eventRemoteData = new Bundle();
//            eventRemoteData.putString("eventProxyClassName", getClass().getName());
//            eventRemoteData.putInt("methodIndex", 0);
//            eventRemoteData.putInt("paramValue1", messageId);
//            eventRemoteData.putString("paramValue2", message);
//            Util.postRemote(processName, eventRemoteData);
//        } else {
//            for (final IMessageEvent iEvent : iEvents) {
//                Subscribe subscribe = iEvent.getClass().getAnnotation(Subscribe.class);
//                int receiveThreadMode = subscribe == null ? Subscribe.POSTING : subscribe.receiveThreadMode();
//                if (Util.isSyncInvoke(isPostMainThread, receiveThreadMode)) {
//                    iEvent.onReceiverMessage(messageId, message);
//                } else {
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            iEvent.onReceiverMessage(messageId, message);
//                        }
//                    };
//                    if ((receiveThreadMode == Subscribe.MAIN) || (receiveThreadMode == Subscribe.POSTING && isPostMainThread)) {
//                        Util.postMain(runnable);
//                    } else {
//                        Util.postThread(runnable);
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onReceiverMessage(final String messageTitle, final String message) {
//        if (!TextUtils.isEmpty(processName)) {
//            Bundle eventRemoteData = new Bundle();
//            eventRemoteData.putSerializable("eventProxyClassName", getClass());
//            eventRemoteData.putInt("methodIndex", 1);
//            eventRemoteData.putString("paramValue1", messageTitle);
//            eventRemoteData.putString("paramValue2", message);
//            Util.postRemote(processName, eventRemoteData);
//        } else {
//            for (final IMessageEvent iEvent : iEvents) {
//                Subscribe subscribe = iEvent.getClass().getAnnotation(Subscribe.class);
//                int receiveThreadMode = subscribe == null ? Subscribe.POSTING : subscribe.receiveThreadMode();
//                if (Util.isSyncInvoke(isPostMainThread, receiveThreadMode)) {
//                    iEvent.onReceiverMessage(messageTitle, message);
//                } else {
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            iEvent.onReceiverMessage(messageTitle, message);
//                        }
//                    };
//                    if ((receiveThreadMode == Subscribe.MAIN) || (receiveThreadMode == Subscribe.POSTING && isPostMainThread)) {
//                        Util.postMain(runnable);
//                    } else {
//                        Util.postThread(runnable);
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onRemoteEvent(Bundle eventRemoteData) {
//        int methodIndex = eventRemoteData.getInt("methodIndex");
//        switch (methodIndex) {
//            case 0:
//                onReceiverMessage(eventRemoteData.getInt("paramValue1"), eventRemoteData.getString("paramValue2"));
//                break;
//            case 1:
//                onReceiverMessage(eventRemoteData.getString("paramValue1"), eventRemoteData.getString("paramValue2"));
//                break;
//        }
//    }
//}
