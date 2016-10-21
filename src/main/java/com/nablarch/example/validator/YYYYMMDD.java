package com.nablarch.example.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import nablarch.core.util.DateUtil;
import nablarch.core.util.StringUtil;

import com.nablarch.example.validator.YYYYMMDD.YYYYMMDDValidator;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 日付書式チェック用バリデータのアノテーション。
 */
@Documented
@Constraint(validatedBy = { YYYYMMDDValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface YYYYMMDD {

    /** グループ */
    Class<?>[] groups() default { };

    /** デフォルトメッセージ */
    String message() default "{com.nablarch.example.validator.YYYYMMDD.message}";

    /** ペイロード */
    Class<? extends Payload>[] payload() default { };

    /** 許容する書式 */
    String allowFormat() default "yyyyMMdd";

    /** 複数指定用のアノテーション */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        /** YYYYMMDDの配列 */
        YYYYMMDD[] value();
    }

    /**
     * 日付書式チェックバリデータの本体。
     */
    class YYYYMMDDValidator implements ConstraintValidator<YYYYMMDD, String> {

        /** 許容する書式 */
        private String allowFormat;

        @Override
        public void initialize(YYYYMMDD constraintAnnotation) {
            this.allowFormat = constraintAnnotation.allowFormat();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtil.isNullOrEmpty(value)) {
                //値が設定されていない場合は、バリデーション成功とする。
                return true;
            }
            try {
                return DateUtil.getParsedDate(value, allowFormat) != null;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

}
