package me.ahmedy.maven.CSVObjectify.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CSVElement {
	public int columnIndex() default 0;
	public boolean innerParsable() default false;
}
