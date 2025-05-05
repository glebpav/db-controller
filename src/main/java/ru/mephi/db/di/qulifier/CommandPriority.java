package ru.mephi.db.di.qulifier;

import ru.mephi.db.domain.valueobject.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandPriority {
    Priority value() default Priority.LOWEST;
}
