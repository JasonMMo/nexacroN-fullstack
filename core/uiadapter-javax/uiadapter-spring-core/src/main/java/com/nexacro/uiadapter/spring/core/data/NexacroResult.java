package com.nexacro.uiadapter.spring.core.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.PlatformData;
import com.nexacro.java.xapi.data.Variable;

/**
 * <pre>
 * Nexacro 플랫폼에 DataSet 또는 Variable 형식의 데이터를 전송하는데 사용되는 정보를 담는 클래스입니다.
 * </pre>
 * <p>
 * 데이터의 삽입 시점에서는 변환 작업이 이루어지지 않으며,
 * 실제 변환은 NexacroHandlerMethodReturnValueHandler 내부에서 처리됩니다.<br>
 * 이 클래스는 Nexacro 플랫폼으로 전달할 데이터들을 수집하는 역할을 하며,
 * 통신 결과의 상태코드 및 메시지, 전송할 DataSet 및 Variable 관련 데이터를 관리합니다.
 * </p>
 *
 * @author Park SeongMin
 * @since 2015.07.27
 * @version 1.0
 * @see com.nexacro.uiadapter.spring.core.resolve.NexacroHandlerMethodReturnValueHandler
 */
public class NexacroResult {

    /** 실제 전송될 플랫폼 데이터 객체 */
    private PlatformData platformData;

    /** DataSet 이름-데이터(리스트 또는 VO 또는 Map) 매핑 */
    private Map<String, Object> dataSetMaps;
    /** Variable 이름-데이터(Object) 매핑 */
    private Map<String, Object> variableMaps;

    /** 처리 결과 코드(0: 정상, 음수: 오류 등) */
    private int errorCode;
    /** 오류 발생 시의 메시지 */
    private String errorMsg;

    /** 오류 등록 여부 */
    private boolean registedError = false;

    /**
     * NexacroResult 인스턴스를 생성합니다.<br>
     * 내부 맵과 플랫폼 데이터 초기화 작업을 수행합니다.
     */
    public NexacroResult() {
        initResult();
    }

    /**
     * 내부 자료구조 및 PlatformData 객체를 초기화합니다.
     */
    private void initResult() {
        dataSetMaps = new HashMap<String, Object>();
        variableMaps = new HashMap<String, Object>();
        platformData = new PlatformData();
    }

    /**
     * 미리 생성된 DataSet 객체를 결과에 추가합니다.
     * <p>즉시 PlatformData 객체에 반영됩니다.
     *
     * @param dataSet 추가할 DataSet
     */
    public void addDataSet(DataSet dataSet) {
        platformData.addDataSet(dataSet);
    }

    /**
     * List 자료형(VO 객체 혹은 Map 객체 리스트)을 DataSet으로 결과에 추가합니다.<br>
     * <b>주의:</b> 실질적인 DataSet 변환은 후처리기에서 수행됩니다.
     *
     * @param dataSetName DataSet 이름(필수)
     * @param beans DataSet에 담길 데이터(List)
     * @throws IllegalArgumentException dataSetName이 null인 경우
     */
    public void addDataSet(String dataSetName, List<?> beans) {
        checkName(dataSetName);
        checkBean(beans);

        dataSetMaps.put(dataSetName, beans);
    }

    /**
     * 단일 개체(VO 또는 Map)를 DataSet으로 결과에 추가합니다.<br>
     * <b>주의:</b> 실질적인 DataSet 변환은 후처리기에서 수행됩니다.
     *
     * @param dataSetName DataSet 이름(필수)
     * @param beans DataSet에 담길 데이터(단일 객체)
     * @throws IllegalArgumentException dataSetName이 null인 경우
     */
    public void addDataSet(String dataSetName, Object beans) {
        checkName(dataSetName);
        checkBean(beans);

        dataSetMaps.put(dataSetName, beans);
    }

    /**
     * 미리 생성된 Variable 객체를 결과에 추가합니다.<br>
     * 즉시 PlatformData에 반영됩니다.
     *
     * @param variable 추가할 Variable
     */
    public void addVariable(Variable variable) {
        platformData.addVariable(variable);
    }

    /**
     * Object를 Variable로 추가합니다.<br>
     * <b>주의:</b> 실질적인 Variable 변환은 후처리기에서 수행됩니다.
     *
     * @param variableName Variable 이름(필수)
     * @param object Variable에 담길 값
     * @throws IllegalArgumentException variableName이 null인 경우
     */
    public void addVariable(String variableName, Object object) {
        checkName(variableName);

        variableMaps.put(variableName, object);
    }

    /**
     * 결과에 추가된 DataSet 전체 목록을 읽기 전용 맵으로 반환합니다.
     * 
     * @return DataSet명-데이터 매핑(읽기 전용)
     */
    public Map<String, Object> getDataSets() {
        return Collections.unmodifiableMap(dataSetMaps);
    }

    /**
     * 결과에 추가된 Variable 전체 목록을 읽기 전용 맵으로 반환합니다.
     * 
     * @return Variable명-데이터 매핑(읽기 전용)
     */
    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variableMaps);
    }

    /**
     * 이름 값이 null인지 검사합니다.
     * 
     * @param dataName 검사할 이름
     * @throws IllegalArgumentException null이면 예외 발생
     */
    private void checkName(String dataName) {
        if(dataName == null) {
            throw new IllegalArgumentException("No name specified");
        }
    }

    /**
     * List Bean의 유효성을 검사합니다.<br>
     * (현재 내용 없음, 향후 확장 가능)
     * 
     * @param bean 검사 대상(리스트)
     */
    private void checkBean(List bean) {
    }

    /**
     * Object Bean의 유효성을 검사합니다.<br>
     * (현재 내용 없음, 향후 확장 가능)
     * 
     * @param bean 검사 대상(객체)
     */
    private void checkBean(Object bean) {
    }

    /**
     * PlatformData 객체를 반환합니다.
     * 
     * @return 플랫폼 데이터 객체
     */
    public PlatformData getPlatformData() {
        return platformData;
    }


    /**
     * PlatformData 객체를 설정합니다.
     * <p>
     * 외부에서 PlatformData 객체를 직접 지정할 경우 사용합니다.<br>
     * 일반적으로는 내부적으로 초기화되어 사용되지만, 필요시 외부에서 완전히 생성된 PlatformData 객체를 주입할 수도 있습니다.
     * </p>
     *
     * @param platformData 설정할 PlatformData 객체
     */
    public void setPlatformData(PlatformData platformData) {
        this.platformData = platformData;
    }

    /**
     * 오류 코드를 반환합니다.
     * <p>
     * 이 오류 코드는 Nexacro 플랫폼과 통신 시 결과코드로 전달됩니다.
     * </p>
     *
     * @return 오류 코드 값
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 오류 코드를 설정하고, 에러 등록 플래그를 true로 변경합니다.
     * <p>
     * 이 오류 코드는 Nexacro 플랫폼과 통신 시 결과코드로 전달됩니다.
     * </p>
     *
     * @param errorCode 설정할 오류 코드 값
     */
    public void setErrorCode(int errorCode) {
        this.registedError = true;
        this.errorCode = errorCode;
    }

    /**
     * 오류 코드가 한 번이라도 등록되었는지 여부를 반환합니다.
     *
     * @return 오류 코드가 등록된 경우 true, 아니면 false
     */
    public boolean registedErrorCode() {
        return this.registedError;
    }

    /**
     * 오류 메시지를 반환합니다.
     * <p>
     * 이 오류 메시지는 Nexacro 플랫폼으로 전송됩니다.
     * </p>
     *
     * @return 오류 메시지
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * 오류 메시지를 설정합니다.
     * <p>
     * 이 오류 메시지는 Nexacro 플랫폼으로 전송됩니다.
     * </p>
     *
     * @param errorMsg 설정할 오류 메시지
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}