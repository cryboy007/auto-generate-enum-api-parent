package pw.jonwinters.register;

import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import pw.jonwinters.config.AutoGenerateEnumConfig;
import pw.jonwinters.generate.EnumControllerGenerator;

import java.util.List;

public class EnumControllerRegister implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private Environment environment;

    private final EnumControllerGenerator enumControllerGenerator;


    public EnumControllerRegister(EnumControllerGenerator enumControllerGenerator) {
        this.enumControllerGenerator = enumControllerGenerator;
    }

    /**
     * We need manually set property cause the processor working for @ConfigurationProperties
     * has not worked in #postProcessBeanDefinitionRegistry stage, for now only support camelcase.
     * @return
     */
    private AutoGenerateEnumConfig prepareConfig() {
        return Binder.get(environment).bind("enums", Bindable.of(AutoGenerateEnumConfig.class)).orElseThrow(IllegalStateException::new);
    }

    /**
     * In this stage no beans has been instantiated yet, so we can't use autowired and other Spring-provided function
     * it means we need assemble object manually and read environment property manually
     * @param registry
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        enumControllerGenerator.setConfig(prepareConfig());
        for (Class clazz : enumControllerGenerator.generateControllerClazz()) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
            definition.setBeanClass(clazz);
            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            registry.registerBeanDefinition(clazz.getSimpleName(), definition);
        }
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //Ignore
        return;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.environment = applicationContext.getEnvironment();
    }
}
