-- ============================================
-- 1. file_group (PK)
-- ============================================
SET search_path TO product;
CREATE SCHEMA IF NOT EXISTS product;
CREATE TABLE file_group (
                            file_grp_srno BIGINT PRIMARY KEY,
                            file_group_name VARCHAR(255) NOT NULL,
                            created_by VARCHAR(255) NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            updated_by VARCHAR(255) NOT NULL,
                            updated_at TIMESTAMP NOT NULL,
                            deleted_yn CHAR(1) NOT NULL DEFAULT 'N',
                            deleted_at TIMESTAMP NULL
);

COMMENT ON COLUMN file_group.file_group_name IS 'PRODUCT,USER 등';


    -- ============================================
    -- 2. file_detail (복합 PK + FK → file_group)
    -- ============================================
CREATE TABLE file_detail (
                             file_dtl_srno BIGINT NOT NULL,
                             file_grp_srno BIGINT NOT NULL,
                             file_path VARCHAR(255) NOT NULL,
                             file_type VARCHAR(255) NOT NULL,
                             file_thum_path VARCHAR(255),
                             file_name VARCHAR(255) NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             created_by VARCHAR(255) NOT NULL,
                             updated_at TIMESTAMP NOT NULL,
                             updated_by VARCHAR(255) NOT NULL,

                             PRIMARY KEY (file_dtl_srno, file_grp_srno),

                             FOREIGN KEY (file_grp_srno)
                                 REFERENCES file_group(file_grp_srno)
);

COMMENT ON COLUMN file_detail.file_dtl_srno IS 'auto increment';
    COMMENT ON COLUMN file_detail.file_path IS 'url or key';


    -- ============================================
    -- 3. category (PK)
    -- ============================================
CREATE TABLE category (
                          id VARCHAR(20) PRIMARY KEY,
                          category_name VARCHAR(50),
                          deleted_yn CHAR(1) NOT NULL DEFAULT 'N',
                          created_at TIMESTAMP NOT NULL,
                          created_by VARCHAR(20) NOT NULL,
                          updated_at TIMESTAMP NOT NULL,
                          updated_by VARCHAR(20) NOT NULL,
                          deleted_at TIMESTAMP NULL
);


-- ============================================
-- 4. product (PK + FK → file_group)
-- ============================================
CREATE TABLE product (
                         id VARCHAR(20) PRIMARY KEY,
                         file_grp_id BIGINT NULL,
                         name VARCHAR(255) NOT NULL,
                         description TEXT NULL,
                         status VARCHAR(255) NOT NULL,
                         deleted_yn CHAR(1) NOT NULL DEFAULT 'N',
                         deleted_at TIMESTAMP NULL,
                         seller_id VARCHAR(20) NOT NULL,
                         created_at TIMESTAMP NOT NULL,  -- 원본대로 유지됨(오타 포함)
                         created_by VARCHAR(20) NOT NULL,
                         updated_at TIMESTAMP NOT NULL,
                         updated_by VARCHAR(20) NOT NULL,

                         FOREIGN KEY (file_grp_id)
                             REFERENCES file_group(file_grp_srno)
);


-- ============================================
-- 5. product_category_rel (복합 PK + 2개 FK)
-- ============================================
CREATE TABLE product_category_rel (
                                      category_id VARCHAR(20) NOT NULL,
                                      product_id VARCHAR(20) NOT NULL,
                                      created_at TIMESTAMP NULL,
                                      created_by VARCHAR(20) NULL,

                                      PRIMARY KEY (category_id, product_id),

                                      FOREIGN KEY (category_id)
                                          REFERENCES category(id),

                                      FOREIGN KEY (product_id)
                                          REFERENCES product(id)
);


-- auto-generated definition
create table "user"
(
    id           varchar(20)              not null
        primary key,
    email        varchar(255)             not null,
    password     varchar(500)             not null,
    nickname     varchar(50)              not null,
    phone_number varchar(20)              not null,
    zip_code     varchar(5)               not null,
    state        varchar(25)              not null,
    city         varchar(20)              not null,
    detail       varchar(255)             not null,
    status       varchar(25)              not null,
    deleted_yn   char default 'N'::bpchar not null,
    leave_time   timestamp,
    created_at   timestamp                not null,
    created_by   varchar(20),
    updated_at   timestamp                not null,
    updated_by   varchar(20),
    name         varchar(50)              not null
);

alter table "user"
    owner to postgres;


