package pw.jonwinters.utils;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import pw.jonwinters.common.annotations.EnumApi;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class CandidateEnumScanner implements ApplicationContextAware, ResourceLoaderAware {

    private ApplicationContext applicationContext;

    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * enum scanner
     * @param basePackage basePackage
     * @return specific enum which meet the requirements
     */
    public Set<Class<?>> scannerPackages(String basePackage) throws IOException {

        Set<Class<?>> set = new LinkedHashSet<>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + '/' + DEFAULT_RESOURCE_PATTERN;
        Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                String className = metadataReader.getClassMetadata().getClassName();
                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                    if (AnnotationUtils.isAnnotationDeclaredLocally(EnumApi.class, clazz)) {
                        set.add(clazz);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError ignored) {
                    //Ignore
                }
            }
        }
        return set;
    }


    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.getEnvironment().resolveRequiredPlaceholders(basePackage));
    }

    private Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }


    private ResourcePatternResolver resourcePatternResolver;

    private MetadataReaderFactory metadataReaderFactory;


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

}
