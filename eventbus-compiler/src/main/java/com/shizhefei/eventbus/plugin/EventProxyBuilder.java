package com.shizhefei.eventbus.plugin;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
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

    public JavaFile build(TypeElement typeElement) {
        PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
        String eventProxyClassName = createEventProxyClassName(typeElement.getSimpleName());
        TypeName eventClass = TypeName.get(typeElement.asType());
        ParameterizedTypeName eventImpTypeName = ParameterizedTypeName.get(TypeUtil.EventProxy, eventClass);
        TypeSpec eventImp = createEventImp(typeElement, eventProxyClassName, eventImpTypeName, eventClass);
        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), eventImp).indent("\t").build();
        return javaFile;
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
    private TypeSpec createEventImp(TypeElement typeElement, String eventImpName, ParameterizedTypeName eventImpTypeName, TypeName eventClass) {
        TypeSpec.Builder eventImp = TypeSpec.classBuilder(eventImpName).addModifiers(PUBLIC);
        eventImp.addSuperinterface(eventClass);
        for (TypeParameterElement typeParameterElement : typeElement.getTypeParameters()) {
            eventImp.addTypeVariable(toTypeVariableName(typeParameterElement));
        }
        eventImp.superclass(eventImpTypeName);
        for (Element element : typeElement.getEnclosedElements()) {
            ExecutableElement executableElement = (ExecutableElement) element;
            MethodSpec methodSpec = createEventImpMethod(eventClass, executableElement);
            eventImp.addMethod(methodSpec);
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

    /**
     *
     * @param name
     * @return
     */
    private String createEventProxyClassName(Name name) {
        String s = name.toString();
        return s + "Proxy";
    }

    //            @Override
//            public void onReceiverMessage(int messageId, String message) {
//                for (IMessageEvent iEvent : iEvents) {
//                    Subscribe subscribe = iEvent.getClass().getAnnotation(Subscribe.class);
//                    boolean isPostMainThread = isPostMainThread();
//                    int receiveThreadMode = subscribe == null ? Subscribe.POSTING : subscribe.receiveThreadMode();
//                    if (Util.isSyncInvoke(isPostMainThread, receiveThreadMode)) {
//                        iEvent.onReceiverMessage(messageId, message);
//                    } else {
//                        final IMessageEvent post_iEvent = iEvent;
//                        final int post_messageId = messageId;
//                        final String post_message = message;
//                        Runnable runnable = new Runnable() {
//                            @Override
//                            public void run() {
//                                post_iEvent.onReceiverMessage(post_messageId, post_message);
//                            }
//                        };
//                        if ((receiveThreadMode == Subscribe.MAIN) || (receiveThreadMode == Subscribe.POSTING && isPostMainThread)) {
//                            Util.postMain(runnable);
//                        } else {
//                            Util.postThread(runnable);
//                        }
//                    }
//                }
//            }
//        }
    private MethodSpec createEventImpMethod(TypeName eventClass, ExecutableElement executableElement) {
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
        StringBuilder invokeStringBuilder = new StringBuilder("iEvent.").append(executableElement.getSimpleName().toString()).append("(");
        StringBuilder invokeRunnableStringBuilder = new StringBuilder("post_iEvent.").append(executableElement.getSimpleName().toString()).append("(");
        for (VariableElement typeParameter : typeParameters) {
            String paramsName = typeParameter.getSimpleName().toString();
            methodBuilder.addParameter(TypeName.get(typeParameter.asType()), paramsName);
            invokeStringBuilder.append(paramsName).append(", ");
            invokeRunnableStringBuilder.append("post_").append(paramsName).append(", ");
        }
        if (invokeStringBuilder.charAt(invokeStringBuilder.length() - 2) == ',' && invokeStringBuilder.charAt(invokeStringBuilder.length() - 1) == ' ') {
            invokeStringBuilder.deleteCharAt(invokeStringBuilder.length() - 1);
            invokeStringBuilder.deleteCharAt(invokeStringBuilder.length() - 1);
            invokeRunnableStringBuilder.deleteCharAt(invokeRunnableStringBuilder.length() - 1);
            invokeRunnableStringBuilder.deleteCharAt(invokeRunnableStringBuilder.length() - 1);
        }
        invokeStringBuilder.append(")");
        invokeRunnableStringBuilder.append(")");
        messager.printMessage(Diagnostic.Kind.NOTE, " createEventImpMethod stringBuilder:" + invokeStringBuilder + "  executableElement:" + executableElement);
        methodBuilder.beginControlFlow("for($T iEvent : iEvents)", eventClass);
        methodBuilder.addStatement("$T subscribe = iEvent.getClass().getAnnotation($T.class)", TypeUtil.Subscribe, TypeUtil.Subscribe);
        methodBuilder.addStatement("int receiveThreadMode = subscribe == null ? Subscribe.POSTING : subscribe.receiveThreadMode()");
        methodBuilder.beginControlFlow("if($T.isSyncInvoke(isPostMainThread, receiveThreadMode))", TypeUtil.Util);
        methodBuilder.addStatement(invokeStringBuilder.toString());
        methodBuilder.nextControlFlow("else");
        methodBuilder.addStatement("final $T post_iEvent = iEvent", eventClass);
        for (VariableElement typeParameter : typeParameters) {
            String p = typeParameter.getSimpleName().toString();
            methodBuilder.addStatement("final $T $L = $L", TypeName.get(typeParameter.asType()), "post_" + p, p);
        }
        methodBuilder.beginControlFlow("$T runnable = new $T()", Runnable.class, Runnable.class);
        methodBuilder.addCode("@Override\n");
        methodBuilder.beginControlFlow("public void run()");
        methodBuilder.addStatement(invokeRunnableStringBuilder.toString());
        methodBuilder.endControlFlow();
        methodBuilder.endControlFlow("");

        methodBuilder.beginControlFlow("if ((receiveThreadMode == Subscribe.MAIN) || (receiveThreadMode == Subscribe.POSTING && isPostMainThread))");
        methodBuilder.addStatement("$T.postMain(runnable)", TypeUtil.Util);
        methodBuilder.nextControlFlow("else");
        methodBuilder.addStatement("$T.postThread(runnable)", TypeUtil.Util);
        methodBuilder.endControlFlow();

        methodBuilder.endControlFlow();
        methodBuilder.endControlFlow();
        return methodBuilder.build();
    }
}

//        public class AccountEventProxy extends EventHandler.EventProxy<IMessageEvent> implements IMessageEvent {
//
//            @Override
//            public void onReceiverMessage(int messageId, String message) {
//                for (IMessageEvent iEvent : iEvents) {
//                    Subscribe subscribe = iEvent.getClass().getAnnotation(Subscribe.class);
//                    boolean isPostMainThread = isPostMainThread();
//                    int receiveThreadMode = subscribe == null ? Subscribe.POSTING : subscribe.receiveThreadMode();
//                    if (Util.isSyncInvoke(isPostMainThread, receiveThreadMode)) {
//                        iEvent.onReceiverMessage(messageId, message);
//                    } else {
//                        final IMessageEvent post_iEvent = iEvent;
//                        final int post_messageId = messageId;
//                        final String post_message = message;
//                        Runnable runnable = new Runnable() {
//                            @Override
//                            public void run() {
//                                post_iEvent.onReceiverMessage(post_messageId, post_message);
//                            }
//                        };
//                        if ((receiveThreadMode == Subscribe.MAIN) || (receiveThreadMode == Subscribe.POSTING && isPostMainThread)) {
//                            Util.postMain(runnable);
//                        } else {
//                            Util.postThread(runnable);
//                        }
//                    }
//                }
//            }
//        }
//    }
