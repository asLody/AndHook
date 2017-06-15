/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *	
 *	from android 4.4.4_r1
 *	@author : rrrfff@foxmail.com
 *  @date : 2017/06/14
 *
 */
#pragma once
#ifndef LOG_TAG
# define LOG_TAG "dalvikvm"
#endif

#include <jni.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <assert.h>
#include <pthread.h>

#if !defined(NDEBUG) && defined(WITH_DALVIK_ASSERT)
# undef assert
# define assert(x) \
    ((x) ? ((void)0) : (ALOGE("ASSERT FAILED (%s:%d): %s", \
        __FILE__, __LINE__, #x), *(int*)39=39, (void)0) )
#endif

#define MIN(x,y) (((x) < (y)) ? (x) : (y))
#define MAX(x,y) (((x) > (y)) ? (x) : (y))

#define ALIGN_UP(x, n) (((size_t)(x) + (n) - 1) & ~((n) - 1))
#define ALIGN_DOWN(x, n) ((size_t)(x) & -(n))
#define ALIGN_UP_TO_PAGE_SIZE(p) ALIGN_UP(p, SYSTEM_PAGE_SIZE)
#define ALIGN_DOWN_TO_PAGE_SIZE(p) ALIGN_DOWN(p, SYSTEM_PAGE_SIZE)

#define CLZ(x) __builtin_clz(x)

/*
 * If "very verbose" logging is enabled, make it equivalent to ALOGV.
 * Otherwise, make it disappear.
 *
 * Define this above the #include "Dalvik.h" to enable for only a
 * single file.
 */
/* #define VERY_VERBOSE_LOG */
#if defined(VERY_VERBOSE_LOG)
# define LOGVV      ALOGV
# define IF_LOGVV() IF_ALOGV()
#else
# define LOGVV(...) ((void)0)
# define IF_LOGVV() if (false)
#endif

#define HAVE_LITTLE_ENDIAN  1

/*
 * These match the definitions in the VM specification.
 */
typedef uint8_t             u1;
typedef uint16_t            u2;
typedef uint32_t            u4;
typedef uint64_t            u8;
typedef int8_t              s1;
typedef int16_t             s2;
typedef int32_t             s4;
typedef int64_t             s8;

/*
 * Storage for primitive types and object references.
 *
 * Some parts of the code (notably object field access) assume that values
 * are "left aligned", i.e. given "JValue jv", "jv.i" and "*((s4*)&jv)"
 * yield the same result.  This seems to be guaranteed by gcc on big- and
 * little-endian systems.
 */
struct Object;

union JValue {
#if defined(HAVE_LITTLE_ENDIAN)
    u1      z;
    s1      b;
    u2      c;
    s2      s;
    s4      i;
    s8      j;
    float   f;
    double  d;
    Object* l;
#endif
#if defined(HAVE_BIG_ENDIAN)
    struct {
        u1    _z[3];
        u1    z;
    };
    struct {
        s1    _b[3];
        s1    b;
    };
    struct {
        u2    _c;
        u2    c;
    };
    struct {
        s2    _s;
        s2    s;
    };
    s4      i;
    s8      j;
    float   f;
    double  d;
    void*   l;
#endif
};

#define OFFSETOF_MEMBER(t, f)         \
  (reinterpret_cast<char*>(           \
     &reinterpret_cast<t*>(16)->f) -  \
   reinterpret_cast<char*>(16))

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

struct ClassObject;
struct Method;
struct ArrayObject;
struct Thread;

/*
 * Native function pointer type.
 *
 * "args[0]" holds the "this" pointer for virtual methods.
 *
 * The "Bridge" form is a super-set of the "Native" form; in many places
 * they are used interchangeably.  Currently, all functions have all
 * arguments passed in, but some functions only care about the first two.
 * Passing extra arguments to a C function is (mostly) harmless.
 */
typedef void (*DalvikBridgeFunc)(const u4* args, JValue* pResult,
    const Method* method, struct Thread* self);
typedef void (*DalvikNativeFunc)(const u4* args, JValue* pResult);


/* vm-internal access flags and related definitions */
enum AccessFlags {
    ACC_MIRANDA         = 0x8000,       // method (internal to VM)
    JAVA_FLAGS_MASK     = 0xffff,       // bits set from Java sources (low 16)
};

/* Use the top 16 bits of the access flags field for
 * other class flags.  Code should use the *CLASS_FLAG*()
 * macros to set/get these flags.
 */
enum ClassFlags {
    CLASS_ISFINALIZABLE        = (1<<31), // class/ancestor overrides finalize()
    CLASS_ISARRAY              = (1<<30), // class is a "[*"
    CLASS_ISOBJECTARRAY        = (1<<29), // class is a "[L*" or "[[*"
    CLASS_ISCLASS              = (1<<28), // class is *the* class Class

    CLASS_ISREFERENCE          = (1<<27), // class is a soft/weak/phantom ref
                                          // only ISREFERENCE is set --> soft
    CLASS_ISWEAKREFERENCE      = (1<<26), // class is a weak reference
    CLASS_ISFINALIZERREFERENCE = (1<<25), // class is a finalizer reference
    CLASS_ISPHANTOMREFERENCE   = (1<<24), // class is a phantom reference

    CLASS_MULTIPLE_DEFS        = (1<<23), // DEX verifier: defs in multiple DEXs

    /* unlike the others, these can be present in the optimized DEX file */
    CLASS_ISOPTIMIZED          = (1<<17), // class may contain opt instrs
    CLASS_ISPREVERIFIED        = (1<<16), // class has been pre-verified
};

/*
 * Get/set class flags.
 */
#define SET_CLASS_FLAG(clazz, flag) \
    do { (clazz)->accessFlags |= (flag); } while (0)

#define CLEAR_CLASS_FLAG(clazz, flag) \
    do { (clazz)->accessFlags &= ~(flag); } while (0)

#define IS_CLASS_FLAG_SET(clazz, flag) \
    (((clazz)->accessFlags & (flag)) != 0)

#define GET_CLASS_FLAG_GROUP(clazz, flags) \
    ((u4)((clazz)->accessFlags & (flags)))

/*
 * Use the top 16 bits of the access flags field for other method flags.
 * Code should use the *METHOD_FLAG*() macros to set/get these flags.
 */
enum MethodFlags {
    METHOD_ISWRITABLE       = (1<<31),  // the method's code is writable
};

/*
 * Get/set method flags.
 */
#define SET_METHOD_FLAG(method, flag) \
    do { (method)->accessFlags |= (flag); } while (0)

#define CLEAR_METHOD_FLAG(method, flag) \
    do { (method)->accessFlags &= ~(flag); } while (0)

#define IS_METHOD_FLAG_SET(method, flag) \
    (((method)->accessFlags & (flag)) != 0)

#define GET_METHOD_FLAG_GROUP(method, flags) \
    ((u4)((method)->accessFlags & (flags)))

/* current state of the class, increasing as we progress */
enum ClassStatus {
    CLASS_ERROR         = -1,

    CLASS_NOTREADY      = 0,
    CLASS_IDX           = 1,    /* loaded, DEX idx in super or ifaces */
    CLASS_LOADED        = 2,    /* DEX idx values resolved */
    CLASS_RESOLVED      = 3,    /* part of linking */
    CLASS_VERIFYING     = 4,    /* in the process of being verified */
    CLASS_VERIFIED      = 5,    /* logically part of linking; done pre-init */
    CLASS_INITIALIZING  = 6,    /* class init in progress */
    CLASS_INITIALIZED   = 7,    /* ready to go */
};

/*
 * Definitions for packing refOffsets in ClassObject.
 */
/*
 * A magic value for refOffsets. Ignore the bits and walk the super
 * chain when this is the value.
 * [This is an unlikely "natural" value, since it would be 30 non-ref instance
 * fields followed by 2 ref instance fields.]
 */
#define CLASS_WALK_SUPER ((unsigned int)(3))
#define CLASS_SMALLEST_OFFSET (sizeof(struct Object))
#define CLASS_BITS_PER_WORD (sizeof(unsigned long int) * 8)
#define CLASS_OFFSET_ALIGNMENT 4
#define CLASS_HIGH_BIT ((unsigned int)1 << (CLASS_BITS_PER_WORD - 1))
/*
 * Given an offset, return the bit number which would encode that offset.
 * Local use only.
 */
#define _CLASS_BIT_NUMBER_FROM_OFFSET(byteOffset) \
    (((unsigned int)(byteOffset) - CLASS_SMALLEST_OFFSET) / \
     CLASS_OFFSET_ALIGNMENT)
/*
 * Is the given offset too large to be encoded?
 */
#define CLASS_CAN_ENCODE_OFFSET(byteOffset) \
    (_CLASS_BIT_NUMBER_FROM_OFFSET(byteOffset) < CLASS_BITS_PER_WORD)
/*
 * Return a single bit, encoding the offset.
 * Undefined if the offset is too large, as defined above.
 */
#define CLASS_BIT_FROM_OFFSET(byteOffset) \
    (CLASS_HIGH_BIT >> _CLASS_BIT_NUMBER_FROM_OFFSET(byteOffset))
/*
 * Return an offset, given a bit number as returned from CLZ.
 */
#define CLASS_OFFSET_FROM_CLZ(rshift) \
    (((int)(rshift) * CLASS_OFFSET_ALIGNMENT) + CLASS_SMALLEST_OFFSET)


/*
 * Used for iftable in ClassObject.
 */
struct InterfaceEntry {
    /* pointer to interface class */
    ClassObject*    clazz;

    /*
     * Index into array of vtable offsets.  This points into the ifviPool,
     * which holds the vtables for all interfaces declared by this class.
     */
    int*            methodIndexArray;
};



/*
 * There are three types of objects:
 *  Class objects - an instance of java.lang.Class
 *  Array objects - an object created with a "new array" instruction
 *  Data objects - an object that is neither of the above
 *
 * We also define String objects.  At present they're equivalent to
 * DataObject, but that may change.  (Either way, they make some of the
 * code more obvious.)
 *
 * All objects have an Object header followed by type-specific data.
 */
struct Object {
    /* ptr to class object */
    ClassObject*    clazz;

    /*
     * A word containing either a "thin" lock or a "fat" monitor.  See
     * the comments in Sync.c for a description of its layout.
     */
    u4              lock;
};

/*
 * Properly initialize an Object.
 * void DVM_OBJECT_INIT(Object *obj, ClassObject *clazz_)
 */
#define DVM_OBJECT_INIT(obj, clazz_) \
    dvmSetFieldObject(obj, OFFSETOF_MEMBER(Object, clazz), clazz_)

/*
 * Data objects have an Object header followed by their instance data.
 */
struct DataObject : Object {
    /* variable #of u4 slots; u8 uses 2 slots */
    u4              instanceData[1];
};

/*
 * Strings are used frequently enough that we may want to give them their
 * own unique type.
 *
 * Using a dedicated type object to access the instance data provides a
 * performance advantage but makes the java/lang/String.java implementation
 * fragile.
 *
 * Currently this is just equal to DataObject, and we pull the fields out
 * like we do for any other object.
 */
struct StringObject : Object {
    /* variable #of u4 slots; u8 uses 2 slots */
    u4              instanceData[1];

    /** Returns this string's length in characters. */
    int length() const;

    /**
     * Returns this string's length in bytes when encoded as modified UTF-8.
     * Does not include a terminating NUL byte.
     */
    int utfLength() const;

    /** Returns this string's char[] as an ArrayObject. */
    ArrayObject* array() const;

    /** Returns this string's char[] as a u2*. */
    const u2* chars() const;
};


/*
 * Array objects have these additional fields.
 *
 * We don't currently store the size of each element.  Usually it's implied
 * by the instruction.  If necessary, the width can be derived from
 * the first char of obj->clazz->descriptor.
 */
struct ArrayObject : Object {
    /* number of elements; immutable after init */
    u4              length;

    /*
     * Array contents; actual size is (length * sizeof(type)).  This is
     * declared as u8 so that the compiler inserts any necessary padding
     * (e.g. for EABI); the actual allocation may be smaller than 8 bytes.
     */
    u8              contents[1];
};

/*
 * For classes created early and thus probably in the zygote, the
 * InitiatingLoaderList is kept in gDvm. Later classes use the structure in
 * Object Class. This helps keep zygote pages shared.
 */
struct InitiatingLoaderList {
    /* a list of initiating loader Objects; grown and initialized on demand */
    Object**  initiatingLoaders;
    /* count of loaders in the above list */
    int       initiatingLoaderCount;
};

/*
 * Generic field header.  We pass this around when we want a generic Field
 * pointer (e.g. for reflection stuff).  Testing the accessFlags for
 * ACC_STATIC allows a proper up-cast.
 */
struct Field {
    ClassObject*    clazz;          /* class in which the field is declared */
    const char*     name;
    const char*     signature;      /* e.g. "I", "[C", "Landroid/os/Debug;" */
    u4              accessFlags;
};

u4 dvmGetFieldIdx(const Field* field);

/*
 * Static field.
 */
struct StaticField : Field {
    JValue          value;          /* initially set from DEX for primitives */
};

/*
 * Instance field.
 */
struct InstField : Field {
    /*
     * This field indicates the byte offset from the beginning of the
     * (Object *) to the actual instance data; e.g., byteOffset==0 is
     * the same as the object pointer (bug!), and byteOffset==4 is 4
     * bytes farther.
     */
    int             byteOffset;
};

/*
 * This defines the amount of space we leave for field slots in the
 * java.lang.Class definition.  If we alter the class to have more than
 * this many fields, the VM will abort at startup.
 */
#define CLASS_FIELD_SLOTS   4


/*
 * gcc-style inline management -- ensures we have a copy of all functions
 * in the library, so code that links against us will work whether or not
 * it was built with optimizations enabled.
 */
#ifndef _DEX_GEN_INLINES             /* only defined by DexInlines.c */
# define DEX_INLINE extern __inline__
#else
# define DEX_INLINE
#endif

/* DEX file magic number */
#define DEX_MAGIC       "dex\n"

/* current version, encoded in 4 bytes of ASCII */
#define DEX_MAGIC_VERS  "036\0"

/*
 * older but still-recognized version (corresponding to Android API
 * levels 13 and earlier
 */
#define DEX_MAGIC_VERS_API_13  "035\0"

/* same, but for optimized DEX header */
#define DEX_OPT_MAGIC   "dey\n"
#define DEX_OPT_MAGIC_VERS  "036\0"

#define DEX_DEP_MAGIC   "deps"

/*
 * 160-bit SHA-1 digest.
 */
enum { kSHA1DigestLen = 20,
       kSHA1DigestOutputLen = kSHA1DigestLen*2 +1 };

/* general constants */
enum {
    kDexEndianConstant = 0x12345678,    /* the endianness indicator */
    kDexNoIndex = 0xffffffff,           /* not a valid index value */
};

/*
 * Enumeration of all the primitive types.
 */
enum PrimitiveType {
    PRIM_NOT        = 0,       /* value is a reference type, not a primitive type */
    PRIM_VOID       = 1,
    PRIM_BOOLEAN    = 2,
    PRIM_BYTE       = 3,
    PRIM_SHORT      = 4,
    PRIM_CHAR       = 5,
    PRIM_INT        = 6,
    PRIM_LONG       = 7,
    PRIM_FLOAT      = 8,
    PRIM_DOUBLE     = 9,
};

/*
 * access flags and masks; the "standard" ones are all <= 0x4000
 *
 * Note: There are related declarations in vm/oo/Object.h in the ClassFlags
 * enum.
 */
enum {
    ACC_PUBLIC       = 0x00000001,       // class, field, method, ic
    ACC_PRIVATE      = 0x00000002,       // field, method, ic
    ACC_PROTECTED    = 0x00000004,       // field, method, ic
    ACC_STATIC       = 0x00000008,       // field, method, ic
    ACC_FINAL        = 0x00000010,       // class, field, method, ic
    ACC_SYNCHRONIZED = 0x00000020,       // method (only allowed on natives)
    ACC_SUPER        = 0x00000020,       // class (not used in Dalvik)
    ACC_VOLATILE     = 0x00000040,       // field
    ACC_BRIDGE       = 0x00000040,       // method (1.5)
    ACC_TRANSIENT    = 0x00000080,       // field
    ACC_VARARGS      = 0x00000080,       // method (1.5)
    ACC_NATIVE       = 0x00000100,       // method
    ACC_INTERFACE    = 0x00000200,       // class, ic
    ACC_ABSTRACT     = 0x00000400,       // class, method, ic
    ACC_STRICT       = 0x00000800,       // method
    ACC_SYNTHETIC    = 0x00001000,       // field, method, ic
    ACC_ANNOTATION   = 0x00002000,       // class, ic (1.5)
    ACC_ENUM         = 0x00004000,       // class, field, ic (1.5)
    ACC_CONSTRUCTOR  = 0x00010000,       // method (Dalvik only)
    ACC_DECLARED_SYNCHRONIZED =
                       0x00020000,       // method (Dalvik only)
    ACC_CLASS_MASK =
        (ACC_PUBLIC | ACC_FINAL | ACC_INTERFACE | ACC_ABSTRACT
                | ACC_SYNTHETIC | ACC_ANNOTATION | ACC_ENUM),
    ACC_INNER_CLASS_MASK =
        (ACC_CLASS_MASK | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC),
    ACC_FIELD_MASK =
        (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC | ACC_FINAL
                | ACC_VOLATILE | ACC_TRANSIENT | ACC_SYNTHETIC | ACC_ENUM),
    ACC_METHOD_MASK =
        (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC | ACC_FINAL
                | ACC_SYNCHRONIZED | ACC_BRIDGE | ACC_VARARGS | ACC_NATIVE
                | ACC_ABSTRACT | ACC_STRICT | ACC_SYNTHETIC | ACC_CONSTRUCTOR
                | ACC_DECLARED_SYNCHRONIZED),
};

/* annotation constants */
enum {
    kDexVisibilityBuild         = 0x00,     /* annotation visibility */
    kDexVisibilityRuntime       = 0x01,
    kDexVisibilitySystem        = 0x02,

    kDexAnnotationByte          = 0x00,
    kDexAnnotationShort         = 0x02,
    kDexAnnotationChar          = 0x03,
    kDexAnnotationInt           = 0x04,
    kDexAnnotationLong          = 0x06,
    kDexAnnotationFloat         = 0x10,
    kDexAnnotationDouble        = 0x11,
    kDexAnnotationString        = 0x17,
    kDexAnnotationType          = 0x18,
    kDexAnnotationField         = 0x19,
    kDexAnnotationMethod        = 0x1a,
    kDexAnnotationEnum          = 0x1b,
    kDexAnnotationArray         = 0x1c,
    kDexAnnotationAnnotation    = 0x1d,
    kDexAnnotationNull          = 0x1e,
    kDexAnnotationBoolean       = 0x1f,

    kDexAnnotationValueTypeMask = 0x1f,     /* low 5 bits */
    kDexAnnotationValueArgShift = 5,
};

/* map item type codes */
enum {
    kDexTypeHeaderItem               = 0x0000,
    kDexTypeStringIdItem             = 0x0001,
    kDexTypeTypeIdItem               = 0x0002,
    kDexTypeProtoIdItem              = 0x0003,
    kDexTypeFieldIdItem              = 0x0004,
    kDexTypeMethodIdItem             = 0x0005,
    kDexTypeClassDefItem             = 0x0006,
    kDexTypeMapList                  = 0x1000,
    kDexTypeTypeList                 = 0x1001,
    kDexTypeAnnotationSetRefList     = 0x1002,
    kDexTypeAnnotationSetItem        = 0x1003,
    kDexTypeClassDataItem            = 0x2000,
    kDexTypeCodeItem                 = 0x2001,
    kDexTypeStringDataItem           = 0x2002,
    kDexTypeDebugInfoItem            = 0x2003,
    kDexTypeAnnotationItem           = 0x2004,
    kDexTypeEncodedArrayItem         = 0x2005,
    kDexTypeAnnotationsDirectoryItem = 0x2006,
};

/* auxillary data section chunk codes */
enum {
    kDexChunkClassLookup            = 0x434c4b50,   /* CLKP */
    kDexChunkRegisterMaps           = 0x524d4150,   /* RMAP */

    kDexChunkEnd                    = 0x41454e44,   /* AEND */
};

/* debug info opcodes and constants */
enum {
    DBG_END_SEQUENCE         = 0x00,
    DBG_ADVANCE_PC           = 0x01,
    DBG_ADVANCE_LINE         = 0x02,
    DBG_START_LOCAL          = 0x03,
    DBG_START_LOCAL_EXTENDED = 0x04,
    DBG_END_LOCAL            = 0x05,
    DBG_RESTART_LOCAL        = 0x06,
    DBG_SET_PROLOGUE_END     = 0x07,
    DBG_SET_EPILOGUE_BEGIN   = 0x08,
    DBG_SET_FILE             = 0x09,
    DBG_FIRST_SPECIAL        = 0x0a,
    DBG_LINE_BASE            = -4,
    DBG_LINE_RANGE           = 15,
};

/*
 * Direct-mapped "header_item" struct.
 */
struct DexHeader {
    u1  magic[8];           /* includes version number */
    u4  checksum;           /* adler32 checksum */
    u1  signature[kSHA1DigestLen]; /* SHA-1 hash */
    u4  fileSize;           /* length of entire file */
    u4  headerSize;         /* offset to start of next section */
    u4  endianTag;
    u4  linkSize;
    u4  linkOff;
    u4  mapOff;
    u4  stringIdsSize;
    u4  stringIdsOff;
    u4  typeIdsSize;
    u4  typeIdsOff;
    u4  protoIdsSize;
    u4  protoIdsOff;
    u4  fieldIdsSize;
    u4  fieldIdsOff;
    u4  methodIdsSize;
    u4  methodIdsOff;
    u4  classDefsSize;
    u4  classDefsOff;
    u4  dataSize;
    u4  dataOff;
};

/*
 * Direct-mapped "map_item".
 */
struct DexMapItem {
    u2 type;              /* type code (see kDexType* above) */
    u2 unused;
    u4 size;              /* count of items of the indicated type */
    u4 offset;            /* file offset to the start of data */
};

/*
 * Direct-mapped "map_list".
 */
struct DexMapList {
    u4  size;               /* #of entries in list */
    DexMapItem list[1];     /* entries */
};

/*
 * Direct-mapped "string_id_item".
 */
struct DexStringId {
    u4 stringDataOff;      /* file offset to string_data_item */
};

/*
 * Direct-mapped "type_id_item".
 */
struct DexTypeId {
    u4  descriptorIdx;      /* index into stringIds list for type descriptor */
};

/*
 * Direct-mapped "field_id_item".
 */
struct DexFieldId {
    u2  classIdx;           /* index into typeIds list for defining class */
    u2  typeIdx;            /* index into typeIds for field type */
    u4  nameIdx;            /* index into stringIds for field name */
};

/*
 * Direct-mapped "method_id_item".
 */
struct DexMethodId {
    u2  classIdx;           /* index into typeIds list for defining class */
    u2  protoIdx;           /* index into protoIds for method prototype */
    u4  nameIdx;            /* index into stringIds for method name */
};

/*
 * Direct-mapped "proto_id_item".
 */
struct DexProtoId {
    u4  shortyIdx;          /* index into stringIds for shorty descriptor */
    u4  returnTypeIdx;      /* index into typeIds list for return type */
    u4  parametersOff;      /* file offset to type_list for parameter types */
};

/*
 * Direct-mapped "class_def_item".
 */
struct DexClassDef {
    u4  classIdx;           /* index into typeIds for this class */
    u4  accessFlags;
    u4  superclassIdx;      /* index into typeIds for superclass */
    u4  interfacesOff;      /* file offset to DexTypeList */
    u4  sourceFileIdx;      /* index into stringIds for source file name */
    u4  annotationsOff;     /* file offset to annotations_directory_item */
    u4  classDataOff;       /* file offset to class_data_item */
    u4  staticValuesOff;    /* file offset to DexEncodedArray */
};

/*
 * Direct-mapped "type_item".
 */
struct DexTypeItem {
    u2  typeIdx;            /* index into typeIds */
};

/*
 * Direct-mapped "type_list".
 */
struct DexTypeList {
    u4  size;               /* #of entries in list */
    DexTypeItem list[1];    /* entries */
};

/*
 * Direct-mapped "code_item".
 *
 * The "catches" table is used when throwing an exception,
 * "debugInfo" is used when displaying an exception stack trace or
 * debugging. An offset of zero indicates that there are no entries.
 */
struct DexCode {
    u2  registersSize;
    u2  insSize;
    u2  outsSize;
    u2  triesSize;
    u4  debugInfoOff;       /* file offset to debug info stream */
    u4  insnsSize;          /* size of the insns array, in u2 units */
    u2  insns[1];
    /* followed by optional u2 padding */
    /* followed by try_item[triesSize] */
    /* followed by uleb128 handlersSize */
    /* followed by catch_handler_item[handlersSize] */
};

/*
 * Direct-mapped "try_item".
 */
struct DexTry {
    u4  startAddr;          /* start address, in 16-bit code units */
    u2  insnCount;          /* instruction count, in 16-bit code units */
    u2  handlerOff;         /* offset in encoded handler data to handlers */
};

/*
 * Link table.  Currently undefined.
 */
struct DexLink {
    u1  bleargh;
};


/*
 * Direct-mapped "annotations_directory_item".
 */
struct DexAnnotationsDirectoryItem {
    u4  classAnnotationsOff;  /* offset to DexAnnotationSetItem */
    u4  fieldsSize;           /* count of DexFieldAnnotationsItem */
    u4  methodsSize;          /* count of DexMethodAnnotationsItem */
    u4  parametersSize;       /* count of DexParameterAnnotationsItem */
    /* followed by DexFieldAnnotationsItem[fieldsSize] */
    /* followed by DexMethodAnnotationsItem[methodsSize] */
    /* followed by DexParameterAnnotationsItem[parametersSize] */
};

/*
 * Direct-mapped "field_annotations_item".
 */
struct DexFieldAnnotationsItem {
    u4  fieldIdx;
    u4  annotationsOff;             /* offset to DexAnnotationSetItem */
};

/*
 * Direct-mapped "method_annotations_item".
 */
struct DexMethodAnnotationsItem {
    u4  methodIdx;
    u4  annotationsOff;             /* offset to DexAnnotationSetItem */
};

/*
 * Direct-mapped "parameter_annotations_item".
 */
struct DexParameterAnnotationsItem {
    u4  methodIdx;
    u4  annotationsOff;             /* offset to DexAnotationSetRefList */
};

/*
 * Direct-mapped "annotation_set_ref_item".
 */
struct DexAnnotationSetRefItem {
    u4  annotationsOff;             /* offset to DexAnnotationSetItem */
};

/*
 * Direct-mapped "annotation_set_ref_list".
 */
struct DexAnnotationSetRefList {
    u4  size;
    DexAnnotationSetRefItem list[1];
};

/*
 * Direct-mapped "annotation_set_item".
 */
struct DexAnnotationSetItem {
    u4  size;
    u4  entries[1];                 /* offset to DexAnnotationItem */
};

/*
 * Direct-mapped "annotation_item".
 *
 * NOTE: this structure is byte-aligned.
 */
struct DexAnnotationItem {
    u1  visibility;
    u1  annotation[1];              /* data in encoded_annotation format */
};

/*
 * Direct-mapped "encoded_array".
 *
 * NOTE: this structure is byte-aligned.
 */
struct DexEncodedArray {
    u1  array[1];                   /* data in encoded_array format */
};

/*
 * Lookup table for classes.  It provides a mapping from class name to
 * class definition.  Used by dexFindClass().
 *
 * We calculate this at DEX optimization time and embed it in the file so we
 * don't need the same hash table in every VM.  This is slightly slower than
 * a hash table with direct pointers to the items, but because it's shared
 * there's less of a penalty for using a fairly sparse table.
 */
struct DexClassLookup {
    int     size;                       // total size, including "size"
    int     numEntries;                 // size of table[]; always power of 2
    struct {
        u4      classDescriptorHash;    // class descriptor hash code
        int     classDescriptorOffset;  // in bytes, from start of DEX
        int     classDefOffset;         // in bytes, from start of DEX
    } table[1];
};

/*
 * Header added by DEX optimization pass.  Values are always written in
 * local byte and structure padding.  The first field (magic + version)
 * is guaranteed to be present and directly readable for all expected
 * compiler configurations; the rest is version-dependent.
 *
 * Try to keep this simple and fixed-size.
 */
struct DexOptHeader {
    u1  magic[8];           /* includes version number */

    u4  dexOffset;          /* file offset of DEX header */
    u4  dexLength;
    u4  depsOffset;         /* offset of optimized DEX dependency table */
    u4  depsLength;
    u4  optOffset;          /* file offset of optimized data tables */
    u4  optLength;

    u4  flags;              /* some info flags */
    u4  checksum;           /* adler32 checksum covering deps/opt */

    /* pad for 64-bit alignment if necessary */
};

#define DEX_OPT_FLAG_BIG            (1<<1)  /* swapped to big-endian */

#define DEX_INTERFACE_CACHE_SIZE    128     /* must be power of 2 */

/*
 * Structure representing a DEX file.
 *
 * Code should regard DexFile as opaque, using the API calls provided here
 * to access specific structures.
 */
struct DexFile {
    /* directly-mapped "opt" header */
    const DexOptHeader* pOptHeader;

    /* pointers to directly-mapped structs and arrays in base DEX */
    const DexHeader*    pHeader;
    const DexStringId*  pStringIds;
    const DexTypeId*    pTypeIds;
    const DexFieldId*   pFieldIds;
    const DexMethodId*  pMethodIds;
    const DexProtoId*   pProtoIds;
    const DexClassDef*  pClassDefs;
    const DexLink*      pLinkData;

    /*
     * These are mapped out of the "auxillary" section, and may not be
     * included in the file.
     */
    const DexClassLookup* pClassLookup;
    const void*         pRegisterMapPool;       // RegisterMapClassPool

    /* points to start of DEX file data */
    const u1*           baseAddr;

    /* track memory overhead for auxillary structures */
    int                 overhead;

    /* additional app-specific data structures associated with the DEX */
    //void*               auxData;
};

/*
 * Utility function -- rounds up to the nearest power of 2.
 */
u4 dexRoundUpPower2(u4 val);

/*
 * Parse an optimized or unoptimized .dex file sitting in memory.
 *
 * On success, return a newly-allocated DexFile.
 */
DexFile* dexFileParse(const u1* data, size_t length, int flags);

/* bit values for "flags" argument to dexFileParse */
enum {
    kDexParseDefault            = 0,
    kDexParseVerifyChecksum     = 1,
    kDexParseContinueOnError    = (1 << 1),
};

/*
 * Fix the byte ordering of all fields in the DEX file, and do
 * structural verification. This is only required for code that opens
 * "raw" DEX files, such as the DEX optimizer.
 *
 * Return 0 on success.
 */
int dexSwapAndVerify(u1* addr, int len);

/*
 * Detect the file type of the given memory buffer via magic number.
 * Call dexSwapAndVerify() on an unoptimized DEX file, do nothing
 * but return successfully on an optimized DEX file, and report an
 * error for all other cases.
 *
 * Return 0 on success.
 */
int dexSwapAndVerifyIfNecessary(u1* addr, int len);

/*
 * Check to see if the file magic and format version in the given
 * header are recognized as valid. Returns true if they are
 * acceptable.
 */
bool dexHasValidMagic(const DexHeader* pHeader);

/*
 * Compute DEX checksum.
 */
u4 dexComputeChecksum(const DexHeader* pHeader);

/*
 * Free a DexFile structure, along with any associated structures.
 */
void dexFileFree(DexFile* pDexFile);

/*
 * Create class lookup table.
 */
DexClassLookup* dexCreateClassLookup(DexFile* pDexFile);

/*
 * Find a class definition by descriptor.
 */
const DexClassDef* dexFindClass(const DexFile* pFile, const char* descriptor);

/*
 * Set up the basic raw data pointers of a DexFile. This function isn't
 * meant for general use.
 */
void dexFileSetupBasicPointers(DexFile* pDexFile, const u1* data);

/* return the DexMapList of the file, if any */
DEX_INLINE const DexMapList* dexGetMap(const DexFile* pDexFile) {
    u4 mapOff = pDexFile->pHeader->mapOff;

    if (mapOff == 0) {
        return NULL;
    } else {
        return (const DexMapList*) (pDexFile->baseAddr + mapOff);
    }
}

/* return the const char* string data referred to by the given string_id */
DEX_INLINE const char* dexGetStringData(const DexFile* pDexFile,
        const DexStringId* pStringId) {
    const u1* ptr = pDexFile->baseAddr + pStringId->stringDataOff;

    // Skip the uleb128 length.
    while (*(ptr++) > 0x7f) /* empty */ ;

    return (const char*) ptr;
}
/* return the StringId with the specified index */
DEX_INLINE const DexStringId* dexGetStringId(const DexFile* pDexFile, u4 idx) {
    assert(idx < pDexFile->pHeader->stringIdsSize);
    return &pDexFile->pStringIds[idx];
}
/* return the UTF-8 encoded string with the specified string_id index */
DEX_INLINE const char* dexStringById(const DexFile* pDexFile, u4 idx) {
    const DexStringId* pStringId = dexGetStringId(pDexFile, idx);
    return dexGetStringData(pDexFile, pStringId);
}

/* Return the UTF-8 encoded string with the specified string_id index,
 * also filling in the UTF-16 size (number of 16-bit code points).*/
const char* dexStringAndSizeById(const DexFile* pDexFile, u4 idx,
        u4* utf16Size);

/* return the TypeId with the specified index */
DEX_INLINE const DexTypeId* dexGetTypeId(const DexFile* pDexFile, u4 idx) {
    assert(idx < pDexFile->pHeader->typeIdsSize);
    return &pDexFile->pTypeIds[idx];
}

/*
 * Get the descriptor string associated with a given type index.
 * The caller should not free() the returned string.
 */
DEX_INLINE const char* dexStringByTypeIdx(const DexFile* pDexFile, u4 idx) {
    const DexTypeId* typeId = dexGetTypeId(pDexFile, idx);
    return dexStringById(pDexFile, typeId->descriptorIdx);
}

/* return the MethodId with the specified index */
DEX_INLINE const DexMethodId* dexGetMethodId(const DexFile* pDexFile, u4 idx) {
    assert(idx < pDexFile->pHeader->methodIdsSize);
    return &pDexFile->pMethodIds[idx];
}

/* return the FieldId with the specified index */
DEX_INLINE const DexFieldId* dexGetFieldId(const DexFile* pDexFile, u4 idx) {
    assert(idx < pDexFile->pHeader->fieldIdsSize);
    return &pDexFile->pFieldIds[idx];
}

/* return the ProtoId with the specified index */
DEX_INLINE const DexProtoId* dexGetProtoId(const DexFile* pDexFile, u4 idx) {
    assert(idx < pDexFile->pHeader->protoIdsSize);
    return &pDexFile->pProtoIds[idx];
}

/*
 * Get the parameter list from a ProtoId. The returns NULL if the ProtoId
 * does not have a parameter list.
 */
DEX_INLINE const DexTypeList* dexGetProtoParameters(
    const DexFile *pDexFile, const DexProtoId* pProtoId) {
    if (pProtoId->parametersOff == 0) {
        return NULL;
    }
    return (const DexTypeList*)
        (pDexFile->baseAddr + pProtoId->parametersOff);
}

/* return the ClassDef with the specified index */
DEX_INLINE const DexClassDef* dexGetClassDef(const DexFile* pDexFile, u4 idx) {
    assert(idx < pDexFile->pHeader->classDefsSize);
    return &pDexFile->pClassDefs[idx];
}

/* given a ClassDef pointer, recover its index */
DEX_INLINE u4 dexGetIndexForClassDef(const DexFile* pDexFile,
    const DexClassDef* pClassDef)
{
    assert(pClassDef >= pDexFile->pClassDefs &&
           pClassDef < pDexFile->pClassDefs + pDexFile->pHeader->classDefsSize);
    return pClassDef - pDexFile->pClassDefs;
}

/* get the interface list for a DexClass */
DEX_INLINE const DexTypeList* dexGetInterfacesList(const DexFile* pDexFile,
    const DexClassDef* pClassDef)
{
    if (pClassDef->interfacesOff == 0)
        return NULL;
    return (const DexTypeList*)
        (pDexFile->baseAddr + pClassDef->interfacesOff);
}
/* return the Nth entry in a DexTypeList. */
DEX_INLINE const DexTypeItem* dexGetTypeItem(const DexTypeList* pList,
    u4 idx)
{
    assert(idx < pList->size);
    return &pList->list[idx];
}
/* return the type_idx for the Nth entry in a TypeList */
DEX_INLINE u4 dexTypeListGetIdx(const DexTypeList* pList, u4 idx) {
    const DexTypeItem* pItem = dexGetTypeItem(pList, idx);
    return pItem->typeIdx;
}

/* get the static values list for a DexClass */
DEX_INLINE const DexEncodedArray* dexGetStaticValuesList(
    const DexFile* pDexFile, const DexClassDef* pClassDef)
{
    if (pClassDef->staticValuesOff == 0)
        return NULL;
    return (const DexEncodedArray*)
        (pDexFile->baseAddr + pClassDef->staticValuesOff);
}

/* get the annotations directory item for a DexClass */
DEX_INLINE const DexAnnotationsDirectoryItem* dexGetAnnotationsDirectoryItem(
    const DexFile* pDexFile, const DexClassDef* pClassDef)
{
    if (pClassDef->annotationsOff == 0)
        return NULL;
    return (const DexAnnotationsDirectoryItem*)
        (pDexFile->baseAddr + pClassDef->annotationsOff);
}

/* get the source file string */
DEX_INLINE const char* dexGetSourceFile(
    const DexFile* pDexFile, const DexClassDef* pClassDef)
{
    if (pClassDef->sourceFileIdx == 0xffffffff)
        return NULL;
    return dexStringById(pDexFile, pClassDef->sourceFileIdx);
}

/* get the size, in bytes, of a DexCode */
size_t dexGetDexCodeSize(const DexCode* pCode);

/* Get the list of "tries" for the given DexCode. */
DEX_INLINE const DexTry* dexGetTries(const DexCode* pCode) {
    const u2* insnsEnd = &pCode->insns[pCode->insnsSize];

    // Round to four bytes.
    if ((((uintptr_t) insnsEnd) & 3) != 0) {
        insnsEnd++;
    }

    return (const DexTry*) insnsEnd;
}

/* Get the base of the encoded data for the given DexCode. */
DEX_INLINE const u1* dexGetCatchHandlerData(const DexCode* pCode) {
    const DexTry* pTries = dexGetTries(pCode);
    return (const u1*) &pTries[pCode->triesSize];
}

/* get a pointer to the start of the debugging data */
DEX_INLINE const u1* dexGetDebugInfoStream(const DexFile* pDexFile,
    const DexCode* pCode)
{
    if (pCode->debugInfoOff == 0) {
        return NULL;
    } else {
        return pDexFile->baseAddr + pCode->debugInfoOff;
    }
}

/* DexClassDef convenience - get class descriptor */
DEX_INLINE const char* dexGetClassDescriptor(const DexFile* pDexFile,
    const DexClassDef* pClassDef)
{
    return dexStringByTypeIdx(pDexFile, pClassDef->classIdx);
}

/* DexClassDef convenience - get superclass descriptor */
DEX_INLINE const char* dexGetSuperClassDescriptor(const DexFile* pDexFile,
    const DexClassDef* pClassDef)
{
    if (pClassDef->superclassIdx == 0)
        return NULL;
    return dexStringByTypeIdx(pDexFile, pClassDef->superclassIdx);
}

/* DexClassDef convenience - get class_data_item pointer */
DEX_INLINE const u1* dexGetClassData(const DexFile* pDexFile,
    const DexClassDef* pClassDef)
{
    if (pClassDef->classDataOff == 0)
        return NULL;
    return (const u1*) (pDexFile->baseAddr + pClassDef->classDataOff);
}

/* Get an annotation set at a particular offset. */
DEX_INLINE const DexAnnotationSetItem* dexGetAnnotationSetItem(
    const DexFile* pDexFile, u4 offset)
{
    if (offset == 0) {
        return NULL;
    }
    return (const DexAnnotationSetItem*) (pDexFile->baseAddr + offset);
}
/* get the class' annotation set */
DEX_INLINE const DexAnnotationSetItem* dexGetClassAnnotationSet(
    const DexFile* pDexFile, const DexAnnotationsDirectoryItem* pAnnoDir)
{
    return dexGetAnnotationSetItem(pDexFile, pAnnoDir->classAnnotationsOff);
}

/* get the class' field annotation list */
DEX_INLINE const DexFieldAnnotationsItem* dexGetFieldAnnotations(
    const DexFile* pDexFile, const DexAnnotationsDirectoryItem* pAnnoDir)
{
    if (pAnnoDir->fieldsSize == 0)
        return NULL;

    // Skip past the header to the start of the field annotations.
    return (const DexFieldAnnotationsItem*) &pAnnoDir[1];
}

/* get field annotation list size */
DEX_INLINE int dexGetFieldAnnotationsSize(const DexFile* pDexFile,
    const DexAnnotationsDirectoryItem* pAnnoDir)
{
    return pAnnoDir->fieldsSize;
}

/* return a pointer to the field's annotation set */
DEX_INLINE const DexAnnotationSetItem* dexGetFieldAnnotationSetItem(
    const DexFile* pDexFile, const DexFieldAnnotationsItem* pItem)
{
    return dexGetAnnotationSetItem(pDexFile, pItem->annotationsOff);
}

/* get the class' method annotation list */
DEX_INLINE const DexMethodAnnotationsItem* dexGetMethodAnnotations(
    const DexFile* pDexFile, const DexAnnotationsDirectoryItem* pAnnoDir)
{
    if (pAnnoDir->methodsSize == 0)
        return NULL;

    /*
     * Skip past the header and field annotations to the start of the
     * method annotations.
     */
    const u1* addr = (const u1*) &pAnnoDir[1];
    addr += pAnnoDir->fieldsSize * sizeof (DexFieldAnnotationsItem);
    return (const DexMethodAnnotationsItem*) addr;
}

/* get method annotation list size */
DEX_INLINE int dexGetMethodAnnotationsSize(const DexFile* pDexFile,
    const DexAnnotationsDirectoryItem* pAnnoDir)
{
    return pAnnoDir->methodsSize;
}

/* return a pointer to the method's annotation set */
DEX_INLINE const DexAnnotationSetItem* dexGetMethodAnnotationSetItem(
    const DexFile* pDexFile, const DexMethodAnnotationsItem* pItem)
{
    return dexGetAnnotationSetItem(pDexFile, pItem->annotationsOff);
}

/* get the class' parameter annotation list */
DEX_INLINE const DexParameterAnnotationsItem* dexGetParameterAnnotations(
    const DexFile* pDexFile, const DexAnnotationsDirectoryItem* pAnnoDir)
{
    if (pAnnoDir->parametersSize == 0)
        return NULL;

    /*
     * Skip past the header, field annotations, and method annotations
     * to the start of the parameter annotations.
     */
    const u1* addr = (const u1*) &pAnnoDir[1];
    addr += pAnnoDir->fieldsSize * sizeof (DexFieldAnnotationsItem);
    addr += pAnnoDir->methodsSize * sizeof (DexMethodAnnotationsItem);
    return (const DexParameterAnnotationsItem*) addr;
}

/* get method annotation list size */
DEX_INLINE int dexGetParameterAnnotationsSize(const DexFile* pDexFile,
    const DexAnnotationsDirectoryItem* pAnnoDir)
{
    return pAnnoDir->parametersSize;
}

/* return the parameter annotation ref list */
DEX_INLINE const DexAnnotationSetRefList* dexGetParameterAnnotationSetRefList(
    const DexFile* pDexFile, const DexParameterAnnotationsItem* pItem)
{
    if (pItem->annotationsOff == 0) {
        return NULL;
    }
    return (const DexAnnotationSetRefList*) (pDexFile->baseAddr + pItem->annotationsOff);
}

/* get method annotation list size */
DEX_INLINE int dexGetParameterAnnotationSetRefSize(const DexFile* pDexFile,
    const DexParameterAnnotationsItem* pItem)
{
    if (pItem->annotationsOff == 0) {
        return 0;
    }
    return dexGetParameterAnnotationSetRefList(pDexFile, pItem)->size;
}

/* return the Nth entry from an annotation set ref list */
DEX_INLINE const DexAnnotationSetRefItem* dexGetParameterAnnotationSetRef(
    const DexAnnotationSetRefList* pList, u4 idx)
{
    assert(idx < pList->size);
    return &pList->list[idx];
}

/* given a DexAnnotationSetRefItem, return the DexAnnotationSetItem */
DEX_INLINE const DexAnnotationSetItem* dexGetSetRefItemItem(
    const DexFile* pDexFile, const DexAnnotationSetRefItem* pItem)
{
    return dexGetAnnotationSetItem(pDexFile, pItem->annotationsOff);
}

/* return the Nth annotation offset from a DexAnnotationSetItem */
DEX_INLINE u4 dexGetAnnotationOff(
    const DexAnnotationSetItem* pAnnoSet, u4 idx)
{
    assert(idx < pAnnoSet->size);
    return pAnnoSet->entries[idx];
}

/* return the Nth annotation item from a DexAnnotationSetItem */
DEX_INLINE const DexAnnotationItem* dexGetAnnotationItem(
    const DexFile* pDexFile, const DexAnnotationSetItem* pAnnoSet, u4 idx)
{
    u4 offset = dexGetAnnotationOff(pAnnoSet, idx);
    if (offset == 0) {
        return NULL;
    }
    return (const DexAnnotationItem*) (pDexFile->baseAddr + offset);
}

/*
 * Get the type descriptor character associated with a given primitive
 * type. This returns '\0' if the type is invalid.
 */
char dexGetPrimitiveTypeDescriptorChar(PrimitiveType type);

/*
 * Get the type descriptor string associated with a given primitive
 * type.
 */
const char* dexGetPrimitiveTypeDescriptor(PrimitiveType type);

/*
 * Get the boxed type descriptor string associated with a given
 * primitive type. This returns NULL for an invalid type, including
 * particularly for type "void". In the latter case, even though there
 * is a class Void, there's no such thing as a boxed instance of it.
 */
const char* dexGetBoxedTypeDescriptor(PrimitiveType type);

/*
 * Get the primitive type constant from the given descriptor character.
 * This returns PRIM_NOT (note: this is a 0) if the character is invalid
 * as a primitive type descriptor.
 */
PrimitiveType dexGetPrimitiveTypeFromDescriptorChar(char descriptorChar);

/*
 * System page size.  Normally you're expected to get this from
 * sysconf(_SC_PAGESIZE) or some system-specific define (usually PAGESIZE
 * or PAGE_SIZE).  If we use a simple #define the compiler can generate
 * appropriate masks directly, so we define it here and verify it as the
 * VM is starting up.
 *
 * Must be a power of 2.
 */
#ifdef PAGE_SHIFT
#define SYSTEM_PAGE_SIZE        (1<<PAGE_SHIFT)
#else
#define SYSTEM_PAGE_SIZE        4096
#endif

/*
 * Use this to keep track of mapped segments.
 */
struct MemMapping {
    void*   addr;           /* start of data */
    size_t  length;         /* length of data */

    void*   baseAddr;       /* page-aligned base address */
    size_t  baseLength;     /* length of mapping */
};

/*
 * Copy a map.
 */
void sysCopyMap(MemMapping* dst, const MemMapping* src);

/*
 * Map a file (from fd's current offset) into a shared, read-only memory
 * segment that can be made writable.  (In some cases, such as when
 * mapping a file on a FAT filesystem, the result may be fully writable.)
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysMapFileInShmemWritableReadOnly(int fd, MemMapping* pMap);

/*
 * Map part of a file into a shared, read-only memory segment.
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysMapFileSegmentInShmem(int fd, off_t start, size_t length,
    MemMapping* pMap);

/*
 * Create a private anonymous mapping, useful for large allocations.
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysCreatePrivateMap(size_t length, MemMapping* pMap);

/*
 * Change the access rights on one or more pages.  If "wantReadWrite" is
 * zero, the pages will be made read-only; otherwise they will be read-write.
 *
 * Returns 0 on success.
 */
int sysChangeMapAccess(void* addr, size_t length, int wantReadWrite,
    MemMapping* pmap);

/*
 * Release the pages associated with a shared memory segment.
 *
 * This does not free "pMap"; it just releases the memory.
 */
void sysReleaseShmem(MemMapping* pMap);

/*
 * Write until all bytes have been written.
 *
 * Returns 0 on success, or an errno value on failure.
 */
int sysWriteFully(int fd, const void* buf, size_t count, const char* logMsg);

/*
 * Copy the given number of bytes from one fd to another. Returns
 * 0 on success, -1 on failure.
 */
int sysCopyFileToFile(int outFd, int inFd, size_t count);


/*
 * Some additional VM data structures that are associated with the DEX file.
 */
struct DvmDex {
    /* pointer to the DexFile we're associated with */
    DexFile*            pDexFile;

    /* clone of pDexFile->pHeader (it's used frequently enough) */
    const DexHeader*    pHeader;

    /* interned strings; parallel to "stringIds" */
    struct StringObject** pResStrings;

    /* resolved classes; parallel to "typeIds" */
    struct ClassObject** pResClasses;

    /* resolved methods; parallel to "methodIds" */
    struct Method**     pResMethods;

    /* resolved instance fields; parallel to "fieldIds" */
    /* (this holds both InstField and StaticField) */
    struct Field**      pResFields;

    /* interface method lookup cache */
    struct AtomicCache* pInterfaceCache;

    /* shared memory region with file contents */
    bool                isMappedReadOnly;
    MemMapping          memMap;

    jobject dex_object;

    /* lock ensuring mutual exclusion during updates */
    pthread_mutex_t     modLock;
};


/*
 * Given a file descriptor for an open "optimized" DEX file, map it into
 * memory and parse the contents.
 *
 * On success, returns 0 and sets "*ppDvmDex" to a newly-allocated DvmDex.
 * On failure, returns a meaningful error code [currently just -1].
 */
int dvmDexFileOpenFromFd(int fd, DvmDex** ppDvmDex);

/*
 * Open a partial DEX file.  Only useful as part of the optimization process.
 */
int dvmDexFileOpenPartial(const void* addr, int len, DvmDex** ppDvmDex);

/*
 * Free a DvmDex structure, along with any associated structures.
 */
void dvmDexFileFree(DvmDex* pDvmDex);


/*
 * Change the 1- or 2-byte value at the specified address to a new value.  If
 * the location already has the new value, do nothing.
 *
 * This does not make any synchronization guarantees.  The caller must
 * ensure exclusivity vs. other callers.
 *
 * For the 2-byte call, the pointer should have 16-bit alignment.
 *
 * Returns "true" on success.
 */
bool dvmDexChangeDex1(DvmDex* pDvmDex, u1* addr, u1 newVal);
bool dvmDexChangeDex2(DvmDex* pDvmDex, u2* addr, u2 newVal);


/*
 * In gcc, "extern inline" ensures that the copy in the header is never
 * turned into a separate function.  This prevents us from having multiple
 * non-inline copies.  However, we still need to provide a non-inline
 * version in the library for the benefit of applications that include our
 * headers and are built with optimizations disabled.  Either that, or use
 * the "always_inline" gcc attribute to ensure that the non-inline version
 * is never needed.
 *
 * (Note C99 has different notions about what the keyword combos mean.)
 */
#ifndef _DALVIK_GEN_INLINES             /* only defined by Inlines.c */
# define INLINE extern __inline__
#else
# define INLINE
#endif

/*
 * Return the requested item if it has been resolved, or NULL if it hasn't.
 */
INLINE struct StringObject* dvmDexGetResolvedString(const DvmDex* pDvmDex,
    u4 stringIdx)
{
    assert(stringIdx < pDvmDex->pHeader->stringIdsSize);
    return pDvmDex->pResStrings[stringIdx];
}
INLINE struct ClassObject* dvmDexGetResolvedClass(const DvmDex* pDvmDex,
    u4 classIdx)
{
    assert(classIdx < pDvmDex->pHeader->typeIdsSize);
    return pDvmDex->pResClasses[classIdx];
}
INLINE struct Method* dvmDexGetResolvedMethod(const DvmDex* pDvmDex,
    u4 methodIdx)
{
    assert(methodIdx < pDvmDex->pHeader->methodIdsSize);
    return pDvmDex->pResMethods[methodIdx];
}
INLINE struct Field* dvmDexGetResolvedField(const DvmDex* pDvmDex,
    u4 fieldIdx)
{
    assert(fieldIdx < pDvmDex->pHeader->fieldIdsSize);
    return pDvmDex->pResFields[fieldIdx];
}

/*
 * Class objects have many additional fields.  This is used for both
 * classes and interfaces, including synthesized classes (arrays and
 * primitive types).
 *
 * Class objects are unusual in that they have some fields allocated with
 * the system malloc (or LinearAlloc), rather than on the GC heap.  This is
 * handy during initialization, but does require special handling when
 * discarding java.lang.Class objects.
 *
 * The separation of methods (direct vs. virtual) and fields (class vs.
 * instance) used in Dalvik works out pretty well.  The only time it's
 * annoying is when enumerating or searching for things with reflection.
 */
struct ClassObject : Object {
    /* leave space for instance data; we could access fields directly if we
       freeze the definition of java/lang/Class */
    u4              instanceData[CLASS_FIELD_SLOTS];

    /* UTF-8 descriptor for the class; from constant pool, or on heap
       if generated ("[C") */
    const char*     descriptor;
    char*           descriptorAlloc;

    /* access flags; low 16 bits are defined by VM spec */
    u4              accessFlags;

    /* VM-unique class serial number, nonzero, set very early */
    u4              serialNumber;

    /* DexFile from which we came; needed to resolve constant pool entries */
    /* (will be NULL for VM-generated, e.g. arrays and primitive classes) */
    DvmDex*         pDvmDex;

    /* state of class initialization */
    ClassStatus     status;

    /* if class verify fails, we must return same error on subsequent tries */
    ClassObject*    verifyErrorClass;

    /* threadId, used to check for recursive <clinit> invocation */
    u4              initThreadId;

    /*
     * Total object size; used when allocating storage on gc heap.  (For
     * interfaces and abstract classes this will be zero.)
     */
    size_t          objectSize;

    /* arrays only: class object for base element, for instanceof/checkcast
       (for String[][][], this will be String) */
    ClassObject*    elementClass;

    /* arrays only: number of dimensions, e.g. int[][] is 2 */
    int             arrayDim;

    /* primitive type index, or PRIM_NOT (-1); set for generated prim classes */
    PrimitiveType   primitiveType;

    /* superclass, or NULL if this is java.lang.Object */
    ClassObject*    super;

    /* defining class loader, or NULL for the "bootstrap" system loader */
    Object*         classLoader;

    /* initiating class loader list */
    /* NOTE: for classes with low serialNumber, these are unused, and the
       values are kept in a table in gDvm. */
    InitiatingLoaderList initiatingLoaderList;

    /* array of interfaces this class implements directly */
    int             interfaceCount;
    ClassObject**   interfaces;

    /* static, private, and <init> methods */
    int             directMethodCount;
    Method*         directMethods;

    /* virtual methods defined in this class; invoked through vtable */
    int             virtualMethodCount;
    Method*         virtualMethods;

    /*
     * Virtual method table (vtable), for use by "invoke-virtual".  The
     * vtable from the superclass is copied in, and virtual methods from
     * our class either replace those from the super or are appended.
     */
    int             vtableCount;
    Method**        vtable;

    /*
     * Interface table (iftable), one entry per interface supported by
     * this class.  That means one entry for each interface we support
     * directly, indirectly via superclass, or indirectly via
     * superinterface.  This will be null if neither we nor our superclass
     * implement any interfaces.
     *
     * Why we need this: given "class Foo implements Face", declare
     * "Face faceObj = new Foo()".  Invoke faceObj.blah(), where "blah" is
     * part of the Face interface.  We can't easily use a single vtable.
     *
     * For every interface a concrete class implements, we create a list of
     * virtualMethod indices for the methods in the interface.
     */
    int             iftableCount;
    InterfaceEntry* iftable;

    /*
     * The interface vtable indices for iftable get stored here.  By placing
     * them all in a single pool for each class that implements interfaces,
     * we decrease the number of allocations.
     */
    int             ifviPoolCount;
    int*            ifviPool;

    /* instance fields
     *
     * These describe the layout of the contents of a DataObject-compatible
     * Object.  Note that only the fields directly defined by this class
     * are listed in ifields;  fields defined by a superclass are listed
     * in the superclass's ClassObject.ifields.
     *
     * All instance fields that refer to objects are guaranteed to be
     * at the beginning of the field list.  ifieldRefCount specifies
     * the number of reference fields.
     */
    int             ifieldCount;
    int             ifieldRefCount; // number of fields that are object refs
    InstField*      ifields;

    /* bitmap of offsets of ifields */
    u4 refOffsets;

    /* source file name, if known */
    const char*     sourceFile;

    /* static fields */
    int             sfieldCount;
    StaticField     sfields[0]; /* MUST be last item */
};



/*
 * Single-thread single-string cache. This structure holds a pointer to
 * a string which is semi-automatically manipulated by some of the
 * method prototype functions. Functions which use in this struct
 * generally return a string that is valid until the next
 * time the same DexStringCache is used.
 */
struct DexStringCache {
    char* value;          /* the latest value */
    size_t allocatedSize; /* size of the allocated buffer, if allocated */
    char buffer[120];     /* buffer used to hold small-enough results */
};

/*
 * Make sure that the given cache can hold a string of the given length,
 * including the final '\0' byte.
 */
void dexStringCacheAlloc(DexStringCache* pCache, size_t length);

/*
 * Initialize the given DexStringCache. Use this function before passing
 * one into any other function.
 */
void dexStringCacheInit(DexStringCache* pCache);

/*
 * Release the allocated contents of the given DexStringCache, if any.
 * Use this function after your last use of a DexStringCache.
 */
void dexStringCacheRelease(DexStringCache* pCache);

/*
 * If the given DexStringCache doesn't already point at the given value,
 * make a copy of it into the cache. This always returns a writable
 * pointer to the contents (whether or not a copy had to be made). This
 * function is intended to be used after making a call that at least
 * sometimes doesn't populate a DexStringCache.
 */
char* dexStringCacheEnsureCopy(DexStringCache* pCache, const char* value);

/*
 * Abandon the given DexStringCache, and return a writable copy of the
 * given value (reusing the string cache's allocation if possible).
 * The return value must be free()d by the caller. Use this instead of
 * dexStringCacheRelease() if you want the buffer to survive past the
 * scope of the DexStringCache.
 */
char* dexStringCacheAbandon(DexStringCache* pCache, const char* value);

/*
 * Method prototype structure, which refers to a protoIdx in a
 * particular DexFile.
 */
struct DexProto {
    const DexFile* dexFile;     /* file the idx refers to */
    u4 protoIdx;                /* index into proto_ids table of dexFile */
};

/*
 * Set the given DexProto to refer to the prototype of the given MethodId.
 */
DEX_INLINE void dexProtoSetFromMethodId(DexProto* pProto,
    const DexFile* pDexFile, const DexMethodId* pMethodId)
{
    pProto->dexFile = pDexFile;
    pProto->protoIdx = pMethodId->protoIdx;
}

/*
 * Get the short-form method descriptor for the given prototype. The
 * prototype must be protoIdx-based.
 */
const char* dexProtoGetShorty(const DexProto* pProto);

/*
 * Get the full method descriptor for the given prototype.
 */
const char* dexProtoGetMethodDescriptor(const DexProto* pProto,
    DexStringCache* pCache);

/*
 * Get a copy of the descriptor string associated with the given prototype.
 * The returned pointer must be free()ed by the caller.
 */
char* dexProtoCopyMethodDescriptor(const DexProto* pProto);

/*
 * Get the parameter descriptors for the given prototype. This is the
 * concatenation of all the descriptors for all the parameters, in
 * order, with no other adornment.
 */
const char* dexProtoGetParameterDescriptors(const DexProto* pProto,
    DexStringCache* pCache);

/*
 * Return the utf-8 encoded descriptor string from the proto of a MethodId.
 */
DEX_INLINE const char* dexGetDescriptorFromMethodId(const DexFile* pDexFile,
        const DexMethodId* pMethodId, DexStringCache* pCache)
{
    DexProto proto;

    dexProtoSetFromMethodId(&proto, pDexFile, pMethodId);
    return dexProtoGetMethodDescriptor(&proto, pCache);
}

/*
 * Get a copy of the utf-8 encoded method descriptor string from the
 * proto of a MethodId. The returned pointer must be free()ed by the
 * caller.
 */
DEX_INLINE char* dexCopyDescriptorFromMethodId(const DexFile* pDexFile,
    const DexMethodId* pMethodId)
{
    DexProto proto;

    dexProtoSetFromMethodId(&proto, pDexFile, pMethodId);
    return dexProtoCopyMethodDescriptor(&proto);
}

/*
 * Get the type descriptor for the return type of the given prototype.
 */
const char* dexProtoGetReturnType(const DexProto* pProto);

/*
 * Get the parameter count of the given prototype.
 */
size_t dexProtoGetParameterCount(const DexProto* pProto);

/*
 * Compute the number of parameter words (u4 units) required by the
 * given prototype. For example, if the method takes (int, long) and
 * returns double, this would return 3 (one for the int, two for the
 * long, and the return type isn't relevant).
 */
int dexProtoComputeArgsSize(const DexProto* pProto);

/*
 * Compare the two prototypes. The two prototypes are compared
 * with the return type as the major order, then the first arguments,
 * then second, etc. If two prototypes are identical except that one
 * has extra arguments, then the shorter argument is considered the
 * earlier one in sort order (similar to strcmp()).
 */
int dexProtoCompare(const DexProto* pProto1, const DexProto* pProto2);

/*
 * Compare the two prototypes, ignoring return type. The two
 * prototypes are compared with the first argument as the major order,
 * then second, etc. If two prototypes are identical except that one
 * has extra arguments, then the shorter argument is considered the
 * earlier one in sort order (similar to strcmp()).
 */
int dexProtoCompareParameters(const DexProto* pProto1,
        const DexProto* pProto2);

/*
 * Compare a prototype and a string method descriptor. The comparison
 * is done as if the descriptor were converted to a prototype and compared
 * with dexProtoCompare().
 */
int dexProtoCompareToDescriptor(const DexProto* proto, const char* descriptor);

/*
 * Compare a prototype and a concatenation of type descriptors. The
 * comparison is done as if the descriptors were converted to a
 * prototype and compared with dexProtoCompareParameters().
 */
int dexProtoCompareToParameterDescriptors(const DexProto* proto,
        const char* descriptors);

/*
 * Single-thread prototype parameter iterator. This structure holds a
 * pointer to a prototype and its parts, along with a cursor.
 */
struct DexParameterIterator {
    const DexProto* proto;
    const DexTypeList* parameters;
    int parameterCount;
    int cursor;
};

/*
 * Initialize the given DexParameterIterator to be at the start of the
 * parameters of the given prototype.
 */
void dexParameterIteratorInit(DexParameterIterator* pIterator,
        const DexProto* pProto);

/*
 * Get the type_id index for the next parameter, if any. This returns
 * kDexNoIndex if the last parameter has already been consumed.
 */
u4 dexParameterIteratorNextIndex(DexParameterIterator* pIterator);

/*
 * Get the type descriptor for the next parameter, if any. This returns
 * NULL if the last parameter has already been consumed.
 */
const char* dexParameterIteratorNextDescriptor(
        DexParameterIterator* pIterator);



/*
 * Format enumeration for RegisterMap data area.
 */
enum RegisterMapFormat {
    kRegMapFormatUnknown = 0,
    kRegMapFormatNone,          /* indicates no map data follows */
    kRegMapFormatCompact8,      /* compact layout, 8-bit addresses */
    kRegMapFormatCompact16,     /* compact layout, 16-bit addresses */
    kRegMapFormatDifferential,  /* compressed, differential encoding */

    kRegMapFormatOnHeap = 0x80, /* bit flag, indicates allocation on heap */
};

/*
 * This is a single variable-size structure.  It may be allocated on the
 * heap or mapped out of a (post-dexopt) DEX file.
 *
 * 32-bit alignment of the structure is NOT guaranteed.  This makes it a
 * little awkward to deal with as a structure; to avoid accidents we use
 * only byte types.  Multi-byte values are little-endian.
 *
 * Size of (format==FormatNone): 1 byte
 * Size of (format==FormatCompact8): 4 + (1 + regWidth) * numEntries
 * Size of (format==FormatCompact16): 4 + (2 + regWidth) * numEntries
 */
struct RegisterMap {
    /* header */
    u1      format;         /* enum RegisterMapFormat; MUST be first entry */
    u1      regWidth;       /* bytes per register line, 1+ */
    u1      numEntries[2];  /* number of entries */

    /* raw data starts here; need not be aligned */
    u1      data[1];
};

bool dvmRegisterMapStartup(void);
void dvmRegisterMapShutdown(void);

/*
 * Get the format.
 */
INLINE RegisterMapFormat dvmRegisterMapGetFormat(const RegisterMap* pMap) {
    return (RegisterMapFormat)(pMap->format & ~(kRegMapFormatOnHeap));
}

/*
 * Set the format.
 */
INLINE void dvmRegisterMapSetFormat(RegisterMap* pMap, RegisterMapFormat format)
{
    pMap->format &= kRegMapFormatOnHeap;
    pMap->format |= format;
}

/*
 * Get the "on heap" flag.
 */
INLINE bool dvmRegisterMapGetOnHeap(const RegisterMap* pMap) {
    return (pMap->format & kRegMapFormatOnHeap) != 0;
}

/*
 * Get the register bit vector width, in bytes.
 */
INLINE u1 dvmRegisterMapGetRegWidth(const RegisterMap* pMap) {
    return pMap->regWidth;
}

/*
 * Set the register bit vector width, in bytes.
 */
INLINE void dvmRegisterMapSetRegWidth(RegisterMap* pMap, int regWidth) {
    pMap->regWidth = regWidth;
}

/*
 * Set the "on heap" flag.
 */
INLINE void dvmRegisterMapSetOnHeap(RegisterMap* pMap, bool val) {
    if (val)
        pMap->format |= kRegMapFormatOnHeap;
    else
        pMap->format &= ~(kRegMapFormatOnHeap);
}

/*
 * Get the number of entries in this map.
 */
INLINE u2 dvmRegisterMapGetNumEntries(const RegisterMap* pMap) {
    return pMap->numEntries[0] | (pMap->numEntries[1] << 8);
}

/*
 * Set the number of entries in this map.
 */
INLINE void dvmRegisterMapSetNumEntries(RegisterMap* pMap, u2 numEntries) {
    pMap->numEntries[0] = (u1) numEntries;
    pMap->numEntries[1] = numEntries >> 8;
}

/*
 * Retrieve the bit vector for the specified address.  This is a pointer
 * to the bit data from an uncompressed map, or to a temporary copy of
 * data from a compressed map.
 *
 * The caller must call dvmReleaseRegisterMapLine() with the result.
 *
 * Returns NULL if not found.
 */
const u1* dvmRegisterMapGetLine(const RegisterMap* pMap, int addr);

/*
 * Release "data".
 *
 * If "pMap" points to a compressed map from which we have expanded a
 * single line onto the heap, this will free "data"; otherwise, it does
 * nothing.
 *
 * TODO: decide if this is still a useful concept.
 */
INLINE void dvmReleaseRegisterMapLine(const RegisterMap* pMap, const u1* data)
{}


/*
 * A pool of register maps for methods associated with a single class.
 *
 * Each entry is a 4-byte method index followed by the 32-bit-aligned
 * RegisterMap.  The size of the RegisterMap is determined by parsing
 * the map.  The lack of an index reduces random access speed, but we
 * should be doing that rarely (during class load) and it saves space.
 *
 * These structures are 32-bit aligned.
 */
struct RegisterMapMethodPool {
    u2      methodCount;            /* chiefly used as a sanity check */

    /* stream of per-method data starts here */
    u4      methodData[1];
};

/*
 * Header for the memory-mapped RegisterMap pool in the DEX file.
 *
 * The classDataOffset table provides offsets from the start of the
 * RegisterMapPool structure.  There is one entry per class (including
 * interfaces, which can have static initializers).
 *
 * The offset points to a RegisterMapMethodPool.
 *
 * These structures are 32-bit aligned.
 */
struct RegisterMapClassPool {
    u4      numClasses;

    /* offset table starts here, 32-bit aligned; offset==0 means no data */
    u4      classDataOffset[1];
};

/*
 * Find the register maps for this class.  (Used during class loading.)
 * If "pNumMaps" is non-NULL, it will return the number of maps in the set.
 *
 * Returns NULL if none is available.
 */
const void* dvmRegisterMapGetClassData(const DexFile* pDexFile, u4 classIdx,
    u4* pNumMaps);

/*
 * Get the register map for the next method.  "*pPtr" will be advanced past
 * the end of the map.  (Used during class loading.)
 *
 * This should initially be called with the result from
 * dvmRegisterMapGetClassData().
 */
const RegisterMap* dvmRegisterMapGetNext(const void** pPtr);

/*
 * This holds some meta-data while we construct the set of register maps
 * for a DEX file.
 *
 * In particular, it keeps track of our temporary mmap region so we can
 * free it later.
 */
struct RegisterMapBuilder {
    /* public */
    void*       data;
    size_t      size;

    /* private */
    MemMapping  memMap;
};

/*
 * Generate a register map set for all verified classes in "pDvmDex".
 */
RegisterMapBuilder* dvmGenerateRegisterMaps(DvmDex* pDvmDex);

/*
 * Free the builder.
 */
void dvmFreeRegisterMapBuilder(RegisterMapBuilder* pBuilder);



/*
 * Enumeration for register type values.  The "hi" piece of a 64-bit value
 * MUST immediately follow the "lo" piece in the enumeration, so we can check
 * that hi==lo+1.
 *
 * Assignment of constants:
 *   [-MAXINT,-32768)   : integer
 *   [-32768,-128)      : short
 *   [-128,0)           : byte
 *   0                  : zero
 *   1                  : one
 *   [2,128)            : posbyte
 *   [128,32768)        : posshort
 *   [32768,65536)      : char
 *   [65536,MAXINT]     : integer
 *
 * Allowed "implicit" widening conversions:
 *   zero -> boolean, posbyte, byte, posshort, short, char, integer, ref (null)
 *   one -> boolean, posbyte, byte, posshort, short, char, integer
 *   boolean -> posbyte, byte, posshort, short, char, integer
 *   posbyte -> posshort, short, integer, char
 *   byte -> short, integer
 *   posshort -> integer, char
 *   short -> integer
 *   char -> integer
 *
 * In addition, all of the above can convert to "float".
 *
 * We're more careful with integer values than the spec requires.  The
 * motivation is to restrict byte/char/short to the correct range of values.
 * For example, if a method takes a byte argument, we don't want to allow
 * the code to load the constant "1024" and pass it in.
 */
enum {
    kRegTypeUnknown = 0,    /* initial state; use value=0 so calloc works */
    kRegTypeUninit = 1,     /* MUST be odd to distinguish from pointer */
    kRegTypeConflict,       /* merge clash makes this reg's type unknowable */

    /*
     * Category-1nr types.  The order of these is chiseled into a couple
     * of tables, so don't add, remove, or reorder if you can avoid it.
     */
#define kRegType1nrSTART    kRegTypeZero
    kRegTypeZero,           /* 32-bit 0, could be Boolean, Int, Float, or Ref */
    kRegTypeOne,            /* 32-bit 1, could be Boolean, Int, Float */
    kRegTypeBoolean,        /* must be 0 or 1 */
    kRegTypeConstPosByte,   /* const derived byte, known positive */
    kRegTypeConstByte,      /* const derived byte */
    kRegTypeConstPosShort,  /* const derived short, known positive */
    kRegTypeConstShort,     /* const derived short */
    kRegTypeConstChar,      /* const derived char */
    kRegTypeConstInteger,   /* const derived integer */
    kRegTypePosByte,        /* byte, known positive (can become char) */
    kRegTypeByte,
    kRegTypePosShort,       /* short, known positive (can become char) */
    kRegTypeShort,
    kRegTypeChar,
    kRegTypeInteger,
    kRegTypeFloat,
#define kRegType1nrEND      kRegTypeFloat

    kRegTypeConstLo,        /* const derived wide, lower half */
    kRegTypeConstHi,        /* const derived wide, upper half */
    kRegTypeLongLo,         /* lower-numbered register; endian-independent */
    kRegTypeLongHi,
    kRegTypeDoubleLo,
    kRegTypeDoubleHi,

    /*
     * Enumeration max; this is used with "full" (32-bit) RegType values.
     *
     * Anything larger than this is a ClassObject or uninit ref.  Mask off
     * all but the low 8 bits; if you're left with kRegTypeUninit, pull
     * the uninit index out of the high 24.  Because kRegTypeUninit has an
     * odd value, there is no risk of a particular ClassObject pointer bit
     * pattern being confused for it (assuming our class object allocator
     * uses word alignment).
     */
    kRegTypeMAX
};
#define kRegTypeUninitMask  0xff
#define kRegTypeUninitShift 8

/*
 * RegType holds information about the type of data held in a register.
 * For most types it's a simple enum.  For reference types it holds a
 * pointer to the ClassObject, and for uninitialized references it holds
 * an index into the UninitInstanceMap.
 */
typedef u4 RegType;

/*
 * A bit vector indicating which entries in the monitor stack are
 * associated with this register.  The low bit corresponds to the stack's
 * bottom-most entry.
 */
typedef u4 MonitorEntries;
#define kMaxMonitorStackDepth   (sizeof(MonitorEntries) * 8)

/*
 * During verification, we associate one of these with every "interesting"
 * instruction.  We track the status of all registers, and (if the method
 * has any monitor-enter instructions) maintain a stack of entered monitors
 * (identified by code unit offset).
 *
 * If live-precise register maps are enabled, the "liveRegs" vector will
 * be populated.  Unlike the other lists of registers here, we do not
 * track the liveness of the method result register (which is not visible
 * to the GC).
 */
struct RegisterLine {
    RegType*        regTypes;
    MonitorEntries* monitorEntries;
    u4*             monitorStack;
    unsigned int    monitorStackTop;
    struct BitVector*      liveRegs;
};

/*
 * Table that maps uninitialized instances to classes, based on the
 * address of the new-instance instruction.  One per method.
 */
struct UninitInstanceMap {
    int numEntries;
    struct {
        int             addr;   /* code offset, or -1 for method arg ("this") */
        ClassObject*    clazz;  /* class created at this address */
    } map[1];
};
#define kUninitThisArgAddr  (-1)
#define kUninitThisArgSlot  0

/*
 * InsnFlags is a 32-bit integer with the following layout:
 *   0-15  instruction length (or 0 if this address doesn't hold an opcode)
 *  16-31  single bit flags:
 *    InTry: in "try" block; exceptions thrown here may be caught locally
 *    BranchTarget: other instructions can branch to this instruction
 *    GcPoint: this instruction is a GC safe point
 *    Visited: verifier has examined this instruction at least once
 *    Changed: set/cleared as bytecode verifier runs
 */
typedef u4 InsnFlags;

#define kInsnFlagWidthMask      0x0000ffff
#define kInsnFlagInTry          (1 << 16)
#define kInsnFlagBranchTarget   (1 << 17)
#define kInsnFlagGcPoint        (1 << 18)
#define kInsnFlagVisited        (1 << 30)
#define kInsnFlagChanged        (1 << 31)

/* add opcode widths to InsnFlags */
bool dvmComputeCodeWidths(const Method* meth, InsnFlags* insnFlags,
    int* pNewInstanceCount);

/* set the "in try" flag for sections of code wrapped with a "try" block */
bool dvmSetTryFlags(const Method* meth, InsnFlags* insnFlags);

/* verification failure reporting */
#define LOG_VFY(...)                dvmLogVerifyFailure(NULL, __VA_ARGS__)
#define LOG_VFY_METH(_meth, ...)    dvmLogVerifyFailure(_meth, __VA_ARGS__)

/* log verification failure with optional method info */
void dvmLogVerifyFailure(const Method* meth, const char* format, ...)
#if defined(__GNUC__)
    __attribute__ ((format(printf, 2, 3)))
#endif
    ;

/* log verification failure due to resolution trouble */
void dvmLogUnableToResolveClass(const char* missingClassDescr,
    const Method* meth);

/* extract the relative branch offset from a branch instruction */
bool dvmGetBranchOffset(const Method* meth, const InsnFlags* insnFlags,
    int curOffset, s4* pOffset, bool* pConditional);

/* return a RegType enumeration value that "value" just fits into */
char dvmDetermineCat1Const(s4 value);

/* debugging */
bool dvmWantVerboseVerification(const Method* meth);


/*
 * Various bits of data used by the verifier and register map generator.
 */
struct VerifierData {
    /*
     * The method we're working on.
     */
    const Method*   method;

    /*
     * Number of code units of instructions in the method.  A cache of the
     * value calculated by dvmGetMethodInsnsSize().
     */
    u4              insnsSize;

    /*
     * Number of registers we track for each instruction.  This is equal
     * to the method's declared "registersSize".  (Does not include the
     * pending return value.)
     */
    u4              insnRegCount;

    /*
     * Instruction widths and flags, one entry per code unit.
     */
    InsnFlags*      insnFlags;

    /*
     * Uninitialized instance map, used for tracking the movement of
     * objects that have been allocated but not initialized.
     */
    UninitInstanceMap* uninitMap;

    /*
     * Array of RegisterLine structs, one entry per code unit.  We only need
     * entries for code units that hold the start of an "interesting"
     * instruction.  For register map generation, we're only interested
     * in GC points.
     */
    RegisterLine*   registerLines;

    /*
     * The number of occurrences of specific opcodes.
     */
    size_t          newInstanceCount;
    size_t          monitorEnterCount;

    /*
     * Array of pointers to basic blocks, one entry per code unit.  Used
     * for liveness analysis.
     */
    struct VfyBasicBlock** basicBlocks;
};


/* table with static merge logic for primitive types */
extern const char gDvmMergeTab[kRegTypeMAX][kRegTypeMAX];


/*
 * Returns "true" if the flags indicate that this address holds the start
 * of an instruction.
 */
INLINE bool dvmInsnIsOpcode(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagWidthMask) != 0;
}

/*
 * Extract the unsigned 16-bit instruction width from "flags".
 */
INLINE int dvmInsnGetWidth(const InsnFlags* insnFlags, int addr) {
    return insnFlags[addr] & kInsnFlagWidthMask;
}

/*
 * Changed?
 */
INLINE bool dvmInsnIsChanged(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagChanged) != 0;
}
INLINE void dvmInsnSetChanged(InsnFlags* insnFlags, int addr, bool changed)
{
    if (changed)
        insnFlags[addr] |= kInsnFlagChanged;
    else
        insnFlags[addr] &= ~kInsnFlagChanged;
}

/*
 * Visited?
 */
INLINE bool dvmInsnIsVisited(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagVisited) != 0;
}
INLINE void dvmInsnSetVisited(InsnFlags* insnFlags, int addr, bool changed)
{
    if (changed)
        insnFlags[addr] |= kInsnFlagVisited;
    else
        insnFlags[addr] &= ~kInsnFlagVisited;
}

/*
 * Visited or changed?
 */
INLINE bool dvmInsnIsVisitedOrChanged(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & (kInsnFlagVisited|kInsnFlagChanged)) != 0;
}

/*
 * In a "try" block?
 */
INLINE bool dvmInsnIsInTry(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagInTry) != 0;
}
INLINE void dvmInsnSetInTry(InsnFlags* insnFlags, int addr, bool inTry)
{
    assert(inTry);
    //if (inTry)
        insnFlags[addr] |= kInsnFlagInTry;
    //else
    //    insnFlags[addr] &= ~kInsnFlagInTry;
}

/*
 * Instruction is a branch target or exception handler?
 */
INLINE bool dvmInsnIsBranchTarget(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagBranchTarget) != 0;
}
INLINE void dvmInsnSetBranchTarget(InsnFlags* insnFlags, int addr,
    bool isBranch)
{
    assert(isBranch);
    //if (isBranch)
        insnFlags[addr] |= kInsnFlagBranchTarget;
    //else
    //    insnFlags[addr] &= ~kInsnFlagBranchTarget;
}

/*
 * Instruction is a GC point?
 */
INLINE bool dvmInsnIsGcPoint(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagGcPoint) != 0;
}
INLINE void dvmInsnSetGcPoint(InsnFlags* insnFlags, int addr,
    bool isGcPoint)
{
    assert(isGcPoint);
    //if (isGcPoint)
        insnFlags[addr] |= kInsnFlagGcPoint;
    //else
    //    insnFlags[addr] &= ~kInsnFlagGcPoint;
}

/*
 * Create a new UninitInstanceMap.
 */
UninitInstanceMap* dvmCreateUninitInstanceMap(const Method* meth,
    const InsnFlags* insnFlags, int newInstanceCount);

/*
 * Release the storage associated with an UninitInstanceMap.
 */
void dvmFreeUninitInstanceMap(UninitInstanceMap* uninitMap);

/*
 * Verify bytecode in "meth".  "insnFlags" should be populated with
 * instruction widths and "in try" flags.
 */
bool dvmVerifyCodeFlow(VerifierData* vdata);

/*
 * Generate the register map for a method that has just been verified
 * (i.e. we're doing this as part of verification).
 *
 * Returns a pointer to a newly-allocated RegisterMap, or NULL on failure.
 */
RegisterMap* dvmGenerateRegisterMapV(VerifierData* vdata);


/* dump stats gathered during register map creation process */
void dvmRegisterMapDumpStats(void);

/*
 * A method.  We create one of these for every method in every class
 * we load, so try to keep the size to a minimum.
 *
 * Much of this comes from and could be accessed in the data held in shared
 * memory.  We hold it all together here for speed.  Everything but the
 * pointers could be held in a shared table generated by the optimizer;
 * if we're willing to convert them to offsets and take the performance
 * hit (e.g. "meth->insns" becomes "baseAddr + meth->insnsOffset") we
 * could move everything but "nativeFunc".
 */
struct Method {
    /* the class we are a part of */
    ClassObject*    clazz;

    /* access flags; low 16 bits are defined by spec (could be u2?) */
    u4              accessFlags;

    /*
     * For concrete virtual methods, this is the offset of the method
     * in "vtable".
     *
     * For abstract methods in an interface class, this is the offset
     * of the method in "iftable[n]->methodIndexArray".
     */
    u2             methodIndex;

    /*
     * Method bounds; not needed for an abstract method.
     *
     * For a native method, we compute the size of the argument list, and
     * set "insSize" and "registerSize" equal to it.
     */
    u2              registersSize;  /* ins + locals */
    u2              outsSize;
    u2              insSize;

    /* method name, e.g. "<init>" or "eatLunch" */
    const char*     name;

    /*
     * Method prototype descriptor string (return and argument types).
     *
     * TODO: This currently must specify the DexFile as well as the proto_ids
     * index, because generated Proxy classes don't have a DexFile.  We can
     * remove the DexFile* and reduce the size of this struct if we generate
     * a DEX for proxies.
     */
	DexProto        prototype;

    /* short-form method descriptor string */
    const char*     shorty;

    /*
     * The remaining items are not used for abstract or native methods.
     * (JNI is currently hijacking "insns" as a function pointer, set
     * after the first call.  For internal-native this stays null.)
     */

    /* the actual code */
    const u2*       insns;          /* instructions, in memory-mapped .dex */

    /* JNI: cached argument and return-type hints */
    int             jniArgInfo;

    /*
     * JNI: native method ptr; could be actual function or a JNI bridge.  We
     * don't currently discriminate between DalvikBridgeFunc and
     * DalvikNativeFunc; the former takes an argument superset (i.e. two
     * extra args) which will be ignored.  If necessary we can use
     * insns==NULL to detect JNI bridge vs. internal native.
     */
    DalvikBridgeFunc nativeFunc;

    /*
     * JNI: true if this static non-synchronized native method (that has no
     * reference arguments) needs a JNIEnv* and jclass/jobject. Libcore
     * uses this.
     */
    bool fastJni;

    /*
     * JNI: true if this method has no reference arguments. This lets the JNI
     * bridge avoid scanning the shorty for direct pointers that need to be
     * converted to local references.
     *
     * TODO: replace this with a list of indexes of the reference arguments.
     */
    bool noRef;

    /*
     * JNI: true if we should log entry and exit. This is the only way
     * developers can log the local references that are passed into their code.
     * Used for debugging JNI problems in third-party code.
     */
    bool shouldTrace;

    /*
     * Register map data, if available.  This will point into the DEX file
     * if the data was computed during pre-verification, or into the
     * linear alloc area if not.
     */
    const RegisterMap* registerMap;

    /* set if method was called during method profiling */
    bool            inProfile;
};


/*
 * Get the expanded form of the register map associated with the specified
 * method.  May update method->registerMap, possibly freeing the previous
 * map.
 *
 * Returns NULL on failure (e.g. unable to expand map).
 *
 * NOTE: this function is not synchronized; external locking is mandatory.
 * (This is expected to be called at GC time.)
 */
const RegisterMap* dvmGetExpandedRegisterMap0(Method* method);
INLINE const RegisterMap* dvmGetExpandedRegisterMap(Method* method)
{
    const RegisterMap* curMap = method->registerMap;
    if (curMap == NULL)
        return NULL;
    RegisterMapFormat format = dvmRegisterMapGetFormat(curMap);
    if (format == kRegMapFormatCompact8 || format == kRegMapFormatCompact16) {
        return curMap;
    } else {
        return dvmGetExpandedRegisterMap0(method);
    }
}

/*
 * Current status; these map to JDWP constants, so don't rearrange them.
 * (If you do alter this, update the strings in dvmDumpThread and the
 * conversion table in VMThread.java.)
 *
 * Note that "suspended" is orthogonal to these values (so says JDWP).
 */
enum ThreadStatus {
    THREAD_UNDEFINED    = -1,       /* makes enum compatible with int32_t */

    /* these match up with JDWP values */
    THREAD_ZOMBIE       = 0,        /* TERMINATED */
    THREAD_RUNNING      = 1,        /* RUNNABLE or running now */
    THREAD_TIMED_WAIT   = 2,        /* TIMED_WAITING in Object.wait() */
    THREAD_MONITOR      = 3,        /* BLOCKED on a monitor */
    THREAD_WAIT         = 4,        /* WAITING in Object.wait() */
    /* non-JDWP states */
    THREAD_INITIALIZING = 5,        /* allocated, not yet running */
    THREAD_STARTING     = 6,        /* started, not yet on thread list */
    THREAD_NATIVE       = 7,        /* off in a JNI native method */
    THREAD_VMWAIT       = 8,        /* waiting on a VM resource */
    THREAD_SUSPENDED    = 9,        /* suspended, usually by GC or debugger */
};

/*
 * Used when changing thread state.  Threads may only change their own.
 * The "self" argument, which may be NULL, is accepted as an optimization.
 *
 * If you're calling this before waiting on a resource (e.g. THREAD_WAIT
 * or THREAD_MONITOR), do so in the same function as the wait -- this records
 * the current stack depth for the GC.
 *
 * If you're changing to THREAD_RUNNING, this will check for suspension.
 *
 * Returns the old status.
 */
ThreadStatus dvmChangeStatus(Thread *self, ThreadStatus newStatus);

/* utility function to get the tid */
pid_t dvmGetSysThreadId(void);

/*
 * Get our Thread* from TLS.
 *
 * Returns NULL if this isn't a thread that the VM is aware of.
 */
Thread* dvmThreadSelf(void);

/* system init/shutdown */
bool dvmJniStartup(void);
void dvmJniShutdown(void);

bool dvmIsBadJniVersion(int version);

/*
 * Retrieve the system (a/k/a application) class loader.
 *
 * The caller must call dvmReleaseTrackedAlloc on the result.
 */
Object* dvmGetSystemClassLoader();

/*
 * Stop tracking an object.
 *
 * We allow attempts to delete NULL "obj" so that callers don't have to wrap
 * calls with "if != NULL".
 */
void dvmReleaseTrackedAlloc(Object* obj, Thread* self);

/*
 * Our data structures for JNIEnv and JavaVM.
 *
 * Native code thinks it has a pointer to a pointer.  We know better.
 */
struct JavaVMExt;

struct JNIEnvExt {
    const struct JNINativeInterface* funcTable;     /* must be first */

    const struct JNINativeInterface* baseFuncTable;

    u4      envThreadId;
    Thread* self;

    /* if nonzero, we are in a "critical" JNI call */
    int     critical;

    struct JNIEnvExt* prev;
    struct JNIEnvExt* next;
};

struct JavaVMExt {
    const struct JNIInvokeInterface* funcTable;     /* must be first */

    const struct JNIInvokeInterface* baseFuncTable;

    /* head of list of JNIEnvs associated with this VM */
    JNIEnvExt*      envList;
    pthread_mutex_t envListLock;
};

/*
 * Native function return type; used by dvmPlatformInvoke().
 *
 * This is part of Method.jniArgInfo, and must fit in 3 bits.
 * Note: Assembly code in arch/<arch>/Call<arch>.S relies on
 * the enum values defined here.
 */
enum DalvikJniReturnType {
    DALVIK_JNI_RETURN_VOID = 0,     /* must be zero */
    DALVIK_JNI_RETURN_FLOAT = 1,
    DALVIK_JNI_RETURN_DOUBLE = 2,
    DALVIK_JNI_RETURN_S8 = 3,
    DALVIK_JNI_RETURN_S4 = 4,
    DALVIK_JNI_RETURN_S2 = 5,
    DALVIK_JNI_RETURN_U2 = 6,
    DALVIK_JNI_RETURN_S1 = 7
};

#define DALVIK_JNI_NO_ARG_INFO  0x80000000
#define DALVIK_JNI_RETURN_MASK  0x70000000
#define DALVIK_JNI_RETURN_SHIFT 28
#define DALVIK_JNI_COUNT_MASK   0x0f000000
#define DALVIK_JNI_COUNT_SHIFT  24

void dvmCallJNIMethod(const u4* args, JValue* pResult,
    const Method* method, Thread* self);
void dvmCheckCallJNIMethod(const u4* args, JValue* pResult,
    const Method* method, Thread* self);

/*
 * Configure "method" to use the JNI bridge to call "func".
 */
void dvmUseJNIBridge(Method* method, void* func);


/*
 * Enable the "checked" versions.
 */
void dvmUseCheckedJniEnv(JNIEnvExt* pEnv);
void dvmUseCheckedJniVm(JavaVMExt* pVm);
void dvmLateEnableCheckedJni(void);

/*
 * Decode a local, global, or weak-global reference.
 */
Object* dvmDecodeIndirectRef(Thread* self, jobject jobj);

/*
 * Verify that a reference passed in from native code is valid.  Returns
 * an indication of local/global/invalid.
 */
jobjectRefType dvmGetJNIRefType(Thread* self, jobject jobj);

/*
 * Get the last method called on the interp stack.  This is the method
 * "responsible" for calling into JNI.
 */
const Method* dvmGetCurrentJNIMethod(void);

/*
 * Create/destroy a JNIEnv for the current thread.
 */
JNIEnv* dvmCreateJNIEnv(Thread* self);
void dvmDestroyJNIEnv(JNIEnv* env);

/*
 * Find the JNIEnv associated with the current thread.
 */
JNIEnvExt* dvmGetJNIEnvForThread(void);

/*
 * Release all MonitorEnter-acquired locks that are still held.  Called at
 * DetachCurrentThread time.
 */
void dvmReleaseJniMonitors(Thread* self);

/*
 * Dump the contents of the JNI reference tables to the log file.
 *
 * The local ref tables associated with other threads are not included.
 */
void dvmDumpJniReferenceTables(void);

// Dumps JNI statistics in response to SIGQUIT.
struct DebugOutputTarget;
void dvmDumpJniStats(DebugOutputTarget* target);

/* debugging */
void dvmDumpObject(const Object* obj);


/*
 * Create a new linear allocation block.
 */
struct LinearAllocHdr* dvmLinearAllocCreate(Object* classLoader);

/*
 * Destroy a linear allocation area.
 *
 * We do a trivial "has everything been freed?" check before unmapping the
 * memory and freeing the LinearAllocHdr.
 */
void dvmLinearAllocDestroy(Object* classLoader);

/*
 * Allocate "size" bytes of storage, associated with a particular class
 * loader.
 *
 * It's okay for size to be zero.
 *
 * We always leave "curOffset" pointing at the next place where we will
 * store the header that precedes the returned storage.
 *
 * This aborts the VM on failure, so it's not necessary to check for a
 * NULL return value.
 */
void* dvmLinearAlloc(Object* classLoader, size_t size);

/*
 * "Reallocate" a piece of memory.
 *
 * If the new size is <= the old size, we return the original pointer
 * without doing anything.
 *
 * If the new size is > the old size, we allocate new storage, copy the
 * old stuff over, and mark the new stuff as free.
 */
void* dvmLinearRealloc(Object* classLoader, void* mem, size_t newSize);

/*
 * Try to mark the pages in which a chunk of memory lives as read-only.
 * Whether or not the pages actually change state depends on how many
 * others are trying to access the same pages.
 *
 * Only call here if ENFORCE_READ_ONLY is true.
 */
void dvmLinearSetReadOnly(Object* classLoader, void* mem);

/*
 * Make the pages on which "mem" sits read-write.
 *
 * This covers the header as well as the data itself.  (We could add a
 * "header-only" mode for dvmLinearFree.)
 *
 * Only call here if ENFORCE_READ_ONLY is true.
 */
void dvmLinearSetReadWrite(Object* classLoader, void* mem);

/*
 * Mark an allocation as free.
 */
void dvmLinearFree(Object* classLoader, void* mem); 

/*
 * For debugging, dump the contents of a linear alloc area.
 *
 * We grab the lock so that the header contents and list output are
 * consistent.
 */
void dvmLinearAllocDump(Object* classLoader);

/*
 * Determine if [start, start+length) is contained in the in-use area of
 * a single LinearAlloc.  The full set of linear allocators is scanned.
 *
 * [ Since we currently only have one region, this is pretty simple.  In
 * the future we'll need to traverse a table of class loaders. ]
 */
bool dvmLinearAllocContains(const void* start, size_t length);

/*
 * We have a method pointer for a method in "clazz", but it might be
 * pointing to a method in a derived class.  We want to find the actual entry
 * from the class' vtable.  If "clazz" is an interface, we have to do a
 * little more digging.
 *
 * For "direct" methods (private / constructor), we just return the
 * original Method.
 *
 * (This is used for reflection and JNI "call method" calls.)
 */
Method* dvmGetVirtualizedMethod(const ClassObject* clazz, const Method* meth);

/* Dalvik puts private, static, and constructors into non-virtual table */
inline bool dvmIsDirectMethod(const Method* method);

/*
 * Find the class object representing the primitive type with the
 * given descriptor. This returns NULL if the given type character
 * is invalid.
 */
ClassObject* dvmFindPrimitiveClass(char type);

/*
 * Create a wrapper object for a primitive data type.  If "returnType" is
 * not primitive, this just casts "value" to an object and returns it.
 *
 * We could invoke the "toValue" method on the box types to take
 * advantage of pre-created values, but running that through the
 * interpreter is probably less efficient than just allocating storage here.
 *
 * The caller must call dvmReleaseTrackedAlloc on the result.
 */
DataObject* dvmBoxPrimitive(jvalue value, ClassObject* returnType);

/*
 * Unwrap a primitive data type, if necessary.
 *
 * If "returnType" is not primitive, we just tuck "value" into JValue and
 * return it after verifying that it's the right type of object.
 *
 * Fails if the field is primitive and "value" is either not a boxed
 * primitive or is of a type that cannot be converted.
 *
 * Returns "true" on success, "false" on failure.
 */
bool dvmUnboxPrimitive(Object* value, ClassObject* returnType, JValue* pResult);

/*
 * Stop tracking an object.
 *
 * We allow attempts to delete NULL "obj" so that callers don't have to wrap
 * calls with "if != NULL".
 */
void dvmReleaseTrackedAlloc(Object* obj, Thread* self);

/*
 * Add a local reference for an object to the current stack frame.  When
 * the native function returns, the reference will be discarded.
 *
 * We need to allow the same reference to be added multiple times.
 *
 * This will be called on otherwise unreferenced objects.  We cannot do
 * GC allocations here, and it's best if we don't grab a mutex.
 */
jobject addLocalReference(Thread* self, Object* obj);

/*
 * Add a global reference for an object.
 *
 * We may add the same object more than once.  Add/remove calls are paired,
 * so it needs to appear on the list multiple times.
 */
jobject addGlobalReference(Object* obj);


/*
 * Symbol
 */
#define SYMBOL_dvmDumpObject                 "_Z13dvmDumpObjectPK6Object"
#define SYMBOL_dvmThreadSelf                 "_Z13dvmThreadSelfv"
#define SYMBOL_dvmChangeStatus               "_Z15dvmChangeStatusP6Thread12ThreadStatus"
#define SYMBOL_dvmDecodeIndirectRef          "_Z20dvmDecodeIndirectRefP6ThreadP8_jobject"
#define SYMBOL_dvmAddClassToHash             "_Z17dvmAddClassToHashP11ClassObject"
#define SYMBOL_dvmGetSystemClassLoader       "_Z23dvmGetSystemClassLoaderv"
#define SYMBOL_dvmReleaseTrackedAlloc        "dvmReleaseTrackedAlloc"
#define SYMBOL_dvmLinearAllocCreate          "_Z20dvmLinearAllocCreateP6Object"
#define SYMBOL_dvmLinearAllocDestroy         "_Z21dvmLinearAllocDestroyP6Object"
#define SYMBOL_dvmLinearAlloc                "_Z14dvmLinearAllocP6Objectj"
#define SYMBOL_dvmLinearRealloc              "_Z16dvmLinearReallocP6ObjectPvj"
#define SYMBOL_dvmLinearSetReadOnly          "_Z20dvmLinearSetReadOnlyP6ObjectPv"
#define SYMBOL_dvmLinearSetReadWrite         "_Z21dvmLinearSetReadWriteP6ObjectPv"
#define SYMBOL_dvmLinearFree                 "_Z13dvmLinearFreeP6ObjectPv"
#define SYMBOL_dvmLinearAllocDump            "_Z18dvmLinearAllocDumpP6Object"
#define SYMBOL_dvmLinearAllocContains        "_Z22dvmLinearAllocContainsPKvj"
#define SYMBOL_dvmGetVirtualizedMethod       "_Z23dvmGetVirtualizedMethodPK11ClassObjectPK6Method"
#define SYMBOL_dexProtoGetReturnType         "_Z21dexProtoGetReturnTypePK8"
#define SYMBOL_dexProtoGetParameterCount     "_Z25dexProtoGetParameterCountPK8DexProto"
#define SYMBOL_dexProtoComputeArgsSize       "_Z23dexProtoComputeArgsSizePK8DexProto"
#define SYMBOL_dexProtoCopyMethodDescriptor  "_Z28dexProtoCopyMethodDescriptorPK8DexProto"
#define SYMBOL_dexProtoGetShorty             "_Z17dexProtoGetShortyPK8DexProto"
#define SYMBOL_dexStringCacheInit            "_Z18dexStringCacheInitP14DexStringCache"
#define SYMBOL_dexStringCacheRelease         "_Z21dexStringCacheReleaseP14DexStringCache"
#define SYMBOL_dexProtoGetMethodDescriptor   "_Z27dexProtoGetMethodDescriptorPK8DexProtoP14DexStringCache"
#define SYMBOL_dvmGetCurrentJNIMethod        "_Z22dvmGetCurrentJNIMethodv"
#define SYMBOL_dvmIsDirectMethod			 "_Z17dvmIsDirectMethodPK6Method" // may not exist
#define SYMBOL_dvmFindPrimitiveClass         "_Z21dvmFindPrimitiveClassc"
#define SYMBOL_dvmBoxPrimitive               "_Z15dvmBoxPrimitive6JValueP11ClassObject"
#define SYMBOL_dvmUnboxPrimitive             "_Z17dvmUnboxPrimitiveP6ObjectP11ClassObjectP6JValue"
#define SYMBOL_dvmReleaseTrackedAlloc        "dvmReleaseTrackedAlloc"
#define SYMBOL_gDvm                          "gDvm"
#define SYMBOL_gDvmInlineOpsTable            "gDvmInlineOpsTable"

// adb pull /system/lib/libdvm.so %TEMP%
// nm %TEMP%/libdvm.so | findstr dvmLinearAllocDump