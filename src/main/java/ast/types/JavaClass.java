package ast.types;

import codegen.Constants;
import errors.DuplicateClassAttributeException;
import errors.DuplicateFunctionSignatureException;
import errors.IllegalPrivateAccessException;
import errors.InvalidAttributeException;
import util.ClassTable;
import util.FunctionTableEntry;
import util.LookupTree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a known Java class.
 */
public class JavaClass extends HeapObjectReference {

    /**
     * The name of the class
     */
    private String name;

    /**
     * A list of all attributes defined in this class only (not including any
     * that are defined in parent classes), in the same order as will be used
     * in the heap.
     */
    private List<AllocatedClassAttribute> allocatedAttributes;

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
    private Map<String, LookupTree<Integer, Type>> vtableIndexLookupTreeMap;

    /**
     * A lookup tree for the class constructor
     */
    private LookupTree<FunctionTableEntry, Type> constructorLookupTree;

    public JavaClass(String name, List<ClassAttribute> attributes, JavaClass parent)
            throws DuplicateClassAttributeException {
        this.name = name;
        this.parent = parent;
        vtableIndexLookupTreeMap = new HashMap<>();
        virtualTable = new ArrayList<>();
        constructorLookupTree = new LookupTree<>();
        if (parent == null) {
            nextFreeAssignmentOffset = 0;
            allocatedAttributes = new ArrayList<>();
        } else {
            nextFreeAssignmentOffset = parent.nextFreeAssignmentOffset;
            virtualTable.addAll(parent.virtualTable);
            allocatedAttributes = parent.allocatedAttributes;
        }

        // Allocate all the attributes defined in this class
        allocatedAttributes = new ArrayList<>();
        attributesMap = new HashMap<>();
        for (ClassAttribute attribute : attributes) {
            String attributeName = attribute.getName();

            // Make sure that the attribute name is unique
            if (attributesMap.containsKey(attributeName)) {
                String message = "Duplicate public attribute " + attributeName
                        + " in class " + name;
                throw new DuplicateClassAttributeException(message);
            } else if (parent != null && parent.hasPublicAttribute(attributeName)) {
                // You can't redeclare a public attribute that has already been
                // declared in a parent class.
                String message = "Attribute " + attributeName
                        + " in class " + name
                        + " has already been defined in a parent class.";
                throw new DuplicateClassAttributeException(message);
            }

            // We are safe to allocate the attribute
            AllocatedClassAttribute allocatedAttribute =
                    new AllocatedClassAttribute(attribute, nextFreeAssignmentOffset);
            allocatedAttributes.add(allocatedAttribute);
            attributesMap.put(attributeName, allocatedAttribute);
            nextFreeAssignmentOffset += allocatedAttribute.getSize();
        }
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
        // Heap layout:
        //  flags            (4 bytes)
        //  vtable pointer   (4 bytes)
        //  size field       (4 bytes)
        //  attributes       (variable)
        //  pointer_info     (variable)
        return Constants.OBJECT_HEADER_LENGTH  // Headers
                + nextFreeAssignmentOffset     // Attributes
                + 4 * getEncodedPointersDescription().size();  // Pointer info
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
     * Returns the number of bytes taken up by attributes.
     */
    public int getNumAttributeBytes() {
        return nextFreeAssignmentOffset;
    }

    /**
     * Builds a list recording, for every 4-byte word in the heap
     * representation of this class, whether that word should be interpreted
     * as a pointer.
     *
     * This is used by the garbage collector to determine which attributes to
     * treat as pointers to other objects.
     *
     * @return A list of booleans where the truth value of the nth element
     *         encodes whether the nth 4-byte block should be treated as a
     *         pointer
     */
    public List<Boolean> getIsPointerList() {
        List<Boolean> list = (parent == null)
                ? new ArrayList<>()
                : parent.getIsPointerList();
        for (ClassAttribute attribute : allocatedAttributes) {
            Type attributeType = attribute.getType();
            if (attributeType.getStackSize() > 4) {
                list.add(false);
                list.add(false);
            } else {
                boolean isPointer = attributeType.isPointer();
                list.add(isPointer);
            }
        }
        return list;
    }

    /**
     * Encodes the pointer information as a list of integers, so that whether
     * each 4-byte word is a pointer is encoded using a single bit.
     *
     * @return The encoded version of the pointer information
     */
    public List<Integer> getEncodedPointersDescription() {
        List<Boolean> pointersList = getIsPointerList();
        List<Integer> encoded = new ArrayList<>();
        while (!pointersList.isEmpty()) {
            int value = 0;
            for (int i = 0; i < 32 && !pointersList.isEmpty(); i++) {
                boolean isPointer = pointersList.remove(0);
                int bit = isPointer ? 1 : 0;
                int mask = bit << i;
                value |= mask;
            }
            encoded.add(value);
        }
        return encoded;
    }

    /**
     * Returns the offset at which the pointer information should start being
     * written.
     *
     * This comes after the header and all attributes.
     *
     * @return The offset at which pointer information starts
     */
    public int getPointerInfoStartOffset() {
    return Constants.OBJECT_HEADER_LENGTH + nextFreeAssignmentOffset;
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
    public void validateAllAttributeTypes(ClassTable classTable) {
        for (ClassAttribute attribute : attributesMap.values()) {
            attribute.type = classTable.validateType(attribute.type);
        }
    }

    /**
     * Adds a new method to the virtual table for this class.
     *
     * @param functionTableEntry The function table entry to register as a
     *                           method of this class
     */
    public void registerNewMethod(FunctionTableEntry functionTableEntry)
            throws DuplicateFunctionSignatureException {

        // Extract information about the method
        String methodName = functionTableEntry.getFunctionName();
        List<Type> parameterTypes = functionTableEntry.getParameterTypes();

        // Look up the tree for this class, creating it if necessary
        if (!vtableIndexLookupTreeMap.containsKey(methodName))
            vtableIndexLookupTreeMap.put(methodName, new LookupTree<>());
        LookupTree<Integer, Type> vtableIndexLookupTree = vtableIndexLookupTreeMap.get(methodName);

        // Attempt to find the vtable index from the parent class. If the parent
        // already has an entry for a method with this signature then this must
        // be an override, so we want to update the existing location in the
        // virtual table. Otherwise, this must be a new method, in which case
        // we want to add a new entry to the virtual table.
        Integer vtableIndex = null;
        if (parent != null) {
            vtableIndex = parent.getVirtualTableIndex(methodName, parameterTypes);
        }


        // If we didn't find an existing entry then we need to insert a new
        // entry to the vtable for this class. If we did then we want to
        // override the existing entry instead.
        if (vtableIndex == null) {
            vtableIndex = virtualTable.size();
            virtualTable.add(functionTableEntry);
        } else {
            virtualTable.set(vtableIndex, functionTableEntry);
        }

        // Update the lookup tree so that we can quickly look up this method
        // in the future.
        boolean success = vtableIndexLookupTree.insert(parameterTypes, vtableIndex);
        if (!success) {
            String signature = functionTableEntry.getQualifiedSignature();
            String message = "Multiple declarations for method with signature "
                    + signature;
            throw new DuplicateFunctionSignatureException(message);
        }
    }

    /**
     * Looks up a method with a given signature.
     *
     * @param name The name of the method
     * @param parameterTypes The types of the parameters for that method
     * @return The function table entry for the method
     */
    public FunctionTableEntry lookupMethod(String name, List<Type> parameterTypes) {
        FunctionTableEntry entry = null;
        if (vtableIndexLookupTreeMap.containsKey(name)) {
            LookupTree<Integer, Type> lookupTree = vtableIndexLookupTreeMap.get(name);
            Integer vtableIndex = lookupTree.lookup(parameterTypes);
            if (vtableIndex != null) {
                entry = virtualTable.get(vtableIndex);
            }
        }

        if (entry == null) {
            if (parent != null) {
                return parent.lookupMethod(name, parameterTypes);
            }
        }

        return entry;
    }

    /**
     * Registers a constructor of this class.
     *
     * @param functionTableEntry The entry in the function table
     */
    public void registerNewConstructor(FunctionTableEntry functionTableEntry)
            throws DuplicateFunctionSignatureException {
        List<Type> parameterTypes = functionTableEntry.getParameterTypes();
        boolean success = constructorLookupTree.insert(parameterTypes, functionTableEntry);
        if (!success) {
            String signature = functionTableEntry.getQualifiedSignature();
            String message = "Duplicate constructors in class " + name
                    + " with signature " + signature;
            throw new DuplicateFunctionSignatureException(message);
        }
    }

    /**
     * Looks up the function table entry for the constructor with the specified
     * parameter types.
     *
     * @param parameterTypes The types of the parameters
     * @return The function table entry of the relevant constructor method
     */
    public FunctionTableEntry lookupConstructor(List<Type> parameterTypes) {
        return constructorLookupTree.lookup(parameterTypes);
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
        LookupTree<Integer, Type> functionLookupTree =
                vtableIndexLookupTreeMap.getOrDefault(name, null);
        if (functionLookupTree == null)
            return null;
        Integer vtableIndex = functionLookupTree.lookup(parameterTypes);
        if (vtableIndex == null && parent != null)
            return parent.getVirtualTableIndex(name, parameterTypes);
        return vtableIndex;
    }

    /**
     * Reports whether this class has defined a zero-argument constructor.
     *
     * @return true if a zero-argument constructor has been defined;
     *         false otherwise
     */
    public boolean hasNoArgumentConstructor() {
        List<Type> emptyList = new ArrayList<>();
        return (constructorLookupTree.lookup(emptyList) != null);
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
            return type.getStackSize();
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
