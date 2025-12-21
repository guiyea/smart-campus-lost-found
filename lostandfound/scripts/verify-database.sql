-- =============================================
-- 数据库验证脚本
-- 用于验证数据库表结构是否正确创建
-- =============================================

USE campuslostandfound;

-- 1. 检查所有表是否存在
SELECT 
    'Checking tables...' AS status;

SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    TABLE_COMMENT
FROM 
    INFORMATION_SCHEMA.TABLES
WHERE 
    TABLE_SCHEMA = 'campuslostandfound'
ORDER BY 
    TABLE_NAME;

-- 2. 检查外键约束
SELECT 
    'Checking foreign keys...' AS status;

SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_SCHEMA = 'campuslostandfound'
    AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY
    TABLE_NAME, CONSTRAINT_NAME;

-- 3. 检查索引
SELECT 
    'Checking indexes...' AS status;

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    NON_UNIQUE,
    INDEX_TYPE
FROM
    INFORMATION_SCHEMA.STATISTICS
WHERE
    TABLE_SCHEMA = 'campuslostandfound'
GROUP BY
    TABLE_NAME, INDEX_NAME, NON_UNIQUE, INDEX_TYPE
ORDER BY
    TABLE_NAME, INDEX_NAME;

-- 4. 检查表字段
SELECT 
    'Checking columns for user table...' AS status;

DESCRIBE user;

SELECT 
    'Checking columns for item table...' AS status;

DESCRIBE item;

-- 5. 验证字符集
SELECT 
    'Checking character sets...' AS status;

SELECT 
    TABLE_NAME,
    TABLE_COLLATION
FROM 
    INFORMATION_SCHEMA.TABLES
WHERE 
    TABLE_SCHEMA = 'campuslostandfound';

-- 6. 统计信息
SELECT 
    'Database statistics...' AS status;

SELECT 
    COUNT(*) AS total_tables
FROM 
    INFORMATION_SCHEMA.TABLES
WHERE 
    TABLE_SCHEMA = 'campuslostandfound';

SELECT 
    COUNT(*) AS total_foreign_keys
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_SCHEMA = 'campuslostandfound'
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 7. 验证完成
SELECT 
    '✓ Database verification completed!' AS status,
    'Expected 7 tables, check the results above.' AS note;
