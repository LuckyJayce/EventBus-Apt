package com.shizhefei.eventbus.plugin;

import com.google.auto.service.AutoService;
import com.shizhefei.eventbus.annotation.Event;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by LuckyJayce on 2017/3/21.
 */
@AutoService(Processor.class)
public class EventBusProcessor extends AbstractProcessor {

    private ProcessingEnvironment processingEnvironment;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Event.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
        messager = processingEnvironment.getMessager();
        TypeUtil.init(processingEnvironment);

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processEventInterfaces(roundEnv);
        return true;
    }

    private void processEventInterfaces(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(Event.class)) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                if (typeElement.getKind() == ElementKind.INTERFACE) {
                    List<? extends TypeMirror> interfaces = ((TypeElement) element).getInterfaces();
                    for (TypeMirror anInterface : interfaces) {
                        if (TypeUtil.ievent.equals(TypeName.get(anInterface))) {
                            write(false, typeElement);
                            break;
                        } else if (TypeUtil.iRemoteEvent.equals(TypeName.get(anInterface))) {
                            write(true, typeElement);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void write(boolean isRemoteEvent, TypeElement typeElement) {
        JavaFile javaFile = new EventProxyBuilder(messager).build(isRemoteEvent, typeElement);
        try {
            javaFile.writeTo(processingEnvironment.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param roundEnv
     */
    private void ppppp(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Event.class)) {
            print("element ", element);
            try {
                if (element instanceof TypeElement) {
                    Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith((TypeElement) element);
                    for (Element element1 : elements) {
                        print("element-> getElementsAnnotatedWith ", element1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Set<? extends Element> rootElements = roundEnv.getRootElements();
        for (Element rootElement : rootElements) {
            print("rootElement", rootElement);
        }
    }

    private void print(String s, Element element) {
        messager.printMessage(Diagnostic.Kind.NOTE, " s: -------------- " + s);
        messager.printMessage(Diagnostic.Kind.NOTE, " element: " + element);
        messager.printMessage(Diagnostic.Kind.NOTE, " element.getAnnotationMirrors():" + element.getAnnotationMirrors());
        messager.printMessage(Diagnostic.Kind.NOTE, " element.asType():" + element.asType());
        messager.printMessage(Diagnostic.Kind.NOTE, " element.getEnclosedElements():" + element.getEnclosedElements());
        messager.printMessage(Diagnostic.Kind.NOTE, " element.getEnclosingElement():" + element.getEnclosingElement());
        messager.printMessage(Diagnostic.Kind.NOTE, " element.getKind():" + element.getKind());
        messager.printMessage(Diagnostic.Kind.NOTE, " element.getModifiers():" + element.getModifiers());
        messager.printMessage(Diagnostic.Kind.NOTE, " element.getSimpleName():" + element.getSimpleName());
        TypeMirror typeMirror = null;
        try {
            typeMirror = processingEnvironment.getTypeUtils().capture(element.asType());
            messager.printMessage(Diagnostic.Kind.NOTE, " processingEnvironment.getTypeUtils().capture(element.asType()):" + typeMirror);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            typeMirror = processingEnvironment.getTypeUtils().erasure(element.asType());
            messager.printMessage(Diagnostic.Kind.NOTE, " processingEnvironment.getTypeUtils().erasure(element.asType()):" + typeMirror);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (AnnotationMirror annotationMirror : processingEnvironment.getElementUtils().getAllAnnotationMirrors(element)) {
            messager.printMessage(Diagnostic.Kind.NOTE, " annotationMirror:" + annotationMirror);
        }
        if (element instanceof TypeElement) {
            TypeElement typeElement = ((TypeElement) element);
            messager.printMessage(Diagnostic.Kind.NOTE, " typeElement.getInterfaces():" + typeElement.getInterfaces());
            messager.printMessage(Diagnostic.Kind.NOTE, " typeElement.getNestingKind():" + typeElement.getNestingKind());
            messager.printMessage(Diagnostic.Kind.NOTE, " typeElement.getQualifiedName():" + typeElement.getQualifiedName());
            messager.printMessage(Diagnostic.Kind.NOTE, " typeElement.getSuperclass():" + typeElement.getSuperclass());
            messager.printMessage(Diagnostic.Kind.NOTE, " typeElement.getTypeParameters():" + typeElement.getTypeParameters());
            Name name = processingEnvironment.getElementUtils().getBinaryName(typeElement);
            messager.printMessage(Diagnostic.Kind.NOTE, " name:" + name);
        }


    }


    /**
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
