package pw.jonwinters.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import pw.jonwinters.config.AutoGenerateEnumConfig;
import pw.jonwinters.generate.EnumControllerGenerator;
import pw.jonwinters.register.EnumControllerRegister;
import pw.jonwinters.utils.CandidateEnumScanner;


@Configuration
@ComponentScan("pw.jonwinters.*")
@EnableConfigurationProperties(AutoGenerateEnumConfig.class)
@ConditionalOnProperty(prefix = "enum", name = "enable", havingValue = "true", matchIfMissing = true)
public class GenerateEnumApiAutoConfiguration {

    @Bean
    public static CandidateEnumScanner candidateEnumScanner() {
        return new CandidateEnumScanner();
    }

    @Bean
    public static EnumControllerGenerator enumControllerGenerator(CandidateEnumScanner candidateEnumScanner, AutoGenerateEnumConfig autoGenerateEnumConfig) {
        return new EnumControllerGenerator(candidateEnumScanner);
    }

    @Bean
    public static EnumControllerRegister controllerRegister(EnumControllerGenerator enumControllerGenerator) {
        return new EnumControllerRegister(enumControllerGenerator);
    }
}
