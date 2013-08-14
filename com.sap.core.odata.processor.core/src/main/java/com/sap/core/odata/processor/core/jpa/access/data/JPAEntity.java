package com.sap.core.odata.processor.core.jpa.access.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmNavigationProperty;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmStructuralType;
import com.sap.core.odata.api.edm.EdmTypeKind;
import com.sap.core.odata.api.edm.EdmTyped;
import com.sap.core.odata.api.ep.entry.ODataEntry;
import com.sap.core.odata.api.ep.feed.ODataFeed;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmMapping;

public class JPAEntity {

  private Object jpaEntity = null;
  //private Object jpaEmbeddableKey = null;
  private EdmEntityType oDataEntityType = null;
  private EdmEntitySet oDataEntitySet = null;
  private Class<?> jpaType = null;
  private HashMap<String, Method> accessModifiersWrite = null;
  private JPAEntityParser jpaEntityParser = null;
  private boolean isKeyBuilt = false;
  public HashMap<EdmNavigationProperty, EdmEntitySet> inlinedEntities = null;

  public JPAEntity(EdmEntityType oDataEntityType, EdmEntitySet oDataEntitySet) {
    this.oDataEntityType = oDataEntityType;
    this.oDataEntitySet = oDataEntitySet;
    try {
      JPAEdmMapping mapping = (JPAEdmMapping) oDataEntityType.getMapping();
      this.jpaType = mapping.getJPAType();
    } catch (EdmException e) {
      return;
    }
    jpaEntityParser = JPAEntityParser.create();
  }

  public void setAccessModifersWrite(HashMap<String, Method> accessModifiersWrite) {
    this.accessModifiersWrite = accessModifiersWrite;
  }

  public Object getJPAEntity() {
    return jpaEntity;
  }

  @SuppressWarnings("unchecked")
  private void write(Map<String, Object> oDataEntryProperties, boolean isCreate) throws ODataJPARuntimeException {
    try {

      EdmStructuralType structuralType = null;
      final List<String> keyNames = oDataEntityType.getKeyPropertyNames();

      if (isCreate)
        jpaEntity = instantiateJPAEntity();
      else if (jpaEntity == null)
        throw ODataJPARuntimeException
            .throwException(ODataJPARuntimeException.RESOURCE_NOT_FOUND, null);

      if (accessModifiersWrite == null)
        accessModifiersWrite = jpaEntityParser.getAccessModifiers(jpaEntity, oDataEntityType, JPAEntityParser.ACCESS_MODIFIER_SET);

      if (oDataEntityType == null || oDataEntryProperties == null)
        throw ODataJPARuntimeException
            .throwException(ODataJPARuntimeException.GENERAL, null);

      final HashMap<String, String> embeddableKeys = jpaEntityParser.getJPAEmbeddableKeyMap(jpaEntity.getClass().getName());

      for (String propertyName : oDataEntryProperties.keySet()) {
        EdmTyped edmTyped = (EdmTyped) oDataEntityType.getProperty(propertyName);

        Method accessModifier = null;

        switch (edmTyped.getType().getKind()) {
        case SIMPLE:
          if (isCreate == false) {
            if (keyNames.contains(edmTyped.getName()))
              continue;
          }
          accessModifier = accessModifiersWrite.get(propertyName);
          if (embeddableKeys != null && isKeyBuilt == false) {
            isKeyBuilt = true;
          }
          else
            setProperty(accessModifier, jpaEntity, oDataEntryProperties.get(propertyName));
          break;
        case COMPLEX:
          structuralType = (EdmStructuralType) edmTyped.getType();
          accessModifier = accessModifiersWrite.get(propertyName);
          setComplexProperty(accessModifier, jpaEntity,
              structuralType,
              (HashMap<String, Object>) oDataEntryProperties.get(propertyName));
          break;
        case NAVIGATION:
        case ENTITY:
          structuralType = (EdmStructuralType) edmTyped.getType();
          accessModifier = jpaEntityParser.getAccessModifier(jpaEntity, (EdmNavigationProperty) edmTyped, JPAEntityParser.ACCESS_MODIFIER_SET);
          EdmEntitySet edmRelatedEntitySet = oDataEntitySet.getRelatedEntitySet((EdmNavigationProperty) edmTyped);
          List<ODataEntry> relatedEntries = (List<ODataEntry>) oDataEntryProperties.get(propertyName);
          List<Object> relatedJPAEntites = new ArrayList<Object>();
          JPAEntity relatedEntity = new JPAEntity((EdmEntityType) structuralType, edmRelatedEntitySet);
          for (ODataEntry oDataEntry : relatedEntries) {
            relatedEntity.create(oDataEntry);
            relatedJPAEntites.add(relatedEntity.getJPAEntity());
          }
          EdmNavigationProperty navProperty = (EdmNavigationProperty) edmTyped;
          switch (navProperty.getMultiplicity()) {
          case MANY:
            accessModifier.invoke(jpaEntity, relatedJPAEntites);
            break;
          case ONE:
          case ZERO_TO_ONE:
            accessModifier.invoke(jpaEntity, relatedJPAEntites.get(0));
            break;
          }

          if (inlinedEntities == null)
            inlinedEntities = new HashMap<EdmNavigationProperty, EdmEntitySet>();

          inlinedEntities.put((EdmNavigationProperty) edmTyped, edmRelatedEntitySet);
        default:
          continue;
        }
      }
    } catch (Exception e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
  }

  public void create(ODataEntry oDataEntry) throws ODataJPARuntimeException {
    if (oDataEntry == null)
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL, null);
    Map<String, Object> oDataEntryProperties = oDataEntry.getProperties();
    if (oDataEntry.containsInlineEntry()) {
      try {
        for (String navigationPropertyName : oDataEntityType.getNavigationPropertyNames()) {
          ODataFeed feed = (ODataFeed) oDataEntry.getProperties().get(navigationPropertyName);
          if (feed == null) continue;
          List<ODataEntry> relatedEntries = feed.getEntries();
          oDataEntryProperties.put(navigationPropertyName, relatedEntries);
        }
      } catch (EdmException e) {
        throw ODataJPARuntimeException
            .throwException(ODataJPARuntimeException.GENERAL
                .addContent(e.getMessage()), e);
      }
    }
    write(oDataEntryProperties, true);
  }

  public void create(Map<String, Object> oDataEntryProperties) throws ODataJPARuntimeException {
    write(oDataEntryProperties, true);
  }

  public void update(ODataEntry oDataEntry) throws ODataJPARuntimeException {
    if (oDataEntry == null)
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL, null);
    Map<String, Object> oDataEntryProperties = oDataEntry.getProperties();
    write(oDataEntryProperties, false);
  }

  public void update(Map<String, Object> oDataEntryProperties) throws ODataJPARuntimeException {
    write(oDataEntryProperties, false);
  }

  @SuppressWarnings("unchecked")
  protected void setComplexProperty(Method accessModifier, Object jpaEntity, EdmStructuralType edmComplexType, HashMap<String, Object> propertyValue)
      throws EdmException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ODataJPARuntimeException {

    JPAEdmMapping mapping = (JPAEdmMapping) edmComplexType.getMapping();
    Object embeddableObject = mapping.getJPAType().newInstance();
    accessModifier.invoke(jpaEntity, embeddableObject);

    HashMap<String, Method> accessModifiers = jpaEntityParser.getAccessModifiers(embeddableObject, edmComplexType, JPAEntityParser.ACCESS_MODIFIER_SET);

    for (String edmPropertyName : edmComplexType.getPropertyNames()) {
      EdmTyped edmTyped = (EdmTyped) edmComplexType.getProperty(edmPropertyName);
      accessModifier = accessModifiers.get(edmPropertyName);
      if (edmTyped.getType().getKind().toString().equals(EdmTypeKind.COMPLEX)) {
        EdmStructuralType structualType = (EdmStructuralType) edmTyped.getType();
        setComplexProperty(accessModifier, embeddableObject, structualType, (HashMap<String, Object>) propertyValue.get(edmPropertyName));
      }
      else
        setProperty(accessModifier, embeddableObject, propertyValue.get(edmPropertyName));
    }
  }

  protected void setProperty(Method method, Object entity, Object entityPropertyValue) throws
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (entityPropertyValue != null)
      method.invoke(entity, entityPropertyValue);
  }

  //  protected void setEmbeddableKeyProperty(HashMap<String, String> embeddableKeys, List<EdmProperty> oDataEntryKeyProperties,
  //      Map<String, Object> oDataEntryProperties, Object entity)
  //      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
  //      InvocationTargetException, EdmException, InstantiationException {
  //
  //    HashMap<String, Object> embeddableTypeMap = new HashMap<String, Object>();
  //
  //    for (EdmProperty edmProperty : oDataEntryKeyProperties)
  //    {
  //      if (oDataEntryProperties.containsKey(edmProperty.getName()) == false)
  //        continue;
  //
  //      String edmPropertyName = edmProperty.getName();
  //      String embeddableKeyNameComposite = embeddableKeys.get(edmPropertyName);
  //      String embeddableKeyNameSplit[] = embeddableKeyNameComposite.split("\\.");
  //
  //      JPAEdmMapping mapping = (JPAEdmMapping) edmProperty.getMapping();
  //
  //      for (int i = mapping.getJPATypeHierachy().length - 1; i >= 0; i--)
  //      {
  //        String typeName = mapping.getJPATypeHierachy()[i].getName();
  //        Object embeddableObj = embeddableTypeMap.get(typeName);
  //        if (embeddableObj == null) {
  //          embeddableObj = instantiateEmbeddableKey(edmProperty, i);
  //          embeddableTypeMap.put(typeName, embeddableObj);
  //          Method method = entity.getClass().getMethod(embeddableKeyNameSplit[0], embeddableObj.getClass());
  //          method.invoke(entity, embeddableObj);
  //        }
  //
  //        Object oDataEntryKeyPropertyValue = oDataEntryProperties.get(edmPropertyName);
  //
  //        Method method = embeddableObj.getClass().getMethod(embeddableKeyNameSplit[i + 1], oDataEntryKeyPropertyValue.getClass());
  //        method.invoke(embeddableObj, oDataEntryKeyPropertyValue);
  //      }
  //
  //    }
  //  }

  protected Object instantiateEmbeddableKey(EdmProperty edmProperty, int nestingLevel) throws EdmException, InstantiationException, IllegalAccessException {
    JPAEdmMapping propertyMapping = (JPAEdmMapping) edmProperty.getMapping();
    if (propertyMapping == null || propertyMapping.getJPAType() == null) throw new InstantiationException();

    Object key = propertyMapping.getJPATypeHierachy()[nestingLevel].newInstance();
    return key;
  }

  protected Object instantiateJPAEntity() throws InstantiationException, IllegalAccessException {
    if (jpaType == null) throw new InstantiationException();

    return jpaType.newInstance();
  }

  public HashMap<EdmNavigationProperty, EdmEntitySet> getInlineJPAEntities() {
    return this.inlinedEntities;
  }

  public void setJPAEntity(Object jpaEntity) {
    this.jpaEntity = jpaEntity;
  }
}
