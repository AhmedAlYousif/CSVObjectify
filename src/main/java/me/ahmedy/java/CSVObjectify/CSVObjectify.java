package me.ahmedy.java.CSVObjectify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import me.ahmedy.java.CSVObjectify.annotations.*;

public class CSVObjectify {

	private static void checkIfSerializable(Class<?> objClass) throws CSVAnnotationMissingException {
		if (!objClass.isAnnotationPresent(CSVParsable.class)) {
			throw new CSVAnnotationMissingException(objClass.getName());
		}
	}

	public static <T> ArrayList<T> parseFile(File file, Class<T> objClass) {
		checkIfSerializable(objClass);
		ArrayList<T> list = new ArrayList<T>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new CSVFileNotFoundException(file.getAbsolutePath());
		}
		String line = "";
		try {
			while((line = reader.readLine()) != null){
				list.add(parseLine(line, objClass));
			}
			reader.close();
		} catch (IOException e) {
			throw new CSVUnableToParseException(e);
		}
		return list;
	}

	public static <T> T parseLine(String line, Class<T> objClass) {
		checkIfSerializable(objClass);
		char sperator = objClass.getAnnotation(CSVParsable.class).sperator();
		CSVParser parser = new CSVParser(sperator);
		String[] values;
		try {
			values = parser.parseLine(line);
		} catch (IOException e) {
			throw new CSVUnableToParseException(e);
		}
		T object = null;
		try {
			object = (T) objClass.newInstance();
		} catch (InstantiationException e) {
			throw new CSVInstantitaionException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new CSVInstantitaionException(e.getMessage());
		}
		for(Field field : objClass.getFields()) {
			if(field.isAnnotationPresent(CSVElement.class)) {
				int elementKey = field.getAnnotation(CSVElement.class).columnIndex();
				boolean innerParsable = field.getAnnotation(CSVElement.class).innerParsable();
				Class<?> filedClass = field.getType();
				try {
					if(innerParsable) {
						field.set( object,  CSVObjectify.parseLine( values[elementKey], filedClass ));
					}
					else if(filedClass.equals(String.class))
						field.set(object, values[elementKey]);
					else if(filedClass.equals(int.class))
						field.set(object, Integer.parseInt(values[elementKey]));
					else if(filedClass.equals(float.class))
						field.set(object, Float.parseFloat(values[elementKey]));
					else if(filedClass.equals(double.class))
						field.set(object, Double.parseDouble(values[elementKey]));
					else if(filedClass.equals(boolean.class))
						field.set(object, Boolean.parseBoolean(values[elementKey]));
					else if(filedClass.equals(long.class))
						field.set(object, Long.parseLong(values[elementKey]));
					else if(filedClass.equals(char.class))
						field.set(object, (values[elementKey]).charAt(0));
					else if(filedClass.equals(short.class))
						field.set(object, Short.parseShort(values[elementKey]));
					else if(filedClass.equals(byte.class))
						field.set(object, Byte.parseByte(values[elementKey]));
					else {
						throw new CSVFieldTypeNotSupportedException(filedClass.getName());
					}
				} catch(IllegalAccessException e) {
					throw new CSVFieldAccessException(e);
				}
			}
		}
		for(Method method: objClass.getMethods()){
			if(method.isAnnotationPresent(CSVElement.class)) {
				int elementKey = method.getAnnotation(CSVElement.class).columnIndex();
				
				if(method.getParameterCount() != 1) {
					throw new CSVMethodAccessException("Method has more than 1 parameter.");
				}
				Class<?> methodParameterClass = method.getParameterTypes()[0];
				try {
					if(methodParameterClass.equals(String.class))
						method.invoke(object, values[elementKey]);
					else if(methodParameterClass.equals(int.class))
						method.invoke(object, Integer.parseInt(values[elementKey]));
					else if(methodParameterClass.equals(float.class))
						method.invoke(object, Float.parseFloat(values[elementKey]));
					else if(methodParameterClass.equals(double.class))
						method.invoke(object, Double.parseDouble(values[elementKey]));
					else if(methodParameterClass.equals(boolean.class))
						method.invoke(object, Boolean.parseBoolean(values[elementKey]));
					else if(methodParameterClass.equals(long.class))
						method.invoke(object, Long.parseLong(values[elementKey]));
					else if(methodParameterClass.equals(char.class))
						method.invoke(object, (values[elementKey]).charAt(0));
					else if(methodParameterClass.equals(short.class))
						method.invoke(object, Short.parseShort(values[elementKey]));
					else if(methodParameterClass.equals(byte.class))
						method.invoke(object, Byte.parseByte(values[elementKey]));
					else {
						throw new CSVMethodParameterTypeNotSupportedException(methodParameterClass.getName() , method.getParameters()[0].getName());
					}
				} catch (InvocationTargetException e) {
					throw new CSVMethodAccessException(e.getMessage());
				} catch (IllegalAccessException e) {
					throw new CSVMethodAccessException(e.getMessage());
				}
			}
		}
		return object;
	}
	public static class CSVAnnotationMissingException extends RuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4864732858610114034L;

		public CSVAnnotationMissingException(String className) {
			super("CSVParsaable annotation is missing from " + className);
		}
	}
	
	public static class CSVFieldTypeNotSupportedException extends RuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3320479695634509082L;

		public CSVFieldTypeNotSupportedException(String className) {
			super(className + " type is not supported");
		}
	}

	public static class CSVMethodParameterTypeNotSupportedException extends RuntimeException {

		/**
		 *
		 */
		private static final long serialVersionUID = -1788921030417742302L;

		public CSVMethodParameterTypeNotSupportedException(String className, String parameterName) {
			super(className + " type of " + parameterName +" parameter is not supported");
		}
	}

	public static class CSVMethodHasMultipleParametersException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2787500890908173424L;

		public CSVMethodHasMultipleParametersException() {
			super("Methods annotated with CSVElement should only have one parameter.");
		}
	}
	
	public static class CSVUnableToParseException extends RuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3939595853090544103L;

		public CSVUnableToParseException(IOException e) {
			super("Unable to parse: " + e.getMessage());
		}
	}
	
	public static class CSVInstantitaionException extends RuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4410785185055084575L;

		public CSVInstantitaionException(String message) {
			super("Unable to instantite object: " + message);
		}
	}
	
	public static class CSVFieldAccessException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4239165543329212224L;

		public CSVFieldAccessException(IllegalAccessException e) {
			super("Unable access field: " + e.getMessage());
		}
	}
	
	public static class CSVMethodAccessException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7442469314746663098L;

		public CSVMethodAccessException(String message) {
			super("Unable access method: " + message);
		}
	}
	
	public static class CSVFileNotFoundException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7442469314746663098L;

		public CSVFileNotFoundException(String filePath) {
			super("Unable to find the CSV file: " + filePath);
		}
	}
}
