package egovframework.com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Loads {@code globals.properties} into Spring's {@link org.springframework.core.env.Environment}.
 *
 * <p>The previous {@code propertiesService()} bean (constructing
 * {@code EgovPropertyServiceImpl}) was removed: nothing in this scaffold injects it,
 * and its {@code afterPropertiesSet()} tries to resolve the i18n key
 * {@code error.properties.initialize.reason} from {@code message-fdl} — a bundle
 * that is not on the classpath here — which aborts pool init with
 * {@code NoSuchMessageException}. Spring's own {@code @Value} / {@code Environment}
 * lookup covers every {@code Globals.*} consumer the runners need.
 */
@Configuration
@PropertySource("classpath:globals.properties")
public class EgovConfigAppProperties {
}
