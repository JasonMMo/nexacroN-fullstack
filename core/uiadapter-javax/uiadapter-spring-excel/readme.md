# uiadapter-spring-excel.jar #

version 정보

```
 - jdk : 1.8.0
 - xapi : 2.0.4
 - xeni : 1.5.21
```

# 1.5.21
2026.03.12

    [#excel:1.5.21: xeni : 1.5.21-SNAPSHOT 반영]

# 1.5.4.2
2025.10.15

    [#excel:1.5.4.2: 인증과정 타지 않도록 pom 수정]
    - 수정 내용 수정  : pom.xml
                    - MANIFEST.MF 에서 서명 관련 항목 제거
	- VERACODE 보안취약점(CWE ID 470) 우회를 위해 nexacro.xeni-compatible 모듈을 추가 사용해야 함.
	- TODO		: ## JAR 서명후 배포.

# 1.5.4.1
2025.08.18

    [#excel:1.5.4.1: xeni.properties 파일 대신 Factory 로딩으로 handler를 지정할 때 사용하는 인증파일 삭제]
    - 수정 폴더 : /META-INF/services/ 경로의 *.MF, *.SF 삭제 (추후 보완)
	- 			/META-INF/services/ 경로에  *.MF, *.SF 이 없을경우, nexacro.xeni-compatible 모듈 필수.
	- TODO		: ## JAR 서명후 배포.

# 1.5.4
2025.05.21

	- #excel:1.5.4: VERACODE 보안취약점(CWE ID 470) 패치
	- 이슈 : VERACODE 보안취약점 
	- 상세 취약점 :
		- Unsafe Reflection" (CWE ID 470) 외부 입력값을 안전하게 처리하지 않으면 심각한 보안 문제 가능.
	- 취약점 패치 : xeni.properties에 handler 지정방식 대신 Factory 로딩으로 handler 변경.
	- 대상 Class : XeniMultipartProcFactoryDef.java 신규 추가, META-INF/services 추가

# 1.3.12-SNAPSHOT
2023.08.16

    - POI 버전 5.2.2 적용
