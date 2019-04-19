package cn.woochen.share_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface WXShareEntry {
    /**
     * 包名
     */
    String packageName();

    /**
     * 类的Class
     */
    Class<?> entryClass();
}
