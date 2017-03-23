package com.shizhefei.eventbus.plugin;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

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

import static com.squareup.javapoet.ParameterizedTypeName.get;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by LuckyJayce on 2017/3/21.
 */

public class EventBusBuilder {
    private static final String packageName = "com.shizhefei.eventbus";
    private static final String className = "EventBus";
    private Messager messager;

    public EventBusBuilder(Messager messager) {
        this.messager = messager;
    }

    public JavaFile build(List<TypeElement> typeElements) {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        List<MethodSpec> methodSpecs = new ArrayList<>();

        fieldSpecs.add(defaultEventHandler());
        fieldSpecs.add(eventHandlerMap());

        methodSpecs.add(createRegister());
        methodSpecs.add(createUnregister());
        methodSpecs.add(createGetEventImp());
        methodSpecs.add(withActivity());

        TypeSpec.Builder eventBusBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodSpecs)
                .addFields(fieldSpecs);

        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();
        for (TypeElement typeElement : typeElements) {
            eventBusBuilder.addType(createEventImp(typeElement));
            PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
            ClassName eventImpClassName = ClassName.get(packageElement.getQualifiedName().toString(), typeElement.getSimpleName().toString());
//          生成  EventHandler.registers.put(IMessageEvent.class, new MessageEventImp()) 代码;
            staticBlockBuilder.add("$T.interfaceImpMap.put($T.class, new $L());\n", TypeUtil.eventHandler, eventImpClassName, createClassName(typeElement.getQualifiedName()));
        }
        eventBusBuilder.addStaticBlock(staticBlockBuilder.build());
        JavaFile javaFile = JavaFile.builder(packageName, eventBusBuilder.build()).indent("\t").build();
        return javaFile;
    }

    private FieldSpec defaultEventHandler() {
        FieldSpec.Builder builder = FieldSpec.builder(TypeUtil.eventHandler, "defaultEventHandler");
        builder.addModifiers(Modifier.STATIC, Modifier.FINAL, Modifier.PRIVATE);
        builder.initializer("new $T()", TypeUtil.eventHandler);
        return builder.build();
    }

    private FieldSpec eventHandlerMap() {
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(WeakHashMap.class), TypeUtil.activity, TypeUtil.ieventHandler);
        FieldSpec.Builder builder = FieldSpec.builder(typeName, "eventHandlerMap");
        builder.addModifiers(Modifier.STATIC, Modifier.FINAL, Modifier.PRIVATE);
        builder.initializer("new $T()", typeName);
        return builder.build();
    }

    //    public static synchronized IEventHandler withActivity(Activity activity) {
//        EventHandler eventBusImp = eventHandlerMap.get(activity);
//        if (eventBusImp == null) {
//            eventBusImp = new EventHandler();
//            eventHandlerMap.put(activity, eventBusImp);
//        }
//        return eventBusImp;
//    }
    private MethodSpec withActivity() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("withActivity");
        builder.addParameter(ParameterSpec.builder(TypeUtil.activity, "activity").addAnnotation(TypeUtil.NonNull).build());
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED);
        builder.returns(TypeUtil.ieventHandler);
        builder.addStatement("$T eventBusImp = eventHandlerMap.get(activity)", TypeUtil.ieventHandler);
        builder.beginControlFlow("if(eventBusImp == null)");
        builder.addStatement("eventBusImp = new $T()", TypeUtil.eventHandler);
        builder.addStatement("eventHandlerMap.put(activity, eventBusImp)");
        builder.endControlFlow();
        builder.addStatement("return eventBusImp");
        return builder.build();
    }

    /**
     * public static void register(IEvent iEvent) {
     * EventHandler.register(iEvent);
     * }
     */
    private MethodSpec createRegister() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("register");
        builder.addParameter(TypeUtil.ievent, "iEvent");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.addStatement("defaultEventHandler.register(iEvent)");
        return builder.build();
    }

    /**
     * public static void unregister(IEvent iEvent) {
     * EventHandler.unregister(iEvent);
     * }
     */
    private MethodSpec createUnregister() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("unregister");
        builder.addParameter(TypeUtil.ievent, "iEvent");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.addStatement("defaultEventHandler.unregister(iEvent)");
        return builder.build();
    }

    /*public static <IEVENT extends IEvent> IEVENT get(Class<IEVENT> eventClass) {
        return EventHandler.get(eventClass);
    }*/
    private MethodSpec createGetEventImp() {
        TypeVariableName typeVariableName = TypeVariableName.get("IEVENT").withBounds(TypeUtil.ievent);
        ParameterizedTypeName clasEvent = get(ClassName.get(Class.class), typeVariableName);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("get");
        builder.addTypeVariable(typeVariableName);
        builder.addParameter(clasEvent, "eventClass");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(typeVariableName);
        builder.addStatement("return defaultEventHandler.get(eventClass)");
        return builder.build();
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
    private TypeSpec createEventImp(TypeElement typeElement) {
        TypeName eventClass = TypeName.get(typeElement.asType());
        TypeSpec.Builder eventImp = TypeSpec.classBuilder(createClassName(typeElement.getQualifiedName())).addModifiers(FINAL, STATIC, PRIVATE);
        eventImp.addSuperinterface(eventClass);
        for (TypeParameterElement typeParameterElement : typeElement.getTypeParameters()) {
            eventImp.addTypeVariable(toTypeVariableName(typeParameterElement));
        }
        eventImp.superclass(ParameterizedTypeName.get(TypeUtil.EventProxy, eventClass));
        for (Element element : typeElement.getEnclosedElements()) {
            ExecutableElement executableElement = (ExecutableElement) element;
            MethodSpec methodSpec = createEventImpMethod(eventClass, executableElement);
            eventImp.addMethod(methodSpec);
        }
        return eventImp.build();
    }


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
        StringBuilder stringBuilder = new StringBuilder("iEvent.").append(executableElement.getSimpleName().toString()).append("(");
        for (VariableElement typeParameter : typeParameters) {
            String paramsName = typeParameter.getSimpleName().toString();
            methodBuilder.addParameter(TypeName.get(typeParameter.asType()), paramsName);
            stringBuilder.append(paramsName).append(", ");
        }
        if (stringBuilder.charAt(stringBuilder.length() - 2) == ',' && stringBuilder.charAt(stringBuilder.length() - 1) == ' ') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append(")");
        messager.printMessage(Diagnostic.Kind.NOTE, " createEventImpMethod stringBuilder:" + stringBuilder + "  executableElement:" + executableElement);
        methodBuilder.beginControlFlow("for($T iEvent : iEvents)", eventClass);
        methodBuilder.addStatement(stringBuilder.toString());
        methodBuilder.endControlFlow();
        return methodBuilder.build();
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
     * 实现IEvent接口的匿名内部类的名字
     *
     * @param name
     * @return
     */
    private String createClassName(Name name) {
        String s = name.toString();
        s = s.replaceAll("\\.", "_");
        return s + "Imp";
    }
}

//以下是生成的目标类
//public class EventBus {
//    static {
//        EventHandler.interfaceImpMap.put(IAccountEvent.class, new com_shizhefei_eventbus_events_IAccountEventImp());
//        EventHandler.interfaceImpMap.put(IMessageEvent.class, new com_shizhefei_eventbus_events_IMessageEventImp());
//    }
//
//    private static EventHandler defaultEventHandler = new EventHandler();
//    private static WeakHashMap<Activity, EventHandler> eventHandlerMap = new WeakHashMap<>();
//
//    /**
//     * 获取Activity内通信的EventHandler
//     *
//     * @param activity
//     * @return
//     */
//    public static synchronized IEventHandler withActivity(Activity activity) {
//        EventHandler eventBusImp = eventHandlerMap.get(activity);
//        if (eventBusImp == null) {
//            eventBusImp = new EventHandler();
//            eventHandlerMap.put(activity, eventBusImp);
//        }
//        return eventBusImp;
//    }
//
//    public static void register(IEvent iEvent) {
//        defaultEventHandler.register(iEvent);
//    }
//
//    public static void unregister(IEvent iEvent) {
//        defaultEventHandler.unregister(iEvent);
//    }
//
//    public static <IEVENT extends IEvent> IEVENT get(Class<IEVENT> eventClass) {
//        return defaultEventHandler.get(eventClass);
//    }
//
//    private static final class com_shizhefei_eventbus_events_IAccountEventImp extends EventHandler.EventProxy<IAccountEvent> implements IAccountEvent {
//        @Override
//        public void logout() {
//            for(IAccountEvent iEvent : iEvents) {
//                iEvent.logout();
//            }
//        }
//
//        @Override
//        public void login() {
//            for(IAccountEvent iEvent : iEvents) {
//                iEvent.login();
//            }
//        }
//    }
//
//    private static final class com_shizhefei_eventbus_events_IMessageEventImp extends EventHandler.EventProxy<IMessageEvent> implements IMessageEvent {
//        @Override
//        public void onReceiverMessage(int messageId, String message) {
//            for(IMessageEvent iEvent : iEvents) {
//                iEvent.onReceiverMessage(messageId, message);
//            }
//        }
//    }
//}
