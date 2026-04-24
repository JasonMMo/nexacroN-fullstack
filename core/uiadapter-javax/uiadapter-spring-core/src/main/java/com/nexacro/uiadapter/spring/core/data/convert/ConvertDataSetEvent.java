package com.nexacro.uiadapter.spring.core.data.convert;

import com.nexacro.java.xapi.data.DataSet;

/**
 * <p>DataSet의 데이터 변환 시 발생하는 이벤트 객체입니다.
 * <p>
 * 현재 변환이 이루어지는 DataSet의 컬럼 명칭 및 행의 위치 정보를 제공하며,  
 * 변환 대상 데이터가 원본데이터인지, 삭제데이터인지를 식별할 수 있습니다.
 * <br>
 * 주로 DataSet 내 데이터 변환 흐름에서 각 행 및 컬럼 단위 처리가 필요할 때 활용됩니다.
 * </p>
 * 
 * @author Park SeongMin
 * @since 08.09.2015
 * @version 1.0
 */
public class ConvertDataSetEvent extends ConvertEvent {

    /** 직렬화 버전 UID */
    private static final long serialVersionUID = -174881582977983877L;

    /** 변환이 이루어지는 행(row)의 인덱스 */
    private final int rowIndex;
    
    /** 변환이 이루어지는 컬럼(column)의 인덱스 */
    private final int columnIndex;
    
    /** 원본 데이터 처리 여부(true: 원본데이터, false: 신규 또는 수정 데이터) */
    private final boolean isSavedData;
    /** 삭제 데이터 처리 여부(true: 삭제 데이터, false: 일반 데이터) */
    private final boolean isRemovedData;
    
    /**
     * 기본 생성자. 삭제/원본 데이터 여부는 false로 설정됩니다.
     *
     * @param source      이벤트 소스(DataSet)
     * @param targetValue 변환 대상 값
     * @param rowIndex    변환이 이루어지는 행 인덱스
     * @param columnIndex 변환이 이루어지는 컬럼 인덱스
     */
    public ConvertDataSetEvent(DataSet source, Object targetValue, int rowIndex, int columnIndex) {
        this(source, targetValue, rowIndex, columnIndex, false, false);
    }
    
    /**
     * 모든 필드를 지정하는 생성자.
     *
     * @param source       이벤트 소스(DataSet)
     * @param targetValue  변환 대상 값
     * @param rowIndex     변환이 이루어지는 행 인덱스
     * @param columnIndex  변환이 이루어지는 컬럼 인덱스
     * @param isSavedData  원본 데이터 처리 여부
     * @param isRemovedData 삭제 데이터 처리 여부
     */
    public ConvertDataSetEvent(DataSet source, Object targetValue, int rowIndex, int columnIndex, boolean isSavedData, boolean isRemovedData) {
        super(source, targetValue);
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.isSavedData = isSavedData;
        this.isRemovedData = isRemovedData;
    }
    
    /**
     * 변환이 이루어지는 행의 인덱스를 반환합니다.
     * 
     * @return 행 인덱스 값
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * 변환이 이루어지는 컬럼의 인덱스를 반환합니다.
     * 
     * @return 컬럼 인덱스 값
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * 변환 대상 컬럼의 명칭을 반환합니다.
     * 
     * @return 컬럼명(String)
     */
    public String getColumnName() {
        DataSet ds = (DataSet) getSource();
        return ds.getColumn(columnIndex).getName();
    }
    
    /**
     * 현재 변환 중인 데이터가 DataSet의 원본데이터(저장 데이터)인지 여부를 반환합니다.
     * 
     * @return true일 경우 원본데이터, false일 경우 신규/수정 데이터
     */
    public boolean isSavedData() {
        return isSavedData;
    }

    /**
     * 현재 변환 중인 데이터가 삭제 데이터인지를 반환합니다.
     * 
     * @return true일 경우 삭제 데이터, false일 경우 일반 데이터
     */
    public boolean isRemovedData() {
        return isRemovedData;
    }

}