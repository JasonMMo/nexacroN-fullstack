/**
 * 
 */
package com.nexacro.uiadapter.spring.core.util;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 0508080
 *
 */
public class EtcPropertiesBase {
	
	private static final Logger logger = LoggerFactory.getLogger(EtcPropertiesBase.class);

    List<Map<String, String>> etcList = new ArrayList<Map<String, String>> ();
	
	public List<Map<String, String>> getEtcList() {
		return etcList;
	}
	
	public void setEtcList(List<Map<String, String>> etcList) {
		this.etcList = etcList;
	}
	
	public boolean hasProperties() {
        if (etcList.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
	
	public void addEtcProperty(Map<String, String> propMap) {
		etcList.add(propMap);
	}
	
	public void addEtcProperty(String key, String value) {
		Map<String, String> propMap = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : propMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            logger.debug("[※] key: {}, value: {}", k, v);
        }

        propMap.put(key, value);
		etcList.add(propMap);
        logger.debug("[addEtcProperty]({},{})", key, value);
	}
	
	public Map<String, String> getEtcPropertyMap(String propertyKey) {
		Map<String, String> propMap ;
		
		propMap =  etcList.stream()
				.filter(m -> m.containsKey(propertyKey))
				.findFirst()
				.orElse(null);

		logger.debug(Objects.requireNonNull(propMap).toString());
		return propMap;
	}
	
	public String getEtcProperty(String propertyKey) {
		String defaultValue = "";
		Map<String, String> propertyValue = etcList.stream()
													.filter(m -> m.containsKey(propertyKey))
													.findFirst()
													.orElse(null);
		
		if(propertyValue!=null)
			return propertyValue.get(propertyKey);
		else
			return defaultValue;
	}
	
	public String toString() {
		StringBuffer resultBuf = new StringBuffer();
		resultBuf.append(etcList.stream().findAny().get().toString());
		logger.debug(resultBuf.toString());
		return resultBuf.toString();
	}
}
