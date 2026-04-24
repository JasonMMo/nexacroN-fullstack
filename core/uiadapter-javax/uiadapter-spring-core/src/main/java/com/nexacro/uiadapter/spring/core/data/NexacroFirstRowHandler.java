package com.nexacro.uiadapter.spring.core.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.DataSetList;
import com.nexacro.java.xapi.data.PlatformData;
import com.nexacro.java.xapi.data.Variable;
import com.nexacro.java.xapi.data.VariableList;
import com.nexacro.java.xapi.tx.HttpPartPlatformResponse;
import com.nexacro.java.xapi.tx.PlatformException;
import com.nexacro.java.xapi.tx.PlatformRequest;
import com.nexacro.java.xapi.tx.PlatformType;

/**
 * <pre>
 * Nexacro 플랫폼에서 대용량 데이터의 분할 전송 시, 첫 번째 행(row) 데이터를 우선적으로 송신하기 위한 처리 핸들러입니다.
 * </pre>
 * <p>
 * 이 클래스는 Split 전송 방식에 따라 <code>HttpPartPlatformResponse</code> 객체를 활용하여
 * 서버에서 클라이언트로의 데이터 스트림을 효율적으로 관리합니다.<br>
 * 데이터를 <b>Variable(변수)</b>를 먼저, <b>DataSet</b>을 그 다음 순서로 전송하며,
 * 큰 데이터셋도 분할하여 첫 번째 행이 전송되는 즉시 클라이언트에서 처리가 가능하도록 지원합니다.
 * </p>
 *
 * <ul>
 *   <li>대용량 데이터의 빠른 초기처리(First Row Processing)를 위해 설계되었습니다.</li>
 *   <li>Variable 후 DataSet 순서 전송, 각 전송 내역을 개별적으로 관리합니다.</li>
 *   <li>송신 이력(변수, 데이터셋 이름) 집계 기능도 제공합니다.</li>
 * </ul>
 *
 * @author Park SeongMin
 * @since 2015.08.05
 * @version 1.0
 * @see com.nexacro.java.xapi.tx.HttpPartPlatformResponse
 * @see com.nexacro.uiadapter.spring.core.context.NexacroContext#getFirstRowHandler()
 */
public class NexacroFirstRowHandler {

    /** 클라이언트 요청 정보를 담는 PlatformRequest */
    private final PlatformRequest platformRequest;
    /** HTTP 응답 객체 */
    private final HttpServletResponse httpResponse;
    /** 부분 전송용 HttpPartPlatformResponse */
    private HttpPartPlatformResponse partPlatformResponse = null;
    /** 텍스트 스트림 전송용 Writer */
    private PrintWriter writer = null;
    /** 데이터 전송에 사용할 MIME Content-Type */
    private String contentType;

    /** 마지막에 전송된 DataSet 객체 */
    private DataSet data = null;
    /** 첫 번째 row 송신 여부 플래그 */
    private boolean isFirstRowFired = false;
    /** DataSet이 이미 송신되었는가 */
    private boolean dataSetSended = false;
    /** partPlatformResponse 등 전송 초기화 여부 */
    private boolean isInit = false;
    /** 암호화 여부(기본 false) */
    @SuppressWarnings("unused")
    private final boolean isEncrypted = false;
    /** 송신 완료된 Variable 이름 집합 */
    private final Set sendOutVariableNameSet = new HashSet();
    /** 송신 완료된 DataSet 이름 집합 */
    private final Set sendOutDataSetNameSet = new HashSet();

    /**
     * HttpServletResponse를 이용하여 NexacroFirstRowHandler 객체를 생성합니다.<br>
     * 내부적으로 두 번째 생성자(PlatformRequest null)로 위임합니다.
     *
     * @param httpServletResponse 데이터 전송용 HTTP 응답 객체 (필수)
     * @throws IllegalArgumentException httpServletResponse가 null인 경우
     */
    public NexacroFirstRowHandler(HttpServletResponse httpServletResponse) {
        this(httpServletResponse, null);
    }

    /**
     * HttpServletResponse, PlatformRequest를 받아 NexacroFirstRowHandler 객체를 생성합니다.<br>
     * 데이터 전송 및 클라이언트 요청 정보 초기화에 사용됩니다.
     *
     * @param httpServletResponse 데이터 전송용 HTTP 응답 객체 (필수)
     * @param platformRequest 클라이언트 요청 정보 객체
     * @throws IllegalArgumentException httpServletResponse가 null인 경우
     */
    public NexacroFirstRowHandler(HttpServletResponse httpServletResponse, PlatformRequest platformRequest) {
        if(httpServletResponse == null) {
            throw new IllegalArgumentException("HttpServletResponse should not be null.");
        }
        this.httpResponse = httpServletResponse;
        this.platformRequest = platformRequest;
    }

    /**
     * 데이터 전송 시 사용할 Content-Type을 반환합니다.
     *
     * @return 현재 Content-Type 문자열
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 데이터 전송에 사용할 Content-Type을 지정합니다.<br>
     * 데이터 전송 전에 반드시 설정되어야 하며, 형식에 맞게 지정하면 클라이언트에서 정상 처리됩니다.
     *
     * @param contentType 전송에 사용할 Content-Type 값
     * @see PlatformType#CONTENT_TYPE_XML
     * @see PlatformType#CONTENT_TYPE_BINARY
     * @see PlatformType#CONTENT_TYPE_SSV
     * @see PlatformType#CONTENT_TYPE_JSON
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 지정한 PlatformData 객체의 모든 Variable과 DataSet을 전송합니다.<br>
     * Variable을 먼저, DataSet을 순차적으로 전송하며,
     * 첫 행(row)이 송신됨을 표시합니다.
     *
     * @param platformData 전송할 PlatformData(변수/데이터셋 포함)
     * @throws PlatformException 전송 중 오류가 발생하면 예외 발생
     */
    public void sendPlatformData(PlatformData platformData) throws PlatformException {
        isFirstRowFired = true;

        VariableList variableList = platformData.getVariableList();
        DataSetList dataSetList = platformData.getDataSetList();

        for (int variableListIndex = 0; variableListIndex < variableList.size(); variableListIndex++) {
            sendVariable(variableList.get(variableListIndex));
        }
        for (int datasetListIndex = 0; datasetListIndex < dataSetList.size(); datasetListIndex++) {
            sendDataSet(dataSetList.get(datasetListIndex));
        }
    }

    /**
     * Variable(변수) 1개를 전송합니다.<br>
     * 전송 전 partPlatformResponse 초기화 여부를 확인하고,
     * 이미 DataSet이 한 번이라도 전송되었다면 이후에는 Variable을 보낼 수 없습니다.
     * Variable의 이름을 송신 목록 집합에 추가합니다.
     *
     * @param variable 전송할 Variable
     * @throws PlatformException DataSet 전송 이후에는 Variable 전송 불가, 혹은 전송 중 오류
     */
    public void sendVariable(Variable variable) throws PlatformException {
        isFirstRowFired = true;
        intPartPlatformResponse();
        if (dataSetSended) {
            throw new PlatformException("DataSet aleady sended. can't send a variable after sending dataSet.");
        }
        partPlatformResponse.sendVariable(variable);
        sendOutVariableNameSet.add(variable.getName());
    }

    /**
     * DataSet 객체를 1개 전송합니다.<br>
     * 전송 전 partPlatformResponse 미리 초기화하며,
     * 전송이 시작되면 이후에는 Variable을 보낼 수 없습니다.<br>
     * 이미 이전에 전송되어 임시 저장된 DataSet이 이름이 다르다면 그것도 추가로 전송합니다.
     *
     * @param dataSet 전송할 DataSet
     * @throws PlatformException 전송 중 오류가 발생한 경우
     */
    public void sendDataSet(DataSet dataSet) throws PlatformException {
        if (dataSet == null) {
            return;
        }

        isFirstRowFired = true;
        dataSetSended = true;
        intPartPlatformResponse();

        // 기존 firstRow 로 이미 전송된 DataSet이 있다면 남은 데이터를 전송
        if (data != null && !data.getName().equals(dataSet.getName())) {
            partPlatformResponse.sendDataSet(data);
        }

        partPlatformResponse.sendDataSet(dataSet);

        data = dataSet;
        sendOutDataSetNameSet.add(dataSet.getName());
    }

    /**
     * 첫 번째 행(First Row) 송신이 시작되었는지 여부를 반환합니다.
     *
     * @return 데이터 전송이 시작되었다면 true, 아니면 false
     */
    public boolean isFirstRowStarted() {
        return isFirstRowFired;
    }

    /**
     * 내부에서 HttpPartPlatformResponse를 필요시 초기화합니다.<br>
     * 플랫폼 요청이 있으면 넘기고, Content-Type도 지정 가능.
     */
    private void intPartPlatformResponse() {
        if (!isInit) {
            if(platformRequest == null) {
                partPlatformResponse = new HttpPartPlatformResponse(httpResponse);
            } else {
                partPlatformResponse = new HttpPartPlatformResponse(httpResponse, platformRequest);
            }
            if(contentType != null) {
                partPlatformResponse.setContentType(contentType);
            }
            isInit = true;
        }
    }

    /**
     * 현재 First Row(첫 번째 행) 상태에서 오류여부를 확인합니다.
     * <br>특정 전송 방식(SSV, BIN)은 체크 동작이 정상적이지 않을 수 있습니다.
     *
     * @return 에러상태 여부
     */
    public boolean checkError() {
        setWriter();
        if (writer != null) {
            return writer.checkError();
        }
        return false;
    }

    /**
     * HTTP 응답의 Writer를 내부에 세팅합니다.
     */
    private void setWriter() {
        // writer 또는 outputstream을 선택하여 Writer 객체 획득
        if (this.httpResponse != null && this.writer != null) {
            try {
                this.writer = this.httpResponse.getWriter();
            } catch(UnsupportedEncodingException | IllegalStateException e) {
                // 예외 처리: 필요 시 확장
            } catch (IOException e) {
                // 예외 처리: 필요 시 확장
            }
        }
    }

    /**
     * 송신된 Variable(변수) 개수를 반환합니다.
     *
     * @return 송신된 변수 개수
     */
    int getSendOutVariableCount() {
        return sendOutVariableNameSet.size();
    }

    /**
     * 송신된 Variable(변수) 이름 목록을 반환합니다.
     *
     * @return 변수명 배열
     */
    String[] getSendOutVariableNames() {
        List sendOutVariableNameList = new ArrayList();
        sendOutVariableNameList.addAll(sendOutVariableNameSet);
        String[] sendOutVariableNames = new String[sendOutVariableNameList.size()];
        return (String[]) sendOutVariableNameList.toArray(sendOutVariableNames);
    }

    /**
     * 송신된 DataSet 개수를 반환합니다.
     *
     * @return 송신된 DataSet 개수
     */
    int getSendOutDataSetCount() {
        return sendOutDataSetNameSet.size();
    }

    /**
     * 송신된 DataSet 이름 목록을 반환합니다.
     *
     * @return 데이터셋명 배열
     */
    String[] getSendOutDataSetNames() {
        List sendOutDataSetNameList = new ArrayList();
        sendOutDataSetNameList.addAll(sendOutDataSetNameSet);

        String[] sendOutDataSetNames = new String[sendOutDataSetNameList.size()];
        return (String[]) sendOutDataSetNameList.toArray(sendOutDataSetNames);
    }

    /**
     * 내부에서 사용하는 HttpPartPlatformResponse 객체를 반환합니다.
     *
     * @return HttpPartPlatformResponse 객체
     */
    HttpPartPlatformResponse getHttpPartPlatformResponse() {
        return partPlatformResponse;
    }

    /**
     * 데이터 송신 완료 처리를 수행합니다.<br>
     * 내부 리소스 초기화 및 응답 종료 호출.
     *
     * @throws PlatformException 종료 과정에서 오류 발생 시
     */
    void end() throws PlatformException {
        if (isInit) {
            isInit = false;
            partPlatformResponse.end();
        }
    }

}