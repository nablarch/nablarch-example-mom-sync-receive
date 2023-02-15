package com.nablarch.example.validator;

import nablarch.core.util.DateUtil;
import nablarch.core.util.StringUtil;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 日付書式チェックバリデータの本体。
 *
 * @author Nabu Rakutaro
 */
public class YYYYMMDDValidator implements ConstraintValidator<YYYYMMDD, String> {

    /** 許容する書式 */
    private String allowFormat;

    @Override
    public void initialize(YYYYMMDD constraintAnnotation) {
        allowFormat = constraintAnnotation.allowFormat();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtil.isNullOrEmpty(value)) {
            //値が設定されていない場合は、バリデーション成功とする。
            return true;
        }
        try {
            return DateUtil.getParsedDate(value, allowFormat) != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
