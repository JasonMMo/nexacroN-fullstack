package com.nexacro.uiadapter.spring.core.context;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.context.ApplicationContext;

/**
 * Spring ApplicationContext에 대한 스레드-세이프한 접근을 제공하는 싱글톤 클래스입니다.
 * <p>
 * 이 클래스는 Spring으로 관리되지 않는 클래스(비 Spring Bean)에서 Spring의 Bean 객체에 접근할 수 있도록
 * ApplicationContext를 안전하게 제공하기 위해 사용합니다.
 * <p>
 * ApplicationContext 접근 시 스레드 안정성을 보장하기 위해 읽기-쓰기 락을 사용합니다.
 *
 * <p>사용 예시:
 * <pre>
 * ApplicationContext ctx = SpringAppContext.getInstance().getApplicationContext();
 * MyBean myBean = ctx.getBean(MyBean.class);
 * </pre>
 *
 * <p>참고: <a href="https://justamonad.com/bridge-pattern-using-applicationcontextaware/">Bridge Pattern Using ApplicationContextAware</a>
 *
 * @see org.springframework.context.ApplicationContext
 * @see com.nexacro.uiadapter.spring.core.context.ApplicationContextProvider
 */
public class SpringAppContext {

    /*
     * @Autowired 어노테이션을 통한 자동 주입이 어려운 환경에서,
     * 아래와 같이 ApplicationContext를 얻어 Bean 객체를 사용할 수 있습니다:
     * <pre>
     * ApplicationContext ctx = SpringAppContext.getInstance().getApplicationContext();
     * MyBean myBean = ctx.getBean(MyBean.class);
     * </pre>
     */

    /* 이 클래스의 싱글톤 인스턴스 */
    public final static SpringAppContext INSTANCE = new SpringAppContext();

    /* 스레드 안전성을 위한 읽기-쓰기 락(ReadWriteLock) */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /* ApplicationContext 접근을 위한 읽기 락 */
    private final Lock readLock = lock.readLock();

    /* ApplicationContext 갱신을 위한 쓰기 락 */
    private final Lock writeLock = lock.writeLock();

    /* Spring ApplicationContext 참조 */
    private ApplicationContext ctx;

    /**
     * 인스턴스 생성을 제한하기 위한 private 생성자.
     * <p>싱글톤 패턴을 강제합니다.
     */
    private SpringAppContext() {
    }

    /**
     * 싱글톤 인스턴스를 반환합니다.
     *
     * @return SpringAppContext 싱글톤 인스턴스
     */
    public static SpringAppContext getInstance() {
        return INSTANCE;
    }

    /**
     * ApplicationContext를 저장합니다.<br>
     * 본 메서드는 ApplicationContextProvider에 의해 Spring 구동 시 자동 호출됩니다.<br>
     * 쓰기 락을 사용하여 스레드 안전하게 ApplicationContext를 설정합니다.
     *
     * @param applicationContext 저장할 ApplicationContext 객체
     * @see com.nexacro.uiadapter.spring.core.context.ApplicationContextProvider
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        writeLock.lock();
        try {
            this.ctx = applicationContext;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Spring ApplicationContext를 반환합니다.<br>
     * ApplicationContext 접근이 필요한 모든 위치(특히 비 Spring Bean)에서 호출하여 Bean 객체를 얻을 수 있습니다.<br>
     * 읽기 락을 사용하여 스레드 안전하게 ApplicationContext를 반환합니다.
     *
     * @return 등록된 Spring ApplicationContext
     */
    public ApplicationContext getApplicationContext() {
        readLock.lock();
        try {
            return this.ctx;
        } finally {
            readLock.unlock();
        }
    }

}