package com.nexacro.uiadapter.spring.core.data.convert;


/**
 * <p><code>PlatformData</code>({@code DataSet} 또는 {@code Variable})와 Java 객체 간 데이터 변환을 담당하는 인터페이스입니다.
 * <p>
 * Nexacro 플랫폼에서 사용하는 데이터 타입과 일반 Java 객체 간의 상호 변환 로직을 정의합니다.
 * 변환 과정에서 값의 검사, 가공, 후처리 등을 위한 리스너 등록 및 관리 기능도 제공합니다.
 *
 * @param <S> 변환의 입력(source) 타입
 * @param <T> 변환의 출력(target) 타입
 * @author Park SeongMin
 * @since 07.28.2015
 * @version 1.0
 */
public interface NexacroConverter<S, T> {

    /**
     * 지정된 소스 객체를 목적 타입으로 변환합니다.
     * <br>
     * 주어진 변환 정의 정보를 바탕으로 Nexacro 플랫폼 데이터에서 Java 객체 또는 반대로 변환이 이뤄집니다.
     *
     * @param source     변환할 입력 객체
     * @param definition 변환 정의 정보(파라미터 및 설정 등 포함)
     * @return 변환된 결과 객체
     * @throws NexacroConvertException 변환 처리 중 오류 발생 시
     */
    T convert(S source, ConvertDefinition definition) throws NexacroConvertException;

    /**
     * 해당 컨버터가 주어진 타입 간 변환을 지원하는지 여부를 반환합니다.
     * 
     * @param source 변환의 입력(source) 타입의 클래스
     * @param target 변환의 출력(target) 타입의 클래스
     * @return 변환 가능 여부(true: 변환 가능, false: 변환 불가)
     */
    boolean canConvert(Class source, Class target);

    /**
     * 변환 중 이벤트를 수신할 {@code NexacroConvertListener}를 등록합니다.
     * <br>
     * 등록된 리스너는 변환 로직 도중 값의 참조/수정 등 후처리 작업을 구현할 수 있습니다.
     *
     * @param listener 등록할 리스너 인스턴스
     * @see #removeListener(NexacroConvertListener)
     */
    void addListener(NexacroConvertListener listener);

    /**
     * 등록된 {@code NexacroConvertListener}를 해제합니다.
     * <br>
     * 등록되지 않은 리스너일 경우에는 아무 효과가 없습니다.
     *
     * @param listener 해제할 리스너 인스턴스
     * @see #addListener(NexacroConvertListener)
     */
    void removeListener(NexacroConvertListener listener);

    /**
     * 변환 쌍(입력 타입, 출력 타입)을 표현하는 내부 헬퍼 클래스입니다.
     * <br>
     * 소스-타겟 타입의 쌍을 저장/비교 용도로 활용하며, equals와 hashCode를 적절히 오버라이드합니다.
     */
    final class ConvertiblePair {

        /** 변환 입출력에서의 소스 타입 */
        private final Class<?> sourceType;
        /** 변환 입출력에서의 타겟 타입 */
        private final Class<?> targetType;

        /**
         * 소스와 타겟 클래스를 지정하여 ConvertiblePair를 생성합니다.
         * 
         * @param sourceType 변환 소스 타입 클래스
         * @param targetType 변환 타겟 타입 클래스
         */
        public ConvertiblePair(Class<?> sourceType, Class<?> targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        /**
         * 이 변환쌍의 소스 타입을 반환합니다.
         * 
         * @return 소스 타입 클래스
         */
        public Class<?> getSourceType() {
            return this.sourceType;
        }

        /**
         * 이 변환쌍의 타겟 타입을 반환합니다.
         * 
         * @return 타겟 타입 클래스
         */
        public Class<?> getTargetType() {
            return this.targetType;
        }

        /**
         * 동등성 비교를 수행합니다.
         * <br>
         * 소스 타입과 타겟 타입 모두 동일해야 같은 쌍으로 간주합니다.
         *
         * @param obj 비교 대상 객체
         * @return 동일한 쌍이면 true, 아니면 false
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || obj.getClass() != ConvertiblePair.class) {
                return false;
            }
            ConvertiblePair other = (ConvertiblePair) obj;
            return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);
        }

        /**
         * 해시코드 값을 반환합니다.
         * <br>
         * 소스 및 타겟 타입을 이용해 hash code를 생성합니다.
         *
         * @return 해시코드 값
         */
        @Override
        public int hashCode() {
            return this.sourceType.hashCode() * 31 + this.targetType.hashCode();
        }

        /**
         * "sourceType->targetType" 형식의 문자열로 변환쌍을 표현합니다.
         *
         * @return 변환쌍의 문자열 표현
         */
        @Override
        public String toString() {
            return this.sourceType.getName() + "->" + this.targetType.getName();
        }

    }

}