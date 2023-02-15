package com.nablarch.example.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 日付書式チェック用バリデータのアノテーション。
 * @author Nabu Rakutaro
 */
@Documented
@Constraint(validatedBy = YYYYMMDDValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface YYYYMMDD {

    /**
     * グループ
     *
     * @return グループ
     */
    Class<?>[] groups() default { };

    /**
     * デフォルトメッセージ
     *
     * @return デフォルトのメッセージ
     */
    String message() default "{com.nablarch.example.validator.YYYYMMDD.message}";

    /**
     * ペイロード
     *
     * @return Payload
     */
    Class<? extends Payload>[] payload() default { };

    /**
     * 許容する書式
     *
     * @return 許容する日付のフォーマット(デフォルトはyyyyMMdd)
     */
    String allowFormat() default "yyyyMMdd";

    /** 複数指定用のアノテーション */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    @interface List {

        /**
         * YYYYMMDDの配列
         *
         * @return {@link YYYYMMDD}の配列
         */
        YYYYMMDD[] value();
    }

}
