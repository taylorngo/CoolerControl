package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Coordinate type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Coordinates", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class Coordinate implements Model {
  public static final QueryField ID = field("Coordinate", "id");
  public static final QueryField DATETIME = field("Coordinate", "datetime");
  public static final QueryField LATITUDE = field("Coordinate", "latitude");
  public static final QueryField LONGITUDE = field("Coordinate", "longitude");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="AWSDateTime") Temporal.DateTime datetime;
  private final @ModelField(targetType="Float") List<Double> latitude;
  private final @ModelField(targetType="Float") List<Double> longitude;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String getId() {
      return id;
  }
  
  public Temporal.DateTime getDatetime() {
      return datetime;
  }
  
  public List<Double> getLatitude() {
      return latitude;
  }
  
  public List<Double> getLongitude() {
      return longitude;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Coordinate(String id, Temporal.DateTime datetime, List<Double> latitude, List<Double> longitude) {
    this.id = id;
    this.datetime = datetime;
    this.latitude = latitude;
    this.longitude = longitude;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Coordinate coordinate = (Coordinate) obj;
      return ObjectsCompat.equals(getId(), coordinate.getId()) &&
              ObjectsCompat.equals(getDatetime(), coordinate.getDatetime()) &&
              ObjectsCompat.equals(getLatitude(), coordinate.getLatitude()) &&
              ObjectsCompat.equals(getLongitude(), coordinate.getLongitude()) &&
              ObjectsCompat.equals(getCreatedAt(), coordinate.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), coordinate.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getDatetime())
      .append(getLatitude())
      .append(getLongitude())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Coordinate {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("datetime=" + String.valueOf(getDatetime()) + ", ")
      .append("latitude=" + String.valueOf(getLatitude()) + ", ")
      .append("longitude=" + String.valueOf(getLongitude()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static BuildStep builder() {
      return new Builder();
  }
  
  /**
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static Coordinate justId(String id) {
    return new Coordinate(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      datetime,
      latitude,
      longitude);
  }
  public interface BuildStep {
    Coordinate build();
    BuildStep id(String id);
    BuildStep datetime(Temporal.DateTime datetime);
    BuildStep latitude(List<Double> latitude);
    BuildStep longitude(List<Double> longitude);
  }
  

  public static class Builder implements BuildStep {
    private String id;
    private Temporal.DateTime datetime;
    private List<Double> latitude;
    private List<Double> longitude;
    @Override
     public Coordinate build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Coordinate(
          id,
          datetime,
          latitude,
          longitude);
    }
    
    @Override
     public BuildStep datetime(Temporal.DateTime datetime) {
        this.datetime = datetime;
        return this;
    }
    
    @Override
     public BuildStep latitude(List<Double> latitude) {
        this.latitude = latitude;
        return this;
    }
    
    @Override
     public BuildStep longitude(List<Double> longitude) {
        this.longitude = longitude;
        return this;
    }
    
    /**
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, Temporal.DateTime datetime, List<Double> latitude, List<Double> longitude) {
      super.id(id);
      super.datetime(datetime)
        .latitude(latitude)
        .longitude(longitude);
    }
    
    @Override
     public CopyOfBuilder datetime(Temporal.DateTime datetime) {
      return (CopyOfBuilder) super.datetime(datetime);
    }
    
    @Override
     public CopyOfBuilder latitude(List<Double> latitude) {
      return (CopyOfBuilder) super.latitude(latitude);
    }
    
    @Override
     public CopyOfBuilder longitude(List<Double> longitude) {
      return (CopyOfBuilder) super.longitude(longitude);
    }
  }
  
}
