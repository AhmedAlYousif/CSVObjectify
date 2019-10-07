package me.ahmedy.maven.CSVObjectify;

import java.io.IOException;
import java.lang.reflect.Field;
import com.opencsv.CSVParser;
import me.ahmedy.maven.CSVObjectify.annotations.*;

public class CSVObjectify 
{
    
    private static void checkIfSerializable(Class<?> objClass) throws CSVAnnotationMissingException {
	    if (!objClass.isAnnotationPresent(me.ahmedy.maven.CSVObjectify.annotations.CSVParsable.class)) {
	        throw new CSVAnnotationMissingException(objClass.getName());
	    }
	}
	
	public static <T> T parseLine(String line, Class<T> objClass) throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException, CSVAnnotationMissingException, CSVFieldTypeNotSupportedException {
		checkIfSerializable(objClass);
		char sperator = objClass.getAnnotation(CSVParsable.class).sperator();
		CSVParser parser = new CSVParser(sperator);
		String[] values = parser.parseLine(line);
		T object = (T) objClass.newInstance();
		for(Field field : objClass.getFields()) {
			if(field.isAnnotationPresent(CSVElement.class)) {
				int elementKey = field.getAnnotation(CSVElement.class).columnIndex();
				boolean innerParsable = field.getAnnotation(CSVElement.class).innerParsable();
				if(innerParsable) {
					field.set( object,  CSVObjectify.parseLine( values[elementKey], field.getType() ));
				}
				else if(field.getType().equals(String.class))
					field.set(object, values[elementKey]);
				else if(field.getType().equals(int.class))
					field.set(object, Integer.parseInt(values[elementKey]));
				else if(field.getType().equals(float.class))
					field.set(object, Float.parseFloat(values[elementKey]));
				else if(field.getType().equals(double.class))
					field.set(object, Double.parseDouble(values[elementKey]));
				else if(field.getType().equals(boolean.class))
					field.set(object, Boolean.parseBoolean(values[elementKey]));
				else if(field.getType().equals(long.class))
					field.set(object, Long.parseLong(values[elementKey]));
				else if(field.getType().equals(char.class))
					field.set(object, (values[elementKey]).charAt(0));
				else if(field.getType().equals(short.class))
					field.set(object, Short.parseShort(values[elementKey]));
				else if(field.getType().equals(byte.class))
					field.set(object, Byte.parseByte(values[elementKey]));
				else {
					throw new CSVFieldTypeNotSupportedException(field.getType().getName());
				}
			}
		}
		return object;
	}
	public static class CSVAnnotationMissingException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4864732858610114034L;

		public CSVAnnotationMissingException(String className) {
			super("CSVParsaable annotation is missing from " + className);
		}
	}
	
	public static class CSVFieldTypeNotSupportedException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3320479695634509082L;

		public CSVFieldTypeNotSupportedException(String className) {
			super(className + " is not supported");
		}
	}
}
