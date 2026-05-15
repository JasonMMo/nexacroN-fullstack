package egovframework.com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Loads {@code globals.properties} into Spring's {@link org.springframework.core.env.Environment}.
 *
 * <p>The eGov 4 {@code EgovPropertyServiceImpl} bean is intentionally not registered here:
 * nothing in this scaffold injects it, and its {@code afterPropertiesSet()} resolves the i18n
 * key {@code error.properties.initialize.reason} from {@code message-fdl} — a bundle that is
 * not on the classpath here — which aborts startup with {@code NoSuchMessageException}.
 * Spring's own {@code @Value} / {@code Environment} lookup covers every {@code Globals.*}
 * consumer the runner needs.
 *
 * <p>Picked up by {@code context-common.xml} via
 * {@code <context:component-scan base-package="egovframework"/>}.
 */
@Configuration
@PropertySource("classpath:globals.properties")
public class EgovConfigAppProperties {
}
