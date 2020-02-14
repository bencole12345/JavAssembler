package ast.types;

import errors.DuplicateClassAttributeException;
import errors.IllegalPrivateAccessException;
import errors.InvalidAttributeException;
import errors.UnknownClassException;
import parser.ParserUtil;
import util.ClassTable;
import util.FunctionTableEntry;
import util.LookupTrie;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a known Java class.
 */
public class JavaClass extends JavaClassReference {

    /**
     * The name of the class
     */
    private String name;

    /**
     * A map of name -> ClassAttribute containing all public and private
     * attributes defined within this class.
     */
    private Map<String, AllocatedClassAttribute> attributesMap;

    /**
     * Reference to parent class - will be null if this is the top of the
     * hierarchy.
     *
     * This is used for looking up public attributes and methods defined in
     * parent classes.
     */
    private JavaClass parent;

    /**
     * Tracks how much memory has been allocated in this class.
     *
     * This will be used by any subclasses so that they can safely allocate
     * memory for additional state they have without overwriting any attributes
     * defined in this class, or any of this class's parents.
     */
    private int nextFreeAssignmentOffset;

    /**
     * The virtual table for this class.
     */
    private List<FunctionTableEntry> virtualTable;

    /**
     * A map of strings (function names) to tries used for efficiently looking
     * up the virtual table index of a method, given its name and type
     * signature.
     */
    private Map<String, LookupTrie<Integer, Type>> vtableIndexLookupTrieMap;

    public JavaClass(String name, List<ClassAttribute> attributes, JavaClass parent)
            throws DuplicateClassAttributeException {
        this.name = name;
        this.parent = parent;
        this.attributesMap = buildAttributeMap(attributes);
        vtableIndexLookupTrieMap = new HashMap<>();
        virtualTable = new ArrayList<>();
        if (parent != null)
            virtualTable.addAll(parent.virtualTable);
    }

    /**
     * Builds a map from attribute names to AllocatedClassAttribute objects.
     *
     * This method will handle checking that each name is unique - that is, for
     * every attribute name in this class, there is no other attribute in this
     * class with the same name, nor is there a public attribute in a parent
     * class with the same name.
     *
     * @param attributes The list of attributes defined in the class body
     * @return A map from attribute names to AllocatedClassAttribute objects
     * @throws DuplicateClassAttributeException
     */
    private Map<String, AllocatedClassAttribute> buildAttributeMap(List<ClassAttribute> attributes)
            throws DuplicateClassAttributeException {
        Map<String, AllocatedClassAttribute> attributeMap = new HashMap<>();
        for (ClassAttribute attribute : attributes) {
            String attributeName = attribute.getName();
            if (attributeMap.containsKey(attributeName)
                    || (parent != null && parent.hasPublicAttribute(attributeName))) {
                String message = "Duplicate public attribute " + attributeName
                        + " in class " + name;
                throw new DuplicateClassAttributeException(message);
            }
            AllocatedClassAttribute allocatedAttribute = new AllocatedClassAttribute(attribute, nextFreeAssignmentOffset);
            attributeMap.put(attributeName, allocatedAttribute);
            nextFreeAssignmentOffset += allocatedAttribute.getSize();
        }
        return attributeMap;
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        return (this.equals(other))
                || (parent != null && parent.isSubtypeOf(other));
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JavaClass)
                && ((JavaClass) obj).name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    public int getHeapSize() {
        return nextFreeAssignmentOffset;
    }

    /**
     * Reports whether this class, or one of its superclasses, has an attribute
     * with the requested name.
     *
     * @param attributeName The name of the attribute to look up
     * @return true if the attribute exists; false otherwise
     */
    private boolean hasPublicAttribute(String attributeName) {
        return (attributesMap.containsKey(attributeName)
                && attributesMap.get(attributeName).getAccessModifier() == AccessModifier.PUBLIC)
                || (parent != null && parent.hasPublicAttribute(attributeName));
    }

    /**
     * Reports the memory offset that was assigned to a given attribute.
     *
     * @param attributeName The name of the attribute to look up
     * @param mustBePublic Whether the attribute is being accessed externally
     * @throws InvalidAttributeException if there is no such attribute
     * @throws IllegalPrivateAccessException if the attribute is private but
     *                                       accessed externally
     * @return The class attribute that was found
     */
    public AllocatedClassAttribute lookupAttribute(String attributeName, boolean mustBePublic)
            throws InvalidAttributeException, IllegalPrivateAccessException {
        AllocatedClassAttribute attribute = null;
        JavaClass currentClass = this;
        while (attribute == null && currentClass != null) {
            if (currentClass.attributesMap.containsKey(attributeName))
                attribute = currentClass.attributesMap.get(attributeName);
            else
                currentClass = currentClass.parent;
        }
        if (attribute == null) {
            String message = "Class " + name + " has no attribute " + attributeName;
            throw new InvalidAttributeException(message);
        }
        if (mustBePublic && attribute.getAccessModifier() != AccessModifier.PUBLIC) {
            String message = "Illegal access to non-public attribute " + attributeName;
            throw new IllegalPrivateAccessException(message);
        }
        return attribute;
    }

    /**
     * Replaces all UnvalidatedJavaClassReference attributes with JavaClass
     * references.
     *
     * This will report an error and terminate the compiler if any of the
     * attributes reference a type that does not actually exist.
     *
     * @param classTable The class table that has been constructed
     */
    public void validateAllClassReferences(ClassTable classTable) {
        for (String attributeName : attributesMap.keySet()) {
            ClassAttribute attribute = attributesMap.get(attributeName);
            if (attribute.type instanceof UnvalidatedJavaClassReference) {
                String className = ((UnvalidatedJavaClassReference) attribute.type).getClassName();
                JavaClass validatedClass = null;
                try {
                    validatedClass = classTable.lookupClass(className);
                } catch (UnknownClassException e) {
                    ParserUtil.reportError(e.getMessage());
                }
                attribute.type = validatedClass;
            }
        }
    }

    /**
     * Adds a new method to the virtual table for this class.
     *
     * @param functionTableEntry The function table entry to register as a
     *                           method of this class
     */
    public void registerNewMethod(FunctionTableEntry functionTableEntry) {

        // Extract information about the method
        String methodName = functionTableEntry.getFunctionName();
        List<Type> parameterTypes = functionTableEntry.getParameterTypes();

        // Look up the trie for this class, creating it if necessary
        if (!vtableIndexLookupTrieMap.containsKey(methodName))
            vtableIndexLookupTrieMap.put(methodName, new LookupTrie<>());
        LookupTrie<Integer, Type> vtableIndexLookupTrie = vtableIndexLookupTrieMap.get(methodName);

        // Determine whether this new method is actually an override of a
        // method defined in a parent class. If it is, then we don't want to
        // use a new virtual table entry: rather, we want to override the
        // entry used in the parent's vtable.
        if (parent != null) {
            Integer vtableIndex = parent.getVirtualTableIndex(methodName, parameterTypes);
            if (vtableIndex != null) {
                // The parent DOES have a mapping for this signature - in other
                // words, this new method is an override.
                vtableIndexLookupTrie.insert(parameterTypes, vtableIndex);
                return;
            }
        }

        // If execution reaches this stage then this must be a new method,
        // so insert it into both the lookup trie and the virtual table.
        vtableIndexLookupTrie.insert(parameterTypes, virtualTable.size());
        virtualTable.add(functionTableEntry);
    }

    /**
     * Returns the list of function table indices that forms the virtual table
     * for this class.
     *
     * @return The list of function table indices for the methods in this class
     */
    public List<Integer> getVirtualTable() {
        return virtualTable
                .stream()
                .map(FunctionTableEntry::getIndex)
                .collect(Collectors.toList());
    }

    /**
     * Returns the return type of the method at the requested position in the
     * virtual table.
     *
     * @param index The index of the virtual table to look up
     * @return The return type of that method, or null if there is no such
     *      method
     */
    public Type getReturnTypeOfMethodAtIndex(int index) {
        if (index < virtualTable.size())
            return virtualTable.get(index).getReturnType();
        else
            return null;
    }

    /**
     * Looks up the virtual table index of a given method.
     *
     * @param name The name of the method
     * @param parameterTypes The types of the parameters of the method
     * @return The virtual table index of the method, or null if the method
     *      does not exist
     */
    public Integer getVirtualTableIndex(String name, List<Type> parameterTypes) {
        LookupTrie<Integer, Type> functionLookupTrie =
                vtableIndexLookupTrieMap.getOrDefault(name, null);
        if (functionLookupTrie == null)
            return null;
        Integer vtableIndex = functionLookupTrie.lookup(parameterTypes);
        if (vtableIndex == null && parent != null)
            return parent.getVirtualTableIndex(name, parameterTypes);
        return vtableIndex;
    }

    /**
     * Wraps information about a given attribute.
     */
    public static class ClassAttribute {

        private String name;
        private Type type;
        private AccessModifier accessModifier;

        public ClassAttribute(String name, Type type, AccessModifier accessModifier) {
            this.name = name;
            this.type = type;
            this.accessModifier = accessModifier;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }

        public AccessModifier getAccessModifier() {
            return accessModifier;
        }

        public int getSize() {
            return type.getSize();
        }
    }

    /**
     * Adds memory positioning information to a class attribute.
     */
    public static class AllocatedClassAttribute extends ClassAttribute {

        /**
         * The offset in memory relative to the start of the object that has
         * been allocated to this specific attribute
         */
        private int memoryOffset;

        public AllocatedClassAttribute(ClassAttribute classAttribute, int memoryOffset) {
            super(classAttribute.getName(), classAttribute.getType(), classAttribute.getAccessModifier());
            this.memoryOffset = memoryOffset;
        }

        public int getMemoryOffset() {
            return memoryOffset;
        }
    }
}
