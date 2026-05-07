-- ============================================================
--  nexacroN-fullstack seed schema (HSQL in-memory)
--  Statement separator: ^^ (set in application.yml)
--  Dialect: HyperSQL 2.x (CREATE CACHED TABLE not needed — mem db)
--  Target runners: all 8 (javax + jakarta + webflux)
--
--  Tables aligned with GitLab canonical sample
--  (gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta):
--    1. TB_USER            — login + user demo
--    2. TB_BOARD           — board CRUD demo (canonical column set)
--    3. TB_DEPT            — flat dept list (canonical lowercase columns)
--    4. TB_LARGE           — paging + streaming demo
--    5. TB_TEST_DATA_TYPE  — testdata column-type demo
--    6. WIDE_COLUMNS       — wide-column load (50 columns) [non-canonical, retained]
--    7. FILE_META          — file upload metadata        [non-canonical, retained]
-- ============================================================

DROP TABLE IF EXISTS FILE_META^^
DROP TABLE IF EXISTS WIDE_COLUMNS^^
DROP TABLE IF EXISTS TB_TEST_DATA_TYPE^^
DROP TABLE IF EXISTS TB_LARGE^^
DROP TABLE IF EXISTS TB_DEPT^^
DROP TABLE IF EXISTS TB_BOARD^^
DROP TABLE IF EXISTS TB_USER^^

-- ------------------------------------------------------------
-- 1. TB_BOARD (canonical)
-- ------------------------------------------------------------
CREATE TABLE TB_BOARD (
      POST_ID       INTEGER       NOT NULL IDENTITY
    , TITLE         VARCHAR(150)  NULL
    , CONTENTS      LONGVARCHAR   NULL
    , REG_ID        VARCHAR(20)   NULL
    , REG_DATE      DATE          NULL
    , COMMUNITY_ID  VARCHAR(16)   NULL
    , HIDDEN_INFO   VARCHAR(32)
    , HIT_COUNT     INTEGER       NULL
    , IS_NOTICE     BOOLEAN
    , PRIMARY KEY (POST_ID)
)^^

-- ------------------------------------------------------------
-- 2. TB_USER (canonical)
-- ------------------------------------------------------------
CREATE TABLE TB_USER (
      USER_ID        VARCHAR(50)  NOT NULL
    , USER_NAME      VARCHAR(50)  NOT NULL
    , EN_NAME        VARCHAR(20)  NULL
    , COMP_PHONE     VARCHAR(20)  NULL
    , PHONE          VARCHAR(20)  NULL
    , CELL_PHONE     VARCHAR(20)  NULL
    , COMPANY        VARCHAR(50)  NULL
    , JOB_POSITION   VARCHAR(20)  NULL
    , ASSIGNMENT     VARCHAR(20)  NULL
    , OFFICER_YN     VARCHAR(20)  NULL
    , FAX            VARCHAR(20)  NULL
    , ZIP_CODE       VARCHAR(20)  NULL
    , ADDRESS        VARCHAR(20)  NULL
    , COMP_ZIP_CODE  VARCHAR(20)  NULL
    , COMP_ADDRESS   VARCHAR(20)  NULL
    , EMAIL          VARCHAR(20)  NULL
    , DEPT_ID        VARCHAR(20)  NULL
    , PASSWORD       VARCHAR(20)  NOT NULL
    , PRIMARY KEY (USER_ID)
)^^

-- ------------------------------------------------------------
-- 3. TB_DEPT (canonical — flat, lowercase columns)
-- ------------------------------------------------------------
CREATE TABLE TB_DEPT (
      deptId       INTEGER      NOT NULL IDENTITY
    , deptName     VARCHAR(36)  NULL
    , memberCount  INTEGER      NULL
    , PRIMARY KEY (deptId)
)^^

-- ------------------------------------------------------------
-- 4. TB_LARGE (canonical — paging + streaming target)
-- ------------------------------------------------------------
CREATE TABLE TB_LARGE (
      LARGE_ID  INTEGER       NOT NULL IDENTITY
    , NAME      VARCHAR(20)   NOT NULL
    , REG_DATE  DATE          DEFAULT CURRENT_DATE
    , STORY     VARCHAR(512)  DEFAULT '현재는 유망한 기술 스타트업에서 최고전략책임자(CSO)로 재직하며, 회사의 코스닥 상장(IPO) 준비와 글로벌 시장 진출을 총괄하고 있습니다.'
    , STATUS    INTEGER       DEFAULT 1
    , PRIMARY KEY (LARGE_ID)
)^^

-- ------------------------------------------------------------
-- 5. TB_TEST_DATA_TYPE — testdata column-type demo
-- ------------------------------------------------------------
CREATE TABLE TB_TEST_DATA_TYPE (
    ID                INTEGER       NOT NULL IDENTITY,
    STRING_VALUE      VARCHAR(200)  NULL,
    INT_VALUE         INTEGER       NULL,
    BOOLEAN_VALUE     BOOLEAN       NULL,
    LONG_VALUE        BIGINT        NULL,
    FLOAT_VALUE       FLOAT         NULL,
    DOUBLE_VALUE      DOUBLE        NULL,
    BIG_DECIMAL_VALUE DECIMAL(19,4) NULL,
    DATE_VALUE        DATE          NULL,
    TIME_VALUE        TIMESTAMP     NULL,
    DATETIME_VALUE    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NULL,
    BYTES_VALUE       BLOB          NULL,
    PRIMARY KEY (ID)
)^^

-- ------------------------------------------------------------
-- 6. WIDE_COLUMNS (non-canonical, retained — 50-column wide demo)
-- ------------------------------------------------------------
CREATE TABLE WIDE_COLUMNS (
    KEY_ID         VARCHAR(20)  NOT NULL PRIMARY KEY,
    COL_01 VARCHAR(50), COL_02 VARCHAR(50), COL_03 VARCHAR(50), COL_04 VARCHAR(50), COL_05 VARCHAR(50),
    COL_06 VARCHAR(50), COL_07 VARCHAR(50), COL_08 VARCHAR(50), COL_09 VARCHAR(50), COL_10 VARCHAR(50),
    COL_11 INTEGER,     COL_12 INTEGER,     COL_13 INTEGER,     COL_14 INTEGER,     COL_15 INTEGER,
    COL_16 INTEGER,     COL_17 INTEGER,     COL_18 INTEGER,     COL_19 INTEGER,     COL_20 INTEGER,
    COL_21 DECIMAL(18,4), COL_22 DECIMAL(18,4), COL_23 DECIMAL(18,4), COL_24 DECIMAL(18,4), COL_25 DECIMAL(18,4),
    COL_26 DECIMAL(18,4), COL_27 DECIMAL(18,4), COL_28 DECIMAL(18,4), COL_29 DECIMAL(18,4), COL_30 DECIMAL(18,4),
    COL_31 DATE,         COL_32 DATE,         COL_33 DATE,         COL_34 DATE,         COL_35 DATE,
    COL_36 TIMESTAMP,    COL_37 TIMESTAMP,    COL_38 TIMESTAMP,    COL_39 TIMESTAMP,    COL_40 TIMESTAMP,
    COL_41 BOOLEAN,      COL_42 BOOLEAN,      COL_43 BOOLEAN,      COL_44 BOOLEAN,      COL_45 BOOLEAN,
    COL_46 CLOB,         COL_47 CLOB,         COL_48 CLOB,         COL_49 CLOB,         COL_50 CLOB
)^^

-- ------------------------------------------------------------
-- 7. FILE_META (non-canonical, retained — file upload metadata)
-- ------------------------------------------------------------
CREATE TABLE FILE_META (
    FILE_ID        VARCHAR(40)  NOT NULL PRIMARY KEY,
    ORIGINAL_NAME  VARCHAR(255) NOT NULL,
    STORED_PATH    VARCHAR(500) NOT NULL,
    CONTENT_TYPE   VARCHAR(100),
    SIZE_BYTES     BIGINT       NOT NULL,
    UPLOADED_BY    VARCHAR(50),
    UPLOADED_AT    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    DELETED        BOOLEAN      DEFAULT FALSE NOT NULL,
    CONSTRAINT FK_FILE_UPLOADER FOREIGN KEY (UPLOADED_BY) REFERENCES TB_USER(USER_ID)
)^^

CREATE INDEX IDX_FILE_UPLOADER ON FILE_META(UPLOADED_BY)^^
CREATE INDEX IDX_FILE_UPLOADED_AT ON FILE_META(UPLOADED_AT)^^
