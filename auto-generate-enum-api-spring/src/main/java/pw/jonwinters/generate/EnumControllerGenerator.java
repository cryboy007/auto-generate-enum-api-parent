package pw.jonwinters.generate;

import com.google.common.collect.Lists;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import pw.jonwinters.common.annotations.EnumApi;
import pw.jonwinters.common.annotations.EnumCode;
import pw.jonwinters.common.annotations.EnumName;
import pw.jonwinters.common.model.Document;
import pw.jonwinters.config.AutoGenerateEnumConfig;
import pw.jonwinters.exception.AnnotatedFieldAccessException;
import pw.jonwinters.exception.IllegalEnumException;
import pw.jonwinters.utils.CandidateEnumScanner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

@Slf4j
public class EnumControllerGenerator {

    private final CandidateEnumScanner candidateEnumScanner;

    private AutoGenerateEnumConfig config;

    public EnumControllerGenerator(CandidateEnumScanner candidateEnumScanner) {
        this.candidateEnumScanner = candidateEnumScanner;
    }

    /**
     * Before you use this clazz instance, you need manually set config class
     * @param config
     */
    public void setConfig(AutoGenerateEnumConfig config) {
        this.config = config;
    }

    /**
     * generate @RestController class
     * @return
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IOException
     */
    public List<Class<?>> generateControllerClazz() throws NotFoundException, CannotCompileException, IOException {
        if (config == null) {
            throw new IllegalStateException("Make sure you have prepared the properly config");
        }
        List<Class<?>> generateControllerClazz = Lists.newArrayList();

        Set<Class<?>> enumClazz = candidateEnumScanner.scannerPackages(StringUtils.isEmpty(config.getBaseScanPackage()) ? "" : config.getBaseScanPackage());

        for (Class clazz : enumClazz) {
            ClassPool classPool = ClassPool.getDefault();
            EnumApi enumApi = (EnumApi) clazz.getAnnotation(EnumApi.class);

            classPool.importPackage("pw.jonwinters.common.model");
            classPool.importPackage("java.util");
            classPool.importPackage("java.lang");
            CtClass generateController = classPool.makeClass("pw.jonwinters.generate.EnumRestController_" + clazz.getSimpleName());
            ClassFile generateControllerClassFile = generateController.getClassFile();
            ConstPool constPool = generateControllerClassFile.getConstPool();

            //Add annotation RestController and RequestMapping for generated-class
            AnnotationsAttribute annotationsAttribute = generateRestController(classPool, constPool, config.getBasePath());
            generateControllerClassFile.addAttribute(annotationsAttribute);

            //Generate method annotation
            CtMethod ctMethod = generateMethod(enumApi, classPool, generateController, constPool, clazz);
            generateController.addMethod(ctMethod);

            if (config.isDebug()) {
                generateController.debugWriteFile(config.getDebugPath());
            }
            generateControllerClazz.add(generateController.toClass());
        }

        return generateControllerClazz;
    }

    private CtMethod generateMethod(EnumApi enumApi, ClassPool classPool, CtClass controllerCtClass,
                                    ConstPool constPool, Class enumClazz) throws NotFoundException, CannotCompileException {
        AnnotationsAttribute getMappingAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation getMappingAnnotation = new Annotation("org.springframework.web.bind.annotation.GetMapping", constPool);

        //Set EnumApi value() as path
        String value = enumApi.value();
        if (StringUtils.isEmpty(value)) {
            //If default url was set to null , then use enum class simple name as path
            value = enumClazz.getSimpleName();
        }
        MemberValue[] memberValues = new MemberValue[]{new StringMemberValue(value, constPool)};
        ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constPool);
        arrayMemberValue.setValue(memberValues);
        getMappingAnnotation.addMemberValue("value", arrayMemberValue);
        getMappingAttribute.setAnnotation(getMappingAnnotation);


        CtClass enumResponseCtClass = classPool.getCtClass("java.util.List");
        CtMethod ctMethod = new CtMethod(enumResponseCtClass, "get" + enumClazz.getSimpleName(), null, controllerCtClass);

        //Reflect enum info
        Object[] enumConstants = enumClazz.getEnumConstants();

        StringBuilder methodBodyCodeBuild = new StringBuilder();

        //Generate Controller method body
        for (int i = 0; i < enumConstants.length; i++) {
            Object clazz = enumConstants[i];
            String name = getName(clazz);
            Object code = getCode(clazz);
            if (code instanceof Integer) {
                //Integer type
                methodBodyCodeBuild.append(String.format("new pw.jonwinters.common.model.EnumResponse(\"%s\",new Integer(%s))", name, code));
            } else if (code instanceof String) {
                //String type
                methodBodyCodeBuild.append(String.format("new pw.jonwinters.common.model.EnumResponse(\"%s\",\"%s\")", name, code));
            } else {
                //Other type use its' toString() method
                methodBodyCodeBuild.append(String.format("new pw.jonwinters.common.model.EnumResponse(\"%s\",\"%s\")", name, code.toString()));
            }
            if (i < enumConstants.length - 1) {
                methodBodyCodeBuild.append(",");
            }
        }
        String methodBody = String.format("return Arrays.asList(new pw.jonwinters.common.model.EnumResponse[]{%s});", methodBodyCodeBuild.toString());
        if (config.isDebug()) {
            log.debug("method code: {}", methodBody);
        }
        ctMethod.setBody(methodBody);
        ctMethod.getMethodInfo().addAttribute(getMappingAttribute);

        return ctMethod;
    }

    private String getName(Object enumObj) {
        if (enumObj instanceof Document) {
            //Through interface get the name
            return ((Document) enumObj).getName();
        }
        return getValueThrough(enumObj, "name", EnumName.class).toString();
    }

    private Object getValueThrough(Object enumObj, String fieldName, Class annotationClazz) {


        Class<?> clazz = enumObj.getClass();

        //Through annotation get the name
        for (Field declaredField : clazz.getDeclaredFields()) {
            java.lang.annotation.Annotation annotation = AnnotationUtils.getAnnotation(declaredField, annotationClazz);
            if (annotation != null) {
                declaredField.setAccessible(true);
                try {
                    return declaredField.get(enumObj);
                } catch (IllegalAccessException e) {
                    throw new AnnotatedFieldAccessException(e);
                }
            }
        }

        //Through default field value get the name
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.getName().equals(fieldName)) {
                declaredField.setAccessible(true);
                try {
                    return declaredField.get(enumObj);
                } catch (IllegalAccessException e) {
                    throw new AnnotatedFieldAccessException(e);
                }
            }
        }

        //Can't find any method to get the name throw exception
        throw new IllegalEnumException();
    }

    private Object getCode(Object enumObj) {
        if (enumObj instanceof Document) {
            //Through interface get the name
            return ((Document) enumObj).getCode();
        }
        return getValueThrough(enumObj, "code", EnumCode.class);
    }


    /**
     * @return @RestController Attribute
     */
    private AnnotationsAttribute generateRestController(ClassPool classPool, ConstPool constPool, String basePath) throws NotFoundException {
        String resourcePath = RestController.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        classPool.insertClassPath(resourcePath);

        AnnotationsAttribute annotationAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation restControllerAnnotation = new Annotation("org.springframework.web.bind.annotation.RestController", constPool);
        Annotation requestMappingAnnotation = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constPool);

        MemberValue[] memberValues = new MemberValue[]{new StringMemberValue(basePath, constPool)};
        ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constPool);
        arrayMemberValue.setValue(memberValues);
        requestMappingAnnotation.addMemberValue("value", arrayMemberValue);

        annotationAttribute.addAnnotation(restControllerAnnotation);
        annotationAttribute.addAnnotation(requestMappingAnnotation);
        return annotationAttribute;
    }
}
