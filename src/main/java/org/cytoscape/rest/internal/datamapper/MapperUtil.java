package org.cytoscape.rest.internal.datamapper;
      
import com.fasterxml.jackson.databind.JsonNode;

public class MapperUtil {

  /**
   * 
   * @param type The simple name of a Java class
   * @param treatNumberAsDouble In some cases, a Number class may have to be treated as a 
   * Double. An example of this is Column generation, which requires that the data type of 
   * the column be one of Java's primitive number types or a List. In this case, this 
   * parameter for the should be set to true, and the for Number type, the class returned 
   * will be Double. For other implementations that require a Number type be returned as 
   * the Number class, this parameter should be false
   * @return
   */
  public static final Class<?> getColumnClass(final String type, final boolean treatNumberAsDouble) {
    if (type.equals(Double.class.getSimpleName())) {
      return Double.class;
    } else if (type.equals(Long.class.getSimpleName())) {
      return Long.class;
    } else if (type.equals(Integer.class.getSimpleName())) {
      return Integer.class;
    } else if (type.equals(Float.class.getSimpleName())) {
      return Float.class;
    } else if (type.equals(Boolean.class.getSimpleName())) {
      return Boolean.class;
    } else if (type.equals(String.class.getSimpleName())) {
      return String.class;
    } else if (type.equals(Number.class.getSimpleName())) {
      return treatNumberAsDouble ? Double.class : Number.class;
    } else {
      return null;
    }
  }
  
  public static final Object getRawValue(final String queryString, Class<?> type) {
    Object raw = queryString;

    if (type == Boolean.class) {
      raw = Boolean.parseBoolean(queryString);
    } else if (type == Double.class) {
      raw = Double.parseDouble(queryString);
    } else if (type == Integer.class) {
      raw = Integer.parseInt(queryString);
    } else if (type == Long.class) {
      raw = Long.parseLong(queryString);
    } else if (type == Float.class) {
      raw = Float.parseFloat(queryString);
    }
    return raw;
  }
  
  public static final Object getValue(final JsonNode value, final Class<?> type) {
    if (type == String.class) {
      return value.asText();
    } else if (type == Boolean.class || type.getSimpleName() == "boolean") {
      return value.asBoolean();
    } else if (type == Double.class || type.getSimpleName() == "double") {
      return value.asDouble();
    } else if (type == Integer.class || type.getSimpleName() == "int") {
      return value.asInt();
    } else if (type == Long.class || type.getSimpleName() == "long") {
      return value.asLong();
    } else if (type == Float.class || type.getSimpleName() == "float") {
      return value.asDouble();
    } else {
      return null;
    }
  }
} 

