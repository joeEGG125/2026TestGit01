package com.syscom.fep.frmcommon.xs.converter;

import com.syscom.fep.frmcommon.xs.entity.XsOriginalNode;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.util.Fields;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.core.util.MemberDictionary;
import com.thoughtworks.xstream.core.util.Primitives;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.Mapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class XsDefaultReflectionConverter extends ReflectionConverter {
    private transient ReflectionProvider pureJavaReflectionProvider;
    // private final transient Map<String, XsOriginalNode> xsOriginalNodeMap = new HashMap<>();

    public XsDefaultReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
        super(mapper, reflectionProvider);
    }

    public XsDefaultReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider, Class type) {
        super(mapper, reflectionProvider, type);
    }

    @Override
    public Object doUnmarshal(final Object result, final HierarchicalStreamReader reader,
                              final UnmarshallingContext context) {
        final Class resultType = result.getClass();
        final MemberDictionary seenFields = new MemberDictionary();

        // process attributes before recursing into child elements.
        Iterator it = reader.getAttributeNames();
        while (it.hasNext()) {
            String attrAlias = (String) it.next();
            // TODO: realMember should return FastField

            // 2025-02-11 Richard modified start
            // String attrName = mapper
            //         .realMember(resultType, mapper.attributeForAlias(attrAlias));
            String attrName = getAttrName(result, resultType, attrAlias);
            // 2025-02-11 Richard modified end

            Field field = reflectionProvider.getFieldOrNull(resultType, attrName);
            if (field != null && shouldUnmarshalField(field)) {
                Class classDefiningField = field.getDeclaringClass();
                if (!mapper.shouldSerializeMember(classDefiningField, attrName)) {
                    continue;
                }

                // we need a converter that produces a string representation only
                SingleValueConverter converter = mapper.getConverterFromAttribute(
                        classDefiningField, attrName, field.getType());
                Class type = field.getType();
                if (converter != null) {
                    Object value = converter.fromString(reader.getAttribute(attrAlias));
                    if (type.isPrimitive()) {
                        type = Primitives.box(type);
                    }
                    if (value != null && !type.isAssignableFrom(value.getClass())) {
                        ConversionException exception = new ConversionException("Cannot convert type");
                        exception.add("source-type", value.getClass().getName());
                        exception.add("target-type", type.getName());
                        throw exception;
                    }
                    if (!seenFields.add(classDefiningField, attrName)) {
                        throw new DuplicateFieldException(attrName);
                    }
                    reflectionProvider.writeField(result, attrName, value, classDefiningField);
                }
            }
        }

        Map implicitCollectionsForCurrentObject = null;
        final Map<String, XsOriginalNode> xsOriginalNodeMap = new HashMap<>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String originalNodeName = reader.getNodeName();

            // 2025-02-11 Richard add start
            XsOriginalNode xsOriginalNode = xsOriginalNodeMap.get(originalNodeName);
            if (xsOriginalNode == null) {
                xsOriginalNode = new XsOriginalNode();
                xsOriginalNode.setName(originalNodeName);
                xsOriginalNode.setCount(1);
                xsOriginalNodeMap.put(originalNodeName, xsOriginalNode);
            } else {
                xsOriginalNode.accumulateCount(1);
            }
            // 2025-02-11 Richard add end

            Class explicitDeclaringClass = readDeclaringClass(reader);
            Class fieldDeclaringClass = explicitDeclaringClass == null
                    ? resultType
                    : explicitDeclaringClass;

            // 2025-02-11 Richard modified start
            // String fieldName = mapper.realMember(fieldDeclaringClass, originalNodeName);
            String fieldName = getFieldName(result, originalNodeName, fieldDeclaringClass, xsOriginalNode);
            // 2025-02-11 Richard modified end

            Mapper.ImplicitCollectionMapping implicitCollectionMapping = mapper
                    .getImplicitCollectionDefForFieldName(fieldDeclaringClass, fieldName);
            final Object value;
            String implicitFieldName = null;
            Field field = null;
            Class type = null;
            if (implicitCollectionMapping == null) {
                // no item of an implicit collection for this name ... do we have a field?
                field = reflectionProvider.getFieldOrNull(fieldDeclaringClass, fieldName);
                if (field == null) {
                    // it is not a field ... do we have a field alias?
                    Class itemType = mapper.getItemTypeForItemFieldName(fieldDeclaringClass, fieldName);
                    if (itemType != null) {
                        String classAttribute = HierarchicalStreams.readClassAttribute(
                                reader, mapper);
                        if (classAttribute != null) {
                            type = mapper.realClass(classAttribute);
                        } else {
                            type = itemType;
                        }
                    } else {
                        // it is not an alias ... do we have an element of an implicit
                        // collection based on type only?
                        try {
                            type = mapper.realClass(originalNodeName);
                            implicitFieldName = mapper.getFieldNameForItemTypeAndName(
                                    fieldDeclaringClass, type, originalNodeName);
                        } catch (CannotResolveClassException e) {
                            // type stays null ...
                        }
                        if (type == null || (type != null && implicitFieldName == null)) {
                            // either not a type or element is a type alias, but does not
                            // belong to an implicit field
                            handleUnknownField(
                                    explicitDeclaringClass, fieldName, fieldDeclaringClass, originalNodeName);

                            // element is unknown in declaring class, ignore it now
                            type = null;
                        }
                    }
                    if (type == null) {
                        // no type, no value
                        value = null;
                    } else {
                        if (Map.Entry.class.equals(type)) {
                            // it is an element of an implicit map with two elements now for
                            // key and value
                            reader.moveDown();
                            final Object key = context.convertAnother(
                                    result, HierarchicalStreams.readClassType(reader, mapper));
                            reader.moveUp();
                            reader.moveDown();
                            final Object v = context.convertAnother(
                                    result, HierarchicalStreams.readClassType(reader, mapper));
                            reader.moveUp();
                            value = Collections.singletonMap(key, v)
                                    .entrySet().iterator().next();
                        } else {
                            // recurse info hierarchy
                            value = context.convertAnother(result, type);
                        }
                    }
                } else {
                    boolean fieldAlreadyChecked = false;

                    // we have a field, but do we have to address a hidden one?
                    if (explicitDeclaringClass == null) {
                        while (field != null
                                && !(fieldAlreadyChecked = shouldUnmarshalField(field)
                                && mapper.shouldSerializeMember(
                                field.getDeclaringClass(), fieldName))) {
                            field = reflectionProvider.getFieldOrNull(field
                                    .getDeclaringClass()
                                    .getSuperclass(), fieldName);
                        }
                    }
                    if (field != null
                            && (fieldAlreadyChecked || (shouldUnmarshalField(field) && mapper
                            .shouldSerializeMember(field.getDeclaringClass(), fieldName)))) {
                        String classAttribute = HierarchicalStreams.readClassAttribute(
                                reader, mapper);
                        if (classAttribute != null) {
                            type = mapper.realClass(classAttribute);
                        } else {
                            type = mapper.defaultImplementationOf(field.getType());
                        }
                        // TODO the reflection provider should already return the proper field
                        value = unmarshallField(context, result, type, field);
                        Class definedType = field.getType();
                        if (!definedType.isPrimitive()) {
                            type = definedType;
                        }
                    } else {
                        value = null;
                    }
                }
            } else {
                // we have an implicit collection with defined names
                implicitFieldName = implicitCollectionMapping.getFieldName();
                type = implicitCollectionMapping.getItemType();
                if (type == null) {
                    String classAttribute = HierarchicalStreams.readClassAttribute(
                            reader, mapper);
                    type = mapper.realClass(classAttribute != null
                            ? classAttribute
                            : originalNodeName);
                }
                value = context.convertAnother(result, type);
            }

            if (value != null && type != null && !type.isAssignableFrom(value.getClass())) {
                throw new ConversionException("Cannot convert type "
                        + value.getClass().getName()
                        + " to type "
                        + type.getName());
            }

            if (field != null) {
                reflectionProvider.writeField(result, fieldName, value, field.getDeclaringClass());
                if (!seenFields.add(field.getDeclaringClass(), fieldName)) {
                    throw new DuplicateFieldException(fieldName);
                }
            } else if (type != null) {
                if (implicitFieldName == null) {
                    // look for implicit field
                    if (value != null) {
                        implicitFieldName = mapper.getFieldNameForItemTypeAndName(
                                fieldDeclaringClass,
                                value.getClass(),
                                originalNodeName);
                    } else {
                        implicitFieldName = mapper.getFieldNameForItemTypeAndName(
                                fieldDeclaringClass,
                                Mapper.Null.class,
                                originalNodeName);
                    }

                }
                if (implicitCollectionsForCurrentObject == null) {
                    implicitCollectionsForCurrentObject = new HashMap();
                }
                writeValueToImplicitCollection(
                        value, implicitCollectionsForCurrentObject, result, new FieldLocation(
                                implicitFieldName, fieldDeclaringClass));
            }

            reader.moveUp();
        }

        if (implicitCollectionsForCurrentObject != null) {
            for (Iterator iter = implicitCollectionsForCurrentObject.entrySet().iterator(); iter
                    .hasNext(); ) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object value = entry.getValue();
                if (value instanceof ArraysList) {
                    Object array = ((ArraysList) value).toPhysicalArray();
                    final FieldLocation fieldLocation = (FieldLocation) entry.getKey();
                    final Field field = reflectionProvider.getFieldOrNull(fieldLocation.definedIn,
                            fieldLocation.fieldName);
                    reflectionProvider.writeField(result, fieldLocation.fieldName, array, field != null
                            ? field.getDeclaringClass()
                            : null);
                }
            }
        }

        return result;
    }

    protected String getAttrName(Object entity, Class resultType, String attrAlias) {
        return mapper.realMember(resultType, mapper.attributeForAlias(attrAlias));
    }

    protected String getFieldName(Object entity, String originalNodeName, Class fieldDeclaringClass, XsOriginalNode xsOriginalNode) {
        return mapper.realMember(fieldDeclaringClass, originalNodeName);
    }

    private Class readDeclaringClass(HierarchicalStreamReader reader) {
        String attributeName = mapper.aliasForSystemAttribute("defined-in");
        String definedIn = attributeName == null ? null : reader.getAttribute(attributeName);
        return definedIn == null ? null : mapper.realClass(definedIn);
    }

    private void writeValueToImplicitCollection(Object value, Map implicitCollections, Object result, final FieldLocation fieldLocation) {
        Collection collection = (Collection) implicitCollections.get(fieldLocation);
        if (collection == null) {
            final Field field = reflectionProvider.getFieldOrNull(fieldLocation.definedIn, fieldLocation.fieldName);
            Class physicalFieldType = field != null
                    ? field.getType()
                    : reflectionProvider.getFieldType(result, fieldLocation.fieldName, null);
            if (physicalFieldType.isArray()) {
                collection = new ArraysList(physicalFieldType);
            } else {
                Class fieldType = mapper.defaultImplementationOf(physicalFieldType);
                if (!(Collection.class.isAssignableFrom(fieldType) || Map.class
                        .isAssignableFrom(fieldType))) {
                    ObjectAccessException oaex = new ObjectAccessException(
                            "Field is configured for an implicit Collection or Map, but is of an incompatible type");
                    oaex.add("field", result.getClass().getName() + "." + fieldLocation.fieldName);
                    oaex.add("field-type", fieldType.getName());
                    throw oaex;
                }
                if (pureJavaReflectionProvider == null) {
                    pureJavaReflectionProvider = new PureJavaReflectionProvider();
                }
                Object instance = pureJavaReflectionProvider.newInstance(fieldType);
                if (instance instanceof Collection) {
                    collection = (Collection) instance;
                } else {
                    Mapper.ImplicitCollectionMapping implicitCollectionMapping = mapper
                            .getImplicitCollectionDefForFieldName(fieldLocation.definedIn, fieldLocation.fieldName);
                    collection = new MappingList(
                            (Map) instance, implicitCollectionMapping.getKeyFieldName());
                }
                reflectionProvider.writeField(result, fieldLocation.fieldName, instance, field != null
                        ? field.getDeclaringClass()
                        : null);
            }
            implicitCollections.put(fieldLocation, collection);
        }
        collection.add(value);
    }

    private void handleUnknownField(Class classDefiningField, String fieldName,
                                    Class resultType, String originalNodeName) {
        if (classDefiningField == null) {
            for (Class cls = resultType; cls != null; cls = cls.getSuperclass()) {
                if (!mapper.shouldSerializeMember(cls, originalNodeName)) {
                    return;
                }
            }
        }
        throw new UnknownFieldException(resultType.getName(), fieldName);
    }

    private static class FieldLocation {
        final String fieldName;
        final Class definedIn;

        FieldLocation(final String fieldName, final Class definedIn) {
            this.fieldName = fieldName;
            this.definedIn = definedIn;
        }

        @Override
        public int hashCode() {
            // final int prime = 7;
            int result = 1;
            result = 7 * result + (definedIn == null ? 0 : definedIn.getName().hashCode());
            result = 7 * result + (fieldName == null ? 0 : fieldName.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FieldLocation other = (FieldLocation) obj;
            if (definedIn != other.definedIn) {
                return false;
            }
            if (fieldName == null) {
                if (other.fieldName != null) {
                    return false;
                }
            } else if (!fieldName.equals(other.fieldName)) {
                return false;
            }
            return true;
        }
    }

    private static class ArraysList extends ArrayList {
        final Class physicalFieldType;

        ArraysList(Class physicalFieldType) {
            this.physicalFieldType = physicalFieldType;
        }

        Object toPhysicalArray() {
            Object[] objects = toArray();
            Object array = Array.newInstance(
                    physicalFieldType.getComponentType(), objects.length);
            if (physicalFieldType.getComponentType().isPrimitive()) {
                for (int i = 0; i < objects.length; ++i) {
                    Array.set(array, i, Array.get(objects, i));
                }
            } else {
                System.arraycopy(objects, 0, array, 0, objects.length);
            }
            return array;
        }
    }

    private class MappingList extends AbstractList {

        private final Map map;
        private final String keyFieldName;
        private final Map fieldCache = new HashMap();

        public MappingList(Map map, String keyFieldName) {
            this.map = map;
            this.keyFieldName = keyFieldName;
        }

        @Override
        public boolean add(Object object) {
            if (object == null) {
                boolean containsNull = !map.containsKey(null);
                map.put(null, null);
                return containsNull;
            }
            Class itemType = object.getClass();

            if (keyFieldName != null) {
                Field field = (Field) fieldCache.get(itemType);
                if (field == null) {
                    field = reflectionProvider.getField(itemType, keyFieldName);
                    fieldCache.put(itemType, field);
                }
                if (field != null) {
                    Object key = Fields.read(field, object);
                    return map.put(key, object) == null;
                }
            } else if (object instanceof Map.Entry) {
                final Map.Entry entry = (Map.Entry) object;
                return map.put(entry.getKey(), entry.getValue()) == null;
            }

            ConversionException exception =
                    new ConversionException("Element  is not defined as entry for implicit map");
            exception.add("map-type", map.getClass().getName());
            exception.add("element-type", object.getClass().getName());
            throw exception;
        }

        @Override
        public Object get(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }
    }
}
