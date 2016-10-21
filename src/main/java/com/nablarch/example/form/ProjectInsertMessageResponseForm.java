package com.nablarch.example.form;

import java.io.Serializable;

/**
 * 応答電文に設定する項目を保持するためのForm。
 */
public class ProjectInsertMessageResponseForm implements Serializable {
    /**
     * serialVersionUID。
     */
    private static final long serialVersionUID = 1L;

    /**
     * リターンコード。
     */
    private String returnCode = "";

    /**
     * 詳細情報。
     */
    private String detail = "";

    /**
     * デフォルトコンストラクタ。
     */
    public ProjectInsertMessageResponseForm() {
    }

    /**
     * コンストラクタ。
     * @param returnCode リターンコード
     * @param detail 詳細情報
     */
    public ProjectInsertMessageResponseForm(String returnCode, String detail) {
        this.returnCode = returnCode;
        this.detail = detail;
    }

    /**
     * リターンコードを取得する。
     * @return リターンコード。
     */
    public String getReturnCode() {
        return returnCode;
    }

    /**
     * リターンコードを設定する。
     * @param returnCode リターンコード。
     */
    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * 詳細情報を取得する。
     * @return 詳細情報。
     */
    public String getDetail() {
        return detail;
    }

    /**
     * 詳細情報を設定する。
     * @param detail 詳細情報
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }
}
