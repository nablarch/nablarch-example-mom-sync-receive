package com.nablarch.example.domain;

import nablarch.core.validation.ee.DomainManager;

/**
 * {@link DomainManager} の実装クラス。
 * <p/>
 * ドメインを定義したBeanクラスを返却する。
 */
public final class ExampleDomainManager implements DomainManager<ExampleDomainBean> {

    @Override
    public Class<ExampleDomainBean> getDomainBean() {
        // ドメインBeanのClassオブジェクトを返す。
        return ExampleDomainBean.class;
    }
}
