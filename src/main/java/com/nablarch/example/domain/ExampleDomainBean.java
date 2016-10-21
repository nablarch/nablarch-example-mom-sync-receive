package com.nablarch.example.domain;

import nablarch.core.validation.ee.Digits;
import nablarch.core.validation.ee.Length;
import nablarch.core.validation.ee.SystemChar;

import com.nablarch.example.validator.YYYYMMDD;

/**
 * ドメイン定義。
 */
public class ExampleDomainBean {

    /** プロジェクト名 */
    @Length(max = 128)
    @SystemChar(charsetDef = "全角文字")
    private String projectName;

    /** プロジェクト種別 */
    @Length(max = 128)
    @SystemChar(charsetDef = "半角英数")
    private String projectType;

    /** プロジェクト分類 */
    @Length(max = 128)
    @SystemChar(charsetDef = "半角英数")
    private String projectClass;

    /** 日付 */
    @YYYYMMDD
    private String date;

    /** 顧客ID */
    @Digits(integer = 9, fraction = 0)
    private String clientId;

    /** プロジェクトマネージャ名 */
    @Length(max = 128)
    @SystemChar(charsetDef = "全角文字")
    private String projectManager;

    /** プロジェクトリーダー名 */
    @Length(max = 128)
    @SystemChar(charsetDef = "全角文字")
    private String projectLeader;

    /** ユーザID */
    @Digits(integer = 9, fraction = 0)
    private String userId;

    /** 備考 */
    @Length(max = 128)
    @SystemChar(charsetDef = "システム許容文字")
    private String note;

    /** 売上高 */
    @Digits(integer = 9, fraction = 0)
    private String sales;

    /** 売上原価 */
    @Digits(integer = 9, fraction = 0)
    private String costOfGoodsSold;

    /** 販管費 */
    @Digits(integer = 9, fraction = 0)
    private String sga;

    /** 本社配賦 */
    @Digits(integer = 9, fraction = 0)
    private String allocationOfCorpExpenses;
}
